package com.gateway.payment.orchestration.service;

import com.gateway.payment.orchestration.domain.ParametreUtilisateur;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.github.benmanes.caffeine.cache.LoadingCache;
import groovy.lang.Binding;
import groovy.lang.GroovyShell;
import groovy.lang.Script;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.time.Duration;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Extraction de sous-valeurs depuis un champ payload composé.
 * <p>
 * Source = {@code codeParametreMethode} (clé payload), cible = {@code codeParametre}.
 * {@code sourceParametre} reste un override optionnel.
 * <ol>
 *   <li><b>REGEX</b> (défaut) — {@code attribut = REGEX:pattern} ou {@code REGEX:pattern:group}</li>
 *   <li><b>Groovy</b> — si {@code extractScript} renseigné (règles complexes)</li>
 * </ol>
 * Les codes compte ({@code NUMERO_COMPTE}, …) sont ignorés ici (traités par {@link CompteFieldExtractorRegistry}).
 */
@Component
public class ParametrePayloadExtractor {

    private static final Logger log = LoggerFactory.getLogger(ParametrePayloadExtractor.class);

    private static final Pattern REGEX_SPEC = Pattern.compile(
            "(?i)^REGEX:(.+?)(?::(\\d+))?$"
    );

    private static final Set<String> COMPTE_CODES = Set.of(
            "TEL_QUEL", "NUMERO_COMPTE", "CODE_BANQUE", "CODE_AGENCE", "CLE", "DEVISE",
            "FSE_TENEUR", "FSE_TITULAIRE", "LIBELLE_COMPTE", "CODE_UO_TITULAIRE",
            "AGENCE_CODE", "TYPE_COMPTE", "SCRIPT",
            "GETNUMEROCOMPTE", "NUMEROCOMPTE", "GETCODEBANQUE", "CODEBANQUE", "COMPTEBANQUE",
            "GETCODEAGENCE", "CODEAGENCE", "GETCLE", "GETDEVISE", "GETFSETENEUR", "FSETENEUR",
            "GETFSETITULAIRE", "FSETITULAIRE", "GETLIBELLECOMPTE", "LIBELLECOMPTE",
            "GETCODEUOTITULAIRE", "CODEUOTITULAIRE", "CODEUO", "GETAGENCECODE", "AGENCECODE",
            "AGENCE", "GETTYPECOMPTE", "TYPECOMPTE"
    );

    private final LoadingCache<String, Pattern> patternCache = Caffeine.newBuilder()
            .maximumSize(256)
            .expireAfterAccess(Duration.ofHours(1))
            .build(Pattern::compile);

    private final ConcurrentHashMap<String, Class<?>> groovyClassCache = new ConcurrentHashMap<>();
    private final GroovyShell groovyShell = new GroovyShell();

    public ParametrePayloadExtractor() {
    }

    /**
     * Enrichit {@code userParams} in-place : écrit {@code codeParametre} avec la sous-valeur extraite.
     */
    public void apply(List<ParametreUtilisateur> interfaceParams, Map<String, String> userParams) {
        if (interfaceParams == null || interfaceParams.isEmpty() || userParams == null) {
            return;
        }
        interfaceParams.stream()
                .sorted(Comparator.comparingInt(ParametreUtilisateur::getOrdre))
                .forEach(p -> applyOne(p, userParams));
    }

    private void applyOne(ParametreUtilisateur p, Map<String, String> userParams) {
        if (!StringUtils.hasText(p.getCodeParametre())) {
            return;
        }
        boolean hasScript = StringUtils.hasText(p.getExtractScript());
        String attribut = p.getAttribut();
        // Source payload = code méthode (ex. vignette) ; sourceParametre = override optionnel
        String sourceKey = firstNonBlank(p.getSourceParametre(), p.getCodeParametreMethode());
        boolean hasSource = StringUtils.hasText(sourceKey);

        if (!hasScript && !hasSource) {
            return;
        }
        if (!hasScript && isCompteAttribut(attribut)) {
            return;
        }
        if (!hasScript && !StringUtils.hasText(attribut)) {
            return;
        }

        String source = hasSource ? userParams.get(sourceKey.trim()) : null;

        String extracted = null;
        try {
            if (hasScript) {
                extracted = evalGroovy(p.getExtractScript().trim(), source, userParams);
            } else {
                extracted = evalRegex(attribut.trim(), source);
            }
        } catch (Exception ex) {
            log.warn(
                    "Extraction PU {} échouée (sourceKey={}, attribut={}): {}",
                    p.getCodeParametre(), sourceKey, attribut, ex.getMessage()
            );
            return;
        }

        if (StringUtils.hasText(extracted)) {
            userParams.put(p.getCodeParametre(), extracted.trim());
        }
    }

    private static String firstNonBlank(String... values) {
        if (values == null) {
            return null;
        }
        for (String v : values) {
            if (StringUtils.hasText(v)) {
                return v.trim();
            }
        }
        return null;
    }

    String evalRegex(String attribut, String source) {
        if (!StringUtils.hasText(source)) {
            return null;
        }
        String patternText;
        int group = 1;
        Matcher spec = REGEX_SPEC.matcher(attribut);
        if (spec.matches()) {
            patternText = spec.group(1);
            if (spec.group(2) != null) {
                group = Integer.parseInt(spec.group(2));
            }
        } else {
            // Défaut : attribut = pattern regex brut
            patternText = attribut;
        }
        Pattern pattern = patternCache.get(patternText);
        Matcher m = pattern.matcher(source);
        if (!m.find()) {
            return null;
        }
        if (group > 0 && group <= m.groupCount()) {
            return m.group(group);
        }
        return m.group();
    }

    String evalGroovy(String script, String source, Map<String, String> params) {
        assertScriptSafe(script);
        Class<?> scriptClass = groovyClassCache.computeIfAbsent(script, s -> {
            Script compiled = groovyShell.parse(s);
            return compiled.getClass();
        });
        try {
            Script instance = (Script) scriptClass.getDeclaredConstructor().newInstance();
            Binding binding = new Binding();
            binding.setVariable("source", source);
            binding.setVariable("params", Map.copyOf(params));
            instance.setBinding(binding);
            Object result = instance.run();
            return result == null ? null : String.valueOf(result);
        } catch (ReflectiveOperationException ex) {
            throw new IllegalStateException("Groovy script instantiate failed", ex);
        }
    }

    /** Garde-fou léger (scripts admin) — pas un sandbox complet. */
    static void assertScriptSafe(String script) {
        String lower = script.toLowerCase(Locale.ROOT);
        for (String forbidden : List.of(
                "system.", "runtime.", "processbuilder", "classloader",
                "execute(", "groovy.lang.groovyclassloader", "javax.script"
        )) {
            if (lower.contains(forbidden)) {
                throw new IllegalArgumentException("Script Groovy refusé (motif: " + forbidden + ")");
            }
        }
    }

    static boolean isCompteAttribut(String attribut) {
        if (!StringUtils.hasText(attribut)) {
            return false;
        }
        if (attribut.regionMatches(true, 0, "REGEX:", 0, 6)) {
            return false;
        }
        String key = attribut.trim().toUpperCase(Locale.ROOT).replace("_", "");
        return COMPTE_CODES.contains(key) || COMPTE_CODES.contains(attribut.trim().toUpperCase(Locale.ROOT));
    }
}
