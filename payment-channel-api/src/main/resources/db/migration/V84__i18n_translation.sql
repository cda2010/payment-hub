-- Table centrale des traductions : tout texte visible utilisateur est référencé par
-- (entity_type, entity_id, field_path, lang). entity_id NULL = traduction globale (clé i18n libre).
CREATE TABLE IF NOT EXISTS i18n_translation (
    id           UUID         PRIMARY KEY,
    entity_type  VARCHAR(60)  NOT NULL,
    entity_id    UUID,
    field_path   VARCHAR(200) NOT NULL,
    lang         VARCHAR(10)  NOT NULL,
    text_value   TEXT         NOT NULL,
    created_at   TIMESTAMPTZ  NOT NULL DEFAULT NOW(),
    updated_at   TIMESTAMPTZ  NOT NULL DEFAULT NOW()
);

COMMENT ON COLUMN i18n_translation.entity_type IS
    'CATALOG_DE_SERVICE | CATEGORIE_SERVICE | CATALOG_DE_SERVICE_VERSION | … (portée de la clé)';
COMMENT ON COLUMN i18n_translation.entity_id IS
    'Id de l''entité traduite — NULL pour une clé i18n globale';
COMMENT ON COLUMN i18n_translation.field_path IS
    'Chemin du texte : name, description, configuration.roles[SOURCE].label, configuration.recap.fields[amount].label…';
COMMENT ON COLUMN i18n_translation.lang IS
    'Code langue normalisé (fr, en) — doit figurer dans gateway.i18n.supported-languages';

-- NULL est distinct en Postgres : deux index partiels au lieu d'une contrainte unique unique.
CREATE UNIQUE INDEX IF NOT EXISTS uk_i18n_translation_entity
    ON i18n_translation (entity_type, entity_id, field_path, lang)
    WHERE entity_id IS NOT NULL;

CREATE UNIQUE INDEX IF NOT EXISTS uk_i18n_translation_global
    ON i18n_translation (entity_type, field_path, lang)
    WHERE entity_id IS NULL;

CREATE INDEX IF NOT EXISTS idx_i18n_translation_scope
    ON i18n_translation (entity_type, entity_id);

-- Reprise de l'existant en fr : les colonnes actuelles restent la valeur de repli.
INSERT INTO i18n_translation (id, entity_type, entity_id, field_path, lang, text_value, created_at, updated_at)
SELECT gen_random_uuid(), 'CATALOG_DE_SERVICE', s.id, 'name', 'fr', s.name, NOW(), NOW()
FROM catalog_de_service s
WHERE s.name IS NOT NULL AND TRIM(s.name) <> ''
ON CONFLICT DO NOTHING;

INSERT INTO i18n_translation (id, entity_type, entity_id, field_path, lang, text_value, created_at, updated_at)
SELECT gen_random_uuid(), 'CATALOG_DE_SERVICE', s.id, 'description', 'fr', s.description, NOW(), NOW()
FROM catalog_de_service s
WHERE s.description IS NOT NULL AND TRIM(s.description) <> ''
ON CONFLICT DO NOTHING;

INSERT INTO i18n_translation (id, entity_type, entity_id, field_path, lang, text_value, created_at, updated_at)
SELECT gen_random_uuid(), 'CATEGORIE_SERVICE', c.id, 'name', 'fr', c.name, NOW(), NOW()
FROM categorie_service c
WHERE c.name IS NOT NULL AND TRIM(c.name) <> ''
ON CONFLICT DO NOTHING;

INSERT INTO i18n_translation (id, entity_type, entity_id, field_path, lang, text_value, created_at, updated_at)
SELECT gen_random_uuid(), 'CATEGORIE_SERVICE', c.id, 'description', 'fr', c.description, NOW(), NOW()
FROM categorie_service c
WHERE c.description IS NOT NULL AND TRIM(c.description) <> ''
ON CONFLICT DO NOTHING;

-- Libellés portés par configuration JSONB : rôles, récap, présentation, soft rules.
INSERT INTO i18n_translation (id, entity_type, entity_id, field_path, lang, text_value, created_at, updated_at)
SELECT gen_random_uuid(), 'CATALOG_DE_SERVICE_VERSION', v.id,
       'configuration.roles[' || (r->>'code') || '].label', 'fr', r->>'label', NOW(), NOW()
FROM catalog_de_service_version v,
     jsonb_array_elements(v.configuration->'roles') r
WHERE jsonb_typeof(v.configuration->'roles') = 'array'
  AND r->>'code' IS NOT NULL
  AND r->>'label' IS NOT NULL
ON CONFLICT DO NOTHING;

INSERT INTO i18n_translation (id, entity_type, entity_id, field_path, lang, text_value, created_at, updated_at)
SELECT gen_random_uuid(), 'CATALOG_DE_SERVICE_VERSION', v.id,
       'configuration.recap.' || k, 'fr', v.configuration->'recap'->>k, NOW(), NOW()
FROM catalog_de_service_version v,
     (VALUES ('title'), ('subtitle'), ('confirmLabel')) AS keys(k)
WHERE jsonb_typeof(v.configuration->'recap') = 'object'
  AND v.configuration->'recap'->>k IS NOT NULL
ON CONFLICT DO NOTHING;

INSERT INTO i18n_translation (id, entity_type, entity_id, field_path, lang, text_value, created_at, updated_at)
SELECT gen_random_uuid(), 'CATALOG_DE_SERVICE_VERSION', v.id,
       'configuration.recap.fields[' || (f->>'key') || '].label', 'fr', f->>'label', NOW(), NOW()
FROM catalog_de_service_version v,
     jsonb_array_elements(v.configuration->'recap'->'fields') f
WHERE jsonb_typeof(v.configuration->'recap'->'fields') = 'array'
  AND f->>'key' IS NOT NULL
  AND f->>'label' IS NOT NULL
ON CONFLICT DO NOTHING;

INSERT INTO i18n_translation (id, entity_type, entity_id, field_path, lang, text_value, created_at, updated_at)
SELECT gen_random_uuid(), 'CATALOG_DE_SERVICE_VERSION', v.id,
       'configuration.presentation.sections[' || (sec->>'id') || '].title', 'fr', sec->>'title', NOW(), NOW()
FROM catalog_de_service_version v,
     jsonb_array_elements(v.configuration->'presentation'->'sections') sec
WHERE jsonb_typeof(v.configuration->'presentation'->'sections') = 'array'
  AND sec->>'id' IS NOT NULL
  AND sec->>'title' IS NOT NULL
ON CONFLICT DO NOTHING;