-- Profils de limite des autres issues d'enrôlement retail.
-- RETAIL-LIMITE reste le refus. COMPTE = ouverture immédiate. IDENTIFIE = pièces fournies.

INSERT INTO identity_profile (id, tenant_id, code, libelle, description, audience, active, kind)
VALUES
    ('f1000001-0000-4000-8000-000000000202', 'CORIS_DEMO', 'RETAIL-LIMITE-COMPTE',
     'Retail — compte ouvert', 'Plafonds d''un compte ouvert sans pièce d''identité', 'RETAIL', TRUE, 'LIMIT'),
    ('f1000001-0000-4000-8000-000000000203', 'CORIS_DEMO', 'RETAIL-LIMITE-IDENTIFIE',
     'Retail — identifié', 'Plafonds d''un compte dont l''identité est vérifiée', 'RETAIL', TRUE, 'LIMIT')
ON CONFLICT (tenant_id, code) DO NOTHING;

INSERT INTO identity_profile_limit (id, profile_id, produit_code, sens, groupe_code)
VALUES
    ('f5000001-0000-4000-8000-000000000011', 'f1000001-0000-4000-8000-000000000202',
     'CASH_OUT', 'EMETTEUR', 'CASH_OUT_LEGER'),
    ('f5000001-0000-4000-8000-000000000012', 'f1000001-0000-4000-8000-000000000202',
     'CASH_IN', 'EMETTEUR', 'CASH_IN_LEGER'),
    ('f5000001-0000-4000-8000-000000000013', 'f1000001-0000-4000-8000-000000000202',
     'ACCOUNT_TRANSFER', 'DETENTION', 'ACCOUNT_TRANSFER_LEGER'),
    ('f5000001-0000-4000-8000-000000000021', 'f1000001-0000-4000-8000-000000000203',
     'CASH_OUT', 'EMETTEUR', 'CASH_OUT_STANDARD'),
    ('f5000001-0000-4000-8000-000000000022', 'f1000001-0000-4000-8000-000000000203',
     'CASH_IN', 'EMETTEUR', 'CASH_IN_STANDARD'),
    ('f5000001-0000-4000-8000-000000000023', 'f1000001-0000-4000-8000-000000000203',
     'ACCOUNT_TRANSFER', 'DETENTION', 'ACCOUNT_TRANSFER_CREDIT')
ON CONFLICT (profile_id, produit_code, sens) DO NOTHING;
