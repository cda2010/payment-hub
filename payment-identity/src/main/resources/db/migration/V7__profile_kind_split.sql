-- Sépare les profils d'offre en SERVICE (produits) et TARIFF (grilles).
-- Les profils existants deviennent SERVICE ; leurs grilles migrent vers des jumeaux *-FEE.

ALTER TABLE identity_profile
    ADD COLUMN IF NOT EXISTS kind VARCHAR(32) NOT NULL DEFAULT 'SERVICE';

UPDATE identity_profile SET kind = 'SERVICE' WHERE kind IS NULL OR kind = '';

CREATE INDEX IF NOT EXISTS idx_identity_profile_kind ON identity_profile (tenant_id, kind);

COMMENT ON COLUMN identity_profile.kind IS
    'SERVICE = services catalogue ; TARIFF = grilles tarifaires de référence';

-- ── Jumeaux tarifaires (parties) ────────────────────────────────────────────

INSERT INTO identity_profile (id, tenant_id, code, libelle, description, audience, active, kind)
VALUES
    ('f1000001-0000-4000-8000-000000000101', 'CORIS_DEMO', 'RETAIL-FEE',
     'Retail — tarifs', 'Grilles tarifaires retail', 'RETAIL', TRUE, 'TARIFF'),
    ('f1000001-0000-4000-8000-000000000102', 'CORIS_DEMO', 'AGENT-FEE',
     'Agent — tarifs', 'Grilles tarifaires agent', 'AGENT', TRUE, 'TARIFF'),
    ('f1000001-0000-4000-8000-000000000103', 'CORIS_DEMO', 'CORPORATE-FEE',
     'Corporate — tarifs', 'Grilles tarifaires corporate', 'CORPORATE', TRUE, 'TARIFF')
ON CONFLICT (tenant_id, code) DO NOTHING;

UPDATE identity_profile_grid SET profile_id = 'f1000001-0000-4000-8000-000000000101'
 WHERE profile_id = 'f1000001-0000-4000-8000-000000000001';
UPDATE identity_profile_grid SET profile_id = 'f1000001-0000-4000-8000-000000000102'
 WHERE profile_id = 'f1000001-0000-4000-8000-000000000002';
UPDATE identity_profile_grid SET profile_id = 'f1000001-0000-4000-8000-000000000103'
 WHERE profile_id = 'f1000001-0000-4000-8000-000000000003';

INSERT INTO identity_party_profile (id, party_id, profile_id, date_debut, active)
VALUES
    ('f4000001-0000-4000-8000-000000000101', 'a1000001-0000-4000-8000-000000000006',
     'f1000001-0000-4000-8000-000000000101', DATE '2026-01-01', TRUE),
    ('f4000001-0000-4000-8000-000000000102', 'a1000001-0000-4000-8000-000000000005',
     'f1000001-0000-4000-8000-000000000102', DATE '2026-01-01', TRUE),
    ('f4000001-0000-4000-8000-000000000103', 'a1000001-0000-4000-8000-000000000002',
     'f1000001-0000-4000-8000-000000000103', DATE '2026-01-01', TRUE)
ON CONFLICT (party_id, profile_id) DO NOTHING;

-- ── Jumeaux tarifaires (réseau) ─────────────────────────────────────────────

INSERT INTO identity_profile (id, tenant_id, code, libelle, description, audience, active, kind)
VALUES
    ('f1000001-0000-4000-8000-000000000110', 'CORIS_DEMO', 'FSE-FEE',
     'FSE — tarifs', 'Grilles FSE', 'CORPORATE', TRUE, 'TARIFF'),
    ('f1000001-0000-4000-8000-000000000111', 'CORIS_DEMO', 'AGENCE-FEE',
     'Agence — tarifs', 'Grilles agence', 'AGENT', TRUE, 'TARIFF'),
    ('f1000001-0000-4000-8000-000000000112', 'CORIS_DEMO', 'MASTER-FEE',
     'Master — tarifs', 'Grilles master', 'AGENT', TRUE, 'TARIFF'),
    ('f1000001-0000-4000-8000-000000000113', 'CORIS_DEMO', 'MARCHAND-FEE',
     'Marchand — tarifs', 'Grilles marchand', 'MERCHANT', TRUE, 'TARIFF'),
    ('f1000001-0000-4000-8000-000000000114', 'CORIS_DEMO', 'SD-FEE',
     'Sous-distributeur — tarifs', 'Grilles SD', 'AGENT', TRUE, 'TARIFF')
ON CONFLICT (tenant_id, code) DO NOTHING;

UPDATE identity_profile_grid SET profile_id = 'f1000001-0000-4000-8000-000000000110'
 WHERE profile_id = 'f1000001-0000-4000-8000-000000000010';
UPDATE identity_profile_grid SET profile_id = 'f1000001-0000-4000-8000-000000000111'
 WHERE profile_id = 'f1000001-0000-4000-8000-000000000011';
UPDATE identity_profile_grid SET profile_id = 'f1000001-0000-4000-8000-000000000112'
 WHERE profile_id = 'f1000001-0000-4000-8000-000000000012';
UPDATE identity_profile_grid SET profile_id = 'f1000001-0000-4000-8000-000000000113'
 WHERE profile_id = 'f1000001-0000-4000-8000-000000000013';
UPDATE identity_profile_grid SET profile_id = 'f1000001-0000-4000-8000-000000000114'
 WHERE profile_id = 'f1000001-0000-4000-8000-000000000014';

-- Dupliquer les affectations unités vers les profils TARIFF correspondants
INSERT INTO identity_unite_profile (id, unite_id, profile_id, date_debut, active)
SELECT
    gen_random_uuid(),
    u.unite_id,
    CASE u.profile_id
        WHEN 'f1000001-0000-4000-8000-000000000010' THEN 'f1000001-0000-4000-8000-000000000110'::uuid
        WHEN 'f1000001-0000-4000-8000-000000000011' THEN 'f1000001-0000-4000-8000-000000000111'::uuid
        WHEN 'f1000001-0000-4000-8000-000000000012' THEN 'f1000001-0000-4000-8000-000000000112'::uuid
        WHEN 'f1000001-0000-4000-8000-000000000013' THEN 'f1000001-0000-4000-8000-000000000113'::uuid
        WHEN 'f1000001-0000-4000-8000-000000000014' THEN 'f1000001-0000-4000-8000-000000000114'::uuid
    END,
    u.date_debut,
    u.active
FROM identity_unite_profile u
WHERE u.profile_id IN (
    'f1000001-0000-4000-8000-000000000010',
    'f1000001-0000-4000-8000-000000000011',
    'f1000001-0000-4000-8000-000000000012',
    'f1000001-0000-4000-8000-000000000013',
    'f1000001-0000-4000-8000-000000000014'
)
ON CONFLICT (unite_id, profile_id) DO NOTHING;

UPDATE identity_profile SET libelle = 'Retail — services', description = 'Services catalogue retail'
 WHERE id = 'f1000001-0000-4000-8000-000000000001';
UPDATE identity_profile SET libelle = 'Agent — services', description = 'Services catalogue agent'
 WHERE id = 'f1000001-0000-4000-8000-000000000002';
UPDATE identity_profile SET libelle = 'Corporate — services', description = 'Services catalogue corporate'
 WHERE id = 'f1000001-0000-4000-8000-000000000003';
UPDATE identity_profile SET libelle = 'FSE — services' WHERE id = 'f1000001-0000-4000-8000-000000000010';
UPDATE identity_profile SET libelle = 'Agence — services' WHERE id = 'f1000001-0000-4000-8000-000000000011';
UPDATE identity_profile SET libelle = 'Master — services' WHERE id = 'f1000001-0000-4000-8000-000000000012';
UPDATE identity_profile SET libelle = 'Marchand — services' WHERE id = 'f1000001-0000-4000-8000-000000000013';
UPDATE identity_profile SET libelle = 'Sous-distributeur — services' WHERE id = 'f1000001-0000-4000-8000-000000000014';
