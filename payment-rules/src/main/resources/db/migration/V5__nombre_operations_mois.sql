-- Nombre d'opérations du mois, pour la personne désignée par l'étape du parcours.
-- Valeur opérationnelle (100), modifiable dans l'écran Règles.
-- Cash-in : le client qui recharge. Transfert : le bénéficiaire, car l'étape CHECK_LIMITS est DESTINATION.

INSERT INTO rule_definition (id, code, libelle, description, category, status, legal_ref)
VALUES
    ('c1000001-0000-4000-8000-000000000003', 'NOMBRE_OPERATIONS_MOIS',
     'Nombre d''opérations du mois',
     '100 opérations au plus sur le mois, pour la personne de l''étape (SOURCE ou DESTINATION). Valeur opérationnelle, modifiable.',
     'LIMIT', 'PUBLIEE', 'Plafond opérationnel')
ON CONFLICT (code) DO NOTHING;

INSERT INTO rule_version (id, rule_id, version, priority, conditions_json, source_type, source_period,
                          source_scope, include_current, operator, limit_value, limit_values_json,
                          action_on_violation, message, mandatory, effective_from, status, published_at)
VALUES
    ('c2000001-0000-4000-8000-000000000003', 'c1000001-0000-4000-8000-000000000003', 1, 55,
     '{"transactionTypes":["CASH_IN","ACCOUNT_TRANSFER"],"kycLevels":["STANDARD","ENHANCED","FULL"],"excludeSubjectRoles":["DISTRIBUTEUR","ACCEPTEUR","DISTRIBUTOR","ACCEPTOR"]}',
     'CUMUL_COUNT', 'MONTH', 'CUSTOMER', true,
     'LTE', 100, NULL, 'REJECT',
     'Nombre d''opérations du mois dépassé : 100',
     true, DATE '2026-11-02', 'PUBLIEE', NOW())
ON CONFLICT (id) DO NOTHING;

INSERT INTO rule_group_item (id, group_id, rule_version_id, sort_order) VALUES
    ('c4000001-0000-4000-8000-000000000004', 'b3000001-0000-4000-8000-000000000001',
     'c2000001-0000-4000-8000-000000000003', 55),
    ('c4000001-0000-4000-8000-000000000005', 'c3000001-0000-4000-8000-000000000001',
     'c2000001-0000-4000-8000-000000000003', 20)
ON CONFLICT (id) DO NOTHING;

INSERT INTO limit_definition (id, code, libelle, period, scope, metric, limit_value, currency, transaction_type, kyc_level)
VALUES
    ('c5000001-0000-4000-8000-000000000003', 'NOMBRE_OPERATIONS_MOIS',
     'Nombre d''opérations du mois (100)', 'MONTH', 'CUSTOMER', 'COUNT', 100, 'XOF', 'CASH_IN', 'STANDARD')
ON CONFLICT (code) DO NOTHING;
