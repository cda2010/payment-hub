-- Grille commission CASH_IN (répartition frais — même clé CDC que cash-out, produit distinct).

INSERT INTO commission_grille (id, code, version, libelle, description, operation_type, product_code,
                               channel_code, currency, status, envelope_mode)
VALUES
    ('c3000001-0000-4000-8000-000000000002', 'CASH_IN-STD', 1,
     'Cash-in standard — répartition frais',
     'Répartition 40/25/20/10/5 % sur les frais transaction (dépôt agent)',
     'CASH_IN', 'CASH_IN', 'AGENT', 'XOF', 'PUBLIEE', 'FULL_FEES')
ON CONFLICT (code, version) DO NOTHING;

INSERT INTO commission_regle (id, grille_id, code, libelle, beneficiary_role, formule_id,
                              commission_base, funding_type, params_json, priority)
VALUES
    ('c4000001-0000-4000-8000-000000000011', 'c3000001-0000-4000-8000-000000000002',
     'EME', 'Part émetteur', 'ISSUER',
     'c1000001-0000-4000-8000-000000000002', 'TRANSACTION_FEES', 'TRANSACTION_FEES',
     '{"taux":0.40}', 10),
    ('c4000001-0000-4000-8000-000000000012', 'c3000001-0000-4000-8000-000000000002',
     'DIST', 'Part distributeur principal', 'DISTRIBUTOR',
     'c1000001-0000-4000-8000-000000000002', 'TRANSACTION_FEES', 'TRANSACTION_FEES',
     '{"taux":0.25}', 20),
    ('c4000001-0000-4000-8000-000000000013', 'c3000001-0000-4000-8000-000000000002',
     'AGENT', 'Part agent', 'AGENT',
     'c1000001-0000-4000-8000-000000000002', 'TRANSACTION_FEES', 'TRANSACTION_FEES',
     '{"taux":0.20}', 30),
    ('c4000001-0000-4000-8000-000000000014', 'c3000001-0000-4000-8000-000000000002',
     'AGG', 'Part agrégateur', 'AGGREGATOR',
     'c1000001-0000-4000-8000-000000000002', 'TRANSACTION_FEES', 'TRANSACTION_FEES',
     '{"taux":0.10}', 40),
    ('c4000001-0000-4000-8000-000000000015', 'c3000001-0000-4000-8000-000000000002',
     'PROC', 'Part processeur', 'PROCESSOR',
     'c1000001-0000-4000-8000-000000000002', 'TRANSACTION_FEES', 'TRANSACTION_FEES',
     '{"taux":0.05}', 50)
ON CONFLICT (grille_id, code) DO NOTHING;
