-- Groupe CASH_IN_STANDARD : plafonds dépôt (valeurs fictives — à valider Conformité).
-- Pas de CHECK_WALLET_BALANCE : le cash-in crédite, il ne débite pas.

INSERT INTO rule_definition (id, code, libelle, description, category, status, legal_ref)
VALUES
    ('b1000001-0000-4000-8000-000000000001', 'CASHIN_CUSTOMER_STATUS',
     'Client actif (cash-in)', 'Le client doit être ACTIVE', 'RESTRICTION', 'PUBLIEE', NULL),
    ('b1000001-0000-4000-8000-000000000002', 'CASHIN_KYC_LEVEL',
     'Niveau KYC cash-in', 'KYC STANDARD ou supérieur pour dépôt', 'KYC', 'PUBLIEE',
     'Instruction fictive — à valider Conformité'),
    ('b1000001-0000-4000-8000-000000000003', 'CASHIN_TX_LIMIT',
     'Montant max par dépôt', 'Plafond unitaire cash-in', 'LIMIT', 'PUBLIEE',
     'Instruction fictive — à valider Conformité'),
    ('b1000001-0000-4000-8000-000000000004', 'CASHIN_DAILY_LIMIT',
     'Plafond journalier des dépôts', 'Cumul journalier wallet (valeur fictive 1 000 000)', 'LIMIT', 'PUBLIEE',
     'Instruction fictive — à valider Conformité')
ON CONFLICT (code) DO NOTHING;

INSERT INTO rule_version (id, rule_id, version, priority, conditions_json, source_type, source_period,
                          source_scope, include_current, operator, limit_value, limit_values_json,
                          action_on_violation, message, mandatory, status, published_at)
VALUES
    ('b2000001-0000-4000-8000-000000000001', 'b1000001-0000-4000-8000-000000000001', 1, 10,
     '{"transactionType":"CASH_IN"}', 'CUSTOMER_STATUS', NULL, NULL, false,
     'EQ', NULL, '["ACTIVE"]', 'REJECT', 'Client non actif', true, 'PUBLIEE', NOW()),
    ('b2000001-0000-4000-8000-000000000002', 'b1000001-0000-4000-8000-000000000002', 1, 20,
     '{"transactionType":"CASH_IN"}', 'KYC_LEVEL', NULL, NULL, false,
     'IN', NULL, '["STANDARD","ENHANCED","FULL"]', 'REJECT', 'Niveau KYC insuffisant', true, 'PUBLIEE', NOW()),
    ('b2000001-0000-4000-8000-000000000003', 'b1000001-0000-4000-8000-000000000003', 1, 30,
     '{"transactionType":"CASH_IN","kycLevel":"STANDARD"}', 'TRANSACTION_AMOUNT', NULL, NULL, true,
     'LTE', 500000, NULL, 'REJECT', 'Montant supérieur au plafond unitaire dépôt', true, 'PUBLIEE', NOW()),
    ('b2000001-0000-4000-8000-000000000004', 'b1000001-0000-4000-8000-000000000004', 1, 40,
     '{"transactionType":"CASH_IN","kycLevel":"STANDARD"}', 'CUMUL_AMOUNT', 'DAY', 'WALLET', true,
     'LTE', 1000000, NULL, 'REJECT', 'Plafond journalier dépôts dépassé', true, 'PUBLIEE', NOW())
ON CONFLICT (id) DO NOTHING;

INSERT INTO rule_group (id, code, libelle, description, transaction_type, product_code, evaluation_strategy, status)
VALUES ('b3000001-0000-4000-8000-000000000001', 'CASH_IN_STANDARD',
        'Contrôles cash-in standard', 'Groupe live path CASH_IN — valeurs fictives',
        'CASH_IN', 'CASH_IN', 'STOP_ON_REJECT', 'ACTIVE')
ON CONFLICT (code) DO NOTHING;

INSERT INTO rule_group_item (id, group_id, rule_version_id, sort_order) VALUES
    ('b4000001-0000-4000-8000-000000000001', 'b3000001-0000-4000-8000-000000000001',
     'b2000001-0000-4000-8000-000000000001', 10),
    ('b4000001-0000-4000-8000-000000000002', 'b3000001-0000-4000-8000-000000000001',
     'b2000001-0000-4000-8000-000000000002', 20),
    ('b4000001-0000-4000-8000-000000000003', 'b3000001-0000-4000-8000-000000000001',
     'b2000001-0000-4000-8000-000000000003', 30),
    ('b4000001-0000-4000-8000-000000000004', 'b3000001-0000-4000-8000-000000000001',
     'b2000001-0000-4000-8000-000000000004', 40)
ON CONFLICT (id) DO NOTHING;

INSERT INTO limit_definition (id, code, libelle, period, scope, metric, limit_value, currency, transaction_type, kyc_level)
VALUES ('b5000001-0000-4000-8000-000000000001', 'CASHIN_DAILY_LIMIT',
        'Plafond journalier dépôts (fictif)', 'DAY', 'WALLET', 'AMOUNT', 1000000, 'XOF', 'CASH_IN', 'STANDARD')
ON CONFLICT (code) DO NOTHING;
