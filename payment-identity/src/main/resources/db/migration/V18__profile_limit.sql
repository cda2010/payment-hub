-- Profil de limite : pour un produit et un sens, le code du groupe de règles (payment-rules).
-- Les montants, périodes et compteurs restent dans payment-rules.

CREATE TABLE identity_profile_limit (
    id            UUID PRIMARY KEY,
    profile_id    UUID         NOT NULL REFERENCES identity_profile (id) ON DELETE CASCADE,
    produit_code  VARCHAR(80)  NOT NULL,
    sens          VARCHAR(32)  NOT NULL,
    groupe_code   VARCHAR(64)  NOT NULL,
    CONSTRAINT uq_identity_profile_limit UNIQUE (profile_id, produit_code, sens)
);

CREATE INDEX idx_identity_profile_limit_profile ON identity_profile_limit (profile_id);

COMMENT ON COLUMN identity_profile.kind IS
    'SERVICE = services catalogue ; TARIFF = grilles tarifaires ; LIMIT = groupes de plafonds';

INSERT INTO identity_profile (id, tenant_id, code, libelle, description, audience, active, kind)
VALUES
    ('f1000001-0000-4000-8000-000000000201', 'CORIS_DEMO', 'RETAIL-LIMITE',
     'Retail — limites', 'Plafonds retail : retrait, dépôt, détention du transfert', 'RETAIL', TRUE, 'LIMIT')
ON CONFLICT (tenant_id, code) DO NOTHING;

INSERT INTO identity_profile_limit (id, profile_id, produit_code, sens, groupe_code)
VALUES
    ('f5000001-0000-4000-8000-000000000001', 'f1000001-0000-4000-8000-000000000201',
     'CASH_OUT', 'EMETTEUR', 'CASH_OUT_STANDARD'),
    ('f5000001-0000-4000-8000-000000000002', 'f1000001-0000-4000-8000-000000000201',
     'CASH_IN', 'EMETTEUR', 'CASH_IN_STANDARD'),
    ('f5000001-0000-4000-8000-000000000003', 'f1000001-0000-4000-8000-000000000201',
     'ACCOUNT_TRANSFER', 'DETENTION', 'ACCOUNT_TRANSFER_CREDIT')
ON CONFLICT (profile_id, produit_code, sens) DO NOTHING;

INSERT INTO identity_party_profile (id, party_id, profile_id, date_debut, active)
SELECT
    'f4000001-0000-4000-8000-000000000201',
    'a1000001-0000-4000-8000-000000000006',
    'f1000001-0000-4000-8000-000000000201',
    DATE '2026-01-01',
    TRUE
WHERE EXISTS (
    SELECT 1 FROM identity_party WHERE id = 'a1000001-0000-4000-8000-000000000006'
)
ON CONFLICT (party_id, profile_id) DO NOTHING;
