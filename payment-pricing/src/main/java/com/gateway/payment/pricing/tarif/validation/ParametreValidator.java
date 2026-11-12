package com.gateway.payment.pricing.tarif.validation;

import com.gateway.payment.pricing.tarif.domain.Formule;
import com.gateway.payment.pricing.tarif.domain.FormuleParametre;
import com.gateway.payment.pricing.tarif.domain.TypeFormule;
import com.gateway.payment.pricing.tarif.formule.ParametresValides;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;
import java.util.stream.Collectors;

/**
 * Contrôle des valeurs saisies pour une formule (EF-007, §6.2) : formule active, aucun paramètre inconnu,
 * obligatoires présents, types, bornes, précision et listes autorisées. Les valeurs par défaut ne
 * s'appliquent qu'aux paramètres facultatifs. Produit les valeurs typées utilisées par le calcul.
 */
@Component
public class ParametreValidator {

    public record Resultat(List<ValidationIssue> erreurs, ParametresValides valeurs) {
        public boolean valide() {
            return erreurs.isEmpty();
        }
    }

    public Resultat valider(Formule formule, Map<String, Object> saisis, Integer tranche) {
        List<ValidationIssue> erreurs = new ArrayList<>();
        Map<String, Object> valeurs = new LinkedHashMap<>();
        Map<String, Object> entrees = saisis != null ? saisis : Map.of();

        if (formule == null) {
            erreurs.add(ValidationIssue.tranche(tranche, "FORMULE_INCONNUE", "formule", "Formule inconnue."));
            return new Resultat(erreurs, new ParametresValides(valeurs));
        }
        if (!formule.estActive()) {
            erreurs.add(ValidationIssue.tranche(tranche, "FORMULE_INACTIVE", "formule",
                    "La formule " + formule.libelleVersion() + " est inactive."));
        }
        if (formule.getType() == TypeFormule.EXPRESSION) {
            erreurs.add(ValidationIssue.tranche(tranche, "FORMULE_NON_SUPPORTEE", "formule",
                    "Les formules de type EXPRESSION ne sont pas encore activées (" + formule.libelleVersion() + ")."));
        }

        Set<String> declares = formule.getParametres().stream()
                .map(FormuleParametre::getCode)
                .collect(Collectors.toSet());
        entrees.keySet().stream()
                .filter(code -> !declares.contains(code))
                .sorted()
                .forEach(code -> erreurs.add(ValidationIssue.tranche(tranche, "PARAM_NON_DECLARE", champ(code),
                        "Paramètre « " + code + " » non déclaré par la formule " + formule.libelleVersion() + ".")));

        for (FormuleParametre p : formule.getParametres()) {
            Object brut = entrees.get(p.getCode());
            if (brut == null) {
                if (p.isObligatoire()) {
                    erreurs.add(issue(tranche, p, "PARAM_OBLIGATOIRE_MANQUANT",
                            "Paramètre obligatoire manquant : " + nom(p) + "."));
                } else if (p.getValeurDefaut() != null) {
                    Object defaut = convertirDefaut(p);
                    if (defaut == null) {
                        erreurs.add(issue(tranche, p, "PARAM_DEFAUT_INVALIDE",
                                "Valeur par défaut invalide pour " + nom(p) + " : « " + p.getValeurDefaut() + " »."));
                    } else {
                        valeurs.put(p.getCode(), defaut);
                    }
                }
                continue;
            }
            Object valeur = controler(p, brut, tranche, erreurs);
            if (valeur != null) {
                valeurs.put(p.getCode(), valeur);
            }
        }
        return new Resultat(erreurs, new ParametresValides(valeurs));
    }

    /** Retourne la valeur normalisée, ou null après avoir enregistré l'erreur. */
    private Object controler(FormuleParametre p, Object brut, Integer tranche, List<ValidationIssue> erreurs) {
        return switch (p.getTypeDonnee()) {
            case DECIMAL -> {
                BigDecimal v = enDecimal(brut);
                if (v == null) {
                    erreurs.add(typeInvalide(tranche, p, "un nombre décimal"));
                    yield null;
                }
                if (!dansLesBornes(p, v, tranche, erreurs)) {
                    yield null;
                }
                if (p.getNbDecimales() != null && echelle(v) > p.getNbDecimales()) {
                    erreurs.add(issue(tranche, p, "PARAM_PRECISION",
                            nom(p) + " accepte au plus " + p.getNbDecimales() + " décimale(s)."));
                    yield null;
                }
                yield v;
            }
            case INTEGER -> {
                BigDecimal v = enDecimal(brut);
                if (v == null || echelle(v) > 0) {
                    erreurs.add(typeInvalide(tranche, p, "un entier"));
                    yield null;
                }
                if (!dansLesBornes(p, v, tranche, erreurs)) {
                    yield null;
                }
                try {
                    yield v.longValueExact();
                } catch (ArithmeticException e) {
                    erreurs.add(typeInvalide(tranche, p, "un entier"));
                    yield null;
                }
            }
            case BOOLEAN -> {
                if (brut instanceof Boolean b) {
                    yield b;
                }
                erreurs.add(typeInvalide(tranche, p, "true ou false"));
                yield null;
            }
            case STRING -> {
                if (!(brut instanceof String s)) {
                    erreurs.add(typeInvalide(tranche, p, "un texte"));
                    yield null;
                }
                if (p.getLongueurMax() != null && s.length() > p.getLongueurMax()) {
                    erreurs.add(issue(tranche, p, "PARAM_HORS_BORNES",
                            nom(p) + " dépasse " + p.getLongueurMax() + " caractère(s)."));
                    yield null;
                }
                if (p.getFormat() != null && !respecteFormat(p.getFormat(), s)) {
                    erreurs.add(issue(tranche, p, "PARAM_FORMAT_INVALIDE",
                            nom(p) + " ne respecte pas le format attendu."));
                    yield null;
                }
                yield autorise(p, s, tranche, erreurs) ? s : null;
            }
            case ENUM -> {
                if (!(brut instanceof String s)) {
                    erreurs.add(typeInvalide(tranche, p, "une valeur de la liste"));
                    yield null;
                }
                yield autorise(p, s, tranche, erreurs) ? s : null;
            }
        };
    }

    private boolean dansLesBornes(FormuleParametre p, BigDecimal v, Integer tranche, List<ValidationIssue> erreurs) {
        boolean sousMin = p.getValeurMin() != null && v.compareTo(p.getValeurMin()) < 0;
        boolean surMax = p.getValeurMax() != null && v.compareTo(p.getValeurMax()) > 0;
        if (sousMin || surMax) {
            erreurs.add(issue(tranche, p, "PARAM_HORS_BORNES",
                    nom(p) + " doit être compris entre " + borne(p.getValeurMin()) + " et " + borne(p.getValeurMax())
                            + " (valeur saisie : " + v.toPlainString() + ")."));
            return false;
        }
        return true;
    }

    private boolean autorise(FormuleParametre p, String valeur, Integer tranche, List<ValidationIssue> erreurs) {
        List<String> liste = p.getValeursAutorisees();
        if (liste != null && !liste.isEmpty() && !liste.contains(valeur)) {
            erreurs.add(issue(tranche, p, "PARAM_VALEUR_NON_AUTORISEE",
                    nom(p) + " doit valoir l'une de : " + String.join(", ", liste) + "."));
            return false;
        }
        return true;
    }

    private Object convertirDefaut(FormuleParametre p) {
        String texte = p.getValeurDefaut().trim();
        try {
            return switch (p.getTypeDonnee()) {
                case DECIMAL -> new BigDecimal(texte);
                case INTEGER -> Long.parseLong(texte);
                case BOOLEAN -> switch (texte) {
                    case "true" -> Boolean.TRUE;
                    case "false" -> Boolean.FALSE;
                    default -> null;
                };
                case STRING, ENUM -> texte;
            };
        } catch (NumberFormatException e) {
            return null;
        }
    }

    /** Les nombres à virgule flottante (Double, Float) sont refusés : seuls les décimaux exacts sont admis. */
    private static BigDecimal enDecimal(Object brut) {
        if (brut instanceof BigDecimal d) {
            return d;
        }
        if (brut instanceof Integer || brut instanceof Long || brut instanceof Short || brut instanceof Byte) {
            return BigDecimal.valueOf(((Number) brut).longValue());
        }
        if (brut instanceof BigInteger i) {
            return new BigDecimal(i);
        }
        return null;
    }

    private static int echelle(BigDecimal v) {
        return Math.max(0, v.stripTrailingZeros().scale());
    }

    private static boolean respecteFormat(String regex, String valeur) {
        try {
            return Pattern.matches(regex, valeur);
        } catch (PatternSyntaxException e) {
            return false;
        }
    }

    private static ValidationIssue typeInvalide(Integer tranche, FormuleParametre p, String attendu) {
        return issue(tranche, p, "PARAM_TYPE_INVALIDE", nom(p) + " doit être " + attendu + ".");
    }

    private static ValidationIssue issue(Integer tranche, FormuleParametre p, String code, String message) {
        return ValidationIssue.tranche(tranche, code, champ(p.getCode()), message);
    }

    private static String nom(FormuleParametre p) {
        return p.getLibelle() + " (" + p.getCode() + ")";
    }

    private static String champ(String code) {
        return "parametres." + code;
    }

    private static String borne(BigDecimal valeur) {
        return valeur == null ? "∞" : valeur.stripTrailingZeros().toPlainString();
    }
}
