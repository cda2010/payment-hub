package com.gateway.payment.pricing.tarif.validation;

import com.gateway.payment.pricing.tarif.JeuDeDonnees;
import com.gateway.payment.pricing.tarif.domain.Formule;
import com.gateway.payment.pricing.tarif.domain.FormuleParametre;
import com.gateway.payment.pricing.tarif.domain.StatutFormule;
import com.gateway.payment.pricing.tarif.domain.TypeDonnee;
import com.gateway.payment.pricing.tarif.domain.TypeFormule;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class ParametreValidatorTest {

    private final ParametreValidator validator = new ParametreValidator();

    @Test
    void parametresValidesNormalisesEnBigDecimal() {
        ParametreValidator.Resultat r = validator.valider(JeuDeDonnees.excess(),
                Map.of("seuil", 8000, "taux", new BigDecimal("0.015"), "fraisFixes", 100), 2);

        assertThat(r.valide()).isTrue();
        assertThat(r.valeurs().decimal("seuil")).isEqualTo(new BigDecimal("8000"));
        assertThat(r.valeurs().decimal("fraisFixes")).isEqualTo(new BigDecimal("100"));
    }

    @Test
    void ta006_parametreObligatoireManquantAvecCodeEtLibelle() {
        ParametreValidator.Resultat r = validator.valider(JeuDeDonnees.excess(),
                Map.of("seuil", 8000, "fraisFixes", 100), 2);

        assertThat(r.erreurs()).singleElement().satisfies(e -> {
            assertThat(e.code()).isEqualTo("PARAM_OBLIGATOIRE_MANQUANT");
            assertThat(e.tranche()).isEqualTo(2);
            assertThat(e.champ()).isEqualTo("parametres.taux");
            assertThat(e.message()).contains("Taux", "taux");
        });
    }

    @Test
    void ta007_parametreNonDeclareRejete() {
        ParametreValidator.Resultat r = validator.valider(JeuDeDonnees.fixed(),
                Map.of("montantFixe", 100, "bonus", 5), 1);

        assertThat(r.erreurs()).singleElement().satisfies(e -> {
            assertThat(e.code()).isEqualTo("PARAM_NON_DECLARE");
            assertThat(e.champ()).isEqualTo("parametres.bonus");
        });
    }

    @Test
    void ta013_formuleInconnueOuInactiveRefusee() {
        assertThat(validator.valider(null, Map.of(), 1).erreurs())
                .extracting(ValidationIssue::code).containsExactly("FORMULE_INCONNUE");

        Formule inactive = JeuDeDonnees.fixed();
        inactive.setStatut(StatutFormule.INACTIVE);
        assertThat(validator.valider(inactive, Map.of("montantFixe", 100), 1).erreurs())
                .extracting(ValidationIssue::code).containsExactly("FORMULE_INACTIVE");
    }

    @Test
    void formuleExpressionPasEncoreSupportee() {
        Formule expression = JeuDeDonnees.fixed();
        expression.setType(TypeFormule.EXPRESSION);

        assertThat(validator.valider(expression, Map.of("montantFixe", 100), 1).erreurs())
                .extracting(ValidationIssue::code).containsExactly("FORMULE_NON_SUPPORTEE");
    }

    @Test
    void ta014_tauxNegatifOuSuperieurAUnRefuse() {
        assertThat(validator.valider(JeuDeDonnees.percentTotal(), Map.of("taux", new BigDecimal("-0.01")), 1).erreurs())
                .extracting(ValidationIssue::code).containsExactly("PARAM_HORS_BORNES");
        assertThat(validator.valider(JeuDeDonnees.percentTotal(), Map.of("taux", new BigDecimal("1.5")), 1).erreurs())
                .extracting(ValidationIssue::code).containsExactly("PARAM_HORS_BORNES");
    }

    @Test
    void typeInvalideRefuse() {
        assertThat(validator.valider(JeuDeDonnees.fixed(), Map.of("montantFixe", "100"), 1).erreurs())
                .extracting(ValidationIssue::code).containsExactly("PARAM_TYPE_INVALIDE");
        assertThat(validator.valider(JeuDeDonnees.fixed(), Map.of("montantFixe", 100.5d), 1).erreurs())
                .as("un double n'est jamais accepté pour un montant")
                .extracting(ValidationIssue::code).containsExactly("PARAM_TYPE_INVALIDE");
    }

    @Test
    void precisionDepasseeRefusee() {
        assertThat(validator.valider(JeuDeDonnees.percentTotal(), Map.of("taux", new BigDecimal("0.0000001")), 1).erreurs())
                .extracting(ValidationIssue::code).containsExactly("PARAM_PRECISION");
    }

    @Test
    void valeurParDefautAppliqueeAuxSeulsParametresFacultatifs() {
        ParametreValidator.Resultat r = validator.valider(JeuDeDonnees.percentTotal(),
                Map.of("taux", new BigDecimal("0.01")), 1);

        assertThat(r.valide()).isTrue();
        assertThat(r.valeurs().decimal("fraisFixes")).isEqualByComparingTo("0");

        Formule f = JeuDeDonnees.axPlusY();
        f.getParametres().get(1).setValeurDefaut("50");
        assertThat(validator.valider(f, Map.of("taux", new BigDecimal("0.01")), 1).erreurs())
                .as("un défaut ne remplace pas un paramètre obligatoire")
                .extracting(ValidationIssue::code).containsExactly("PARAM_OBLIGATOIRE_MANQUANT");
    }

    @Test
    void typesEntierBooleenEnumEtTexte() {
        Formule f = JeuDeDonnees.fixed();
        f.getParametres().clear();
        f.getParametres().add(param("nb", TypeDonnee.INTEGER, p -> p.setValeurMax(new BigDecimal("10"))));
        f.getParametres().add(param("actif", TypeDonnee.BOOLEAN, p -> { }));
        f.getParametres().add(param("mode", TypeDonnee.ENUM, p -> p.setValeursAutorisees(List.of("A", "B"))));
        f.getParametres().add(param("ref", TypeDonnee.STRING, p -> {
            p.setLongueurMax(4);
            p.setFormat("[A-Z]+");
        }));

        ParametreValidator.Resultat ok = validator.valider(f,
                Map.of("nb", 3, "actif", true, "mode", "B", "ref", "ABC"), 1);
        assertThat(ok.valide()).isTrue();
        assertThat(ok.valeurs().valeurs().get("nb")).isEqualTo(3L);

        ParametreValidator.Resultat ko = validator.valider(f,
                Map.of("nb", new BigDecimal("2.5"), "actif", "oui", "mode", "C", "ref", "abc"), 1);
        assertThat(ko.erreurs()).extracting(ValidationIssue::code).containsExactly(
                "PARAM_TYPE_INVALIDE", "PARAM_TYPE_INVALIDE", "PARAM_VALEUR_NON_AUTORISEE", "PARAM_FORMAT_INVALIDE");
    }

    private static FormuleParametre param(String code, TypeDonnee type, java.util.function.Consumer<FormuleParametre> config) {
        FormuleParametre p = FormuleParametre.builder().code(code).libelle(code).typeDonnee(type).obligatoire(true).build();
        config.accept(p);
        return p;
    }
}
