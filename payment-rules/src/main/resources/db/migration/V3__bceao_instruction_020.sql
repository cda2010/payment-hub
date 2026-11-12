-- Instruction BCEAO n°020 du 8 octobre 2026, en vigueur le 2 novembre 2026.
-- Art. 31 : client identifié, même émetteur, tous instruments confondus.
--   détention  <= 4 000 000 XOF
--   rechargements du mois <= 15 000 000 XOF
-- Ne s'applique pas aux distributeurs ni aux accepteurs agissant à titre professionnel.
-- Le solde transmis au contrôle de détention doit être le total du client auprès de l'émetteur.

INSERT INTO rule_definition (id, code, libelle, description, category, status, legal_ref, legal_text_no, legal_effective)
VALUES
    ('c1000001-0000-4000-8000-000000000001', 'BCEAO_EME_SOLDE_MAX',
     'Détention maximale de monnaie électronique',
     'Client identifié : 4 000 000 XOF auprès du même émetteur, tous instruments confondus. Instruction BCEAO n°020, art. 31.',
     'LIMIT', 'PUBLIEE',
     'Instruction BCEAO n°020 du 08/10/2026, art. 31', '020', DATE '2026-11-02'),
    ('c1000001-0000-4000-8000-000000000002', 'BCEAO_EME_RECHARGEMENT_MOIS',
     'Rechargement mensuel de monnaie électronique',
     'Client identifié : 15 000 000 XOF de rechargements sur le mois, auprès du même émetteur. Instruction BCEAO n°020, art. 31.',
     'LIMIT', 'PUBLIEE',
     'Instruction BCEAO n°020 du 08/10/2026, art. 31', '020', DATE '2026-11-02')
ON CONFLICT (code) DO NOTHING;

INSERT INTO rule_version (id, rule_id, version, priority, conditions_json, source_type, source_period,
                          source_scope, include_current, operator, limit_value, limit_values_json,
                          action_on_violation, message, mandatory, effective_from, status, published_at)
VALUES
    ('c2000001-0000-4000-8000-000000000001', 'c1000001-0000-4000-8000-000000000001', 1, 45,
     '{"transactionType":"CASH_IN","kycLevels":["STANDARD","ENHANCED","FULL"],"excludeSubjectRoles":["DISTRIBUTEUR","ACCEPTEUR","DISTRIBUTOR","ACCEPTOR"]}',
     'PROJECTED_HOLDING', NULL, 'CUSTOMER', true,
     'LTE', 4000000, NULL, 'REJECT',
     'Plafond de détention dépassé : 4 000 000 XOF (instruction BCEAO n°020)',
     true, DATE '2026-11-02', 'PUBLIEE', NOW()),
    ('c2000001-0000-4000-8000-000000000002', 'c1000001-0000-4000-8000-000000000002', 1, 50,
     '{"transactionType":"CASH_IN","kycLevels":["STANDARD","ENHANCED","FULL"],"excludeSubjectRoles":["DISTRIBUTEUR","ACCEPTEUR","DISTRIBUTOR","ACCEPTOR"]}',
     'CUMUL_AMOUNT', 'MONTH', 'CUSTOMER', true,
     'LTE', 15000000, NULL, 'REJECT',
     'Plafond mensuel de rechargement dépassé : 15 000 000 XOF (instruction BCEAO n°020)',
     true, DATE '2026-11-02', 'PUBLIEE', NOW())
ON CONFLICT (id) DO NOTHING;

INSERT INTO rule_group_item (id, group_id, rule_version_id, sort_order) VALUES
    ('c4000001-0000-4000-8000-000000000001', 'b3000001-0000-4000-8000-000000000001',
     'c2000001-0000-4000-8000-000000000001', 45),
    ('c4000001-0000-4000-8000-000000000002', 'b3000001-0000-4000-8000-000000000001',
     'c2000001-0000-4000-8000-000000000002', 50)
ON CONFLICT (id) DO NOTHING;

INSERT INTO limit_definition (id, code, libelle, period, scope, metric, limit_value, currency, transaction_type, kyc_level)
VALUES
    ('c5000001-0000-4000-8000-000000000001', 'BCEAO_EME_SOLDE_MAX',
     'Détention maximale BCEAO (4 000 000 XOF)', 'CUSTOM', 'CUSTOMER', 'AMOUNT', 4000000, 'XOF', 'CASH_IN', 'STANDARD'),
    ('c5000001-0000-4000-8000-000000000002', 'BCEAO_EME_RECHARGEMENT_MOIS',
     'Rechargement mensuel BCEAO (15 000 000 XOF)', 'MONTH', 'CUSTOMER', 'AMOUNT', 15000000, 'XOF', 'CASH_IN', 'STANDARD')
ON CONFLICT (code) DO NOTHING;
