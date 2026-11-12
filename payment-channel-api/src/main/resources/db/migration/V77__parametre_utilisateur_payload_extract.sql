-- Extraction payload générique : source + script Groovy (REGEX via attribut)

ALTER TABLE parametre_utilisateur
    ALTER COLUMN attribut TYPE VARCHAR(500);

ALTER TABLE parametre_utilisateur
    ADD COLUMN IF NOT EXISTS source_parametre VARCHAR(100),
    ADD COLUMN IF NOT EXISTS extract_script TEXT;

COMMENT ON COLUMN parametre_utilisateur.source_parametre IS
    'Clé payload source (ex. vignette, reference) pour extraction REGEX/Groovy';
COMMENT ON COLUMN parametre_utilisateur.attribut IS
    'REGEX:pattern[:group] | code compte (NUMERO_COMPTE) | SCRIPT';
COMMENT ON COLUMN parametre_utilisateur.extract_script IS
    'Script Groovy si règle complexe (bindings: source, params)';
