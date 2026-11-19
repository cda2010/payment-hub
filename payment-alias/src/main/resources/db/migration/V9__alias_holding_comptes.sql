-- Comptes du holding statique qui n'étaient pas encore des alias.
-- Propriétaire = nœud Identity (ORG). Les soldes ne sont pas portés par cette table.

UPDATE account_alias SET label = 'Compte courant', updated_at = NOW()
WHERE tenant_id = 'CORIS_DEMO' AND alias_code IN ('ALIAS-BF001-C01', 'ALIAS-ML001-C01', 'ALIAS-CI001-C01');

UPDATE account_alias SET label = 'Compte salaires', updated_at = NOW()
WHERE tenant_id = 'CORIS_DEMO' AND alias_code = 'ALIAS-BF001-C02';

INSERT INTO account_alias (
    id, tenant_id, alias_code, label, alias_type, external_ref, currency,
    owner_kind, owner_id, provider_code, active
)
VALUES
    ('c1000001-0000-4000-8000-000000000011', 'CORIS_DEMO', 'ALIAS-H001-C02',
     'Compte trésorerie groupe', 'IBAN', 'BF12 CORI 0000 0001 0001 0002', 'EUR',
     'ORG', 'b1000001-0000-4000-8000-000000000001', 'CORIS_BANK', TRUE),
    ('c1000001-0000-4000-8000-000000000012', 'CORIS_DEMO', 'ALIAS-BF001-C03',
     'Compte fournisseurs', 'IBAN', 'BF12 CORI 0001 0001 0001 0003', 'XOF',
     'ORG', 'b1000001-0000-4000-8000-000000000002', 'CORIS_BANK', TRUE),
    ('c1000001-0000-4000-8000-000000000013', 'CORIS_DEMO', 'ALIAS-BF001-C04',
     'Compte fiscalité', 'IBAN', 'BF12 CORI 0001 0001 0001 0004', 'XOF',
     'ORG', 'b1000001-0000-4000-8000-000000000002', 'CORIS_BANK', TRUE),
    ('c1000001-0000-4000-8000-000000000014', 'CORIS_DEMO', 'ALIAS-ML001-C02',
     'Compte opérations', 'IBAN', 'ML12 CORI 0002 0001 0001 0002', 'XOF',
     'ORG', 'b1000001-0000-4000-8000-000000000003', 'CORIS_BANK', TRUE),
    ('c1000001-0000-4000-8000-000000000015', 'CORIS_DEMO', 'ALIAS-ML001-C03',
     'Compte devises EUR', 'IBAN', 'ML12 CORI 0002 0001 0001 0003', 'EUR',
     'ORG', 'b1000001-0000-4000-8000-000000000003', 'CORIS_BANK', TRUE),
    ('c1000001-0000-4000-8000-000000000016', 'CORIS_DEMO', 'ALIAS-CI001-C02',
     'Compte opérations', 'IBAN', 'CI93 CORI 0003 0001 0001 0002', 'XOF',
     'ORG', 'b1000001-0000-4000-8000-000000000004', 'CORIS_BANK', TRUE)
ON CONFLICT (tenant_id, alias_code) DO NOTHING;
