-- Seed schémas TRANSFERT same_fse / inter_fse (idempotent via ON CONFLICT)

INSERT INTO schema_ecriture (id, nom_schema, service_type, pattern_code, workflow_name, workflow_version, engine, actif, created_at)
VALUES
    ('a0000001-0000-4000-8000-000000000001', 'Transfert même FSE', 'TRANSFERT', 'SAME_FSE_ANY', 'same_fse', 1, 'flowable', TRUE, NOW()),
    ('a0000001-0000-4000-8000-000000000002', 'Transfert inter-FSE', 'TRANSFERT', 'DIFF_FSE_ANY', 'inter_fse', 1, 'flowable', TRUE, NOW())
ON CONFLICT (service_type, pattern_code) DO NOTHING;

INSERT INTO ligne_schema_ecriture (
    id, schema_ecriture_id, ordre, step_id, type_methode, api_a_utiliser, agence_imputer,
    execution_mode, timeout_policy, timeout_duration, idempotency_policy, compensation_policy, blocking, criticality, created_at
)
VALUES
    ('b0000001-0000-4000-8000-000000000001', 'a0000001-0000-4000-8000-000000000001', 1,
     'transfertSource', 'TRANSFERT', 'FSESOURCE', 'AGENCESOURCE',
     'SYNC', 'FINANCIAL_15S', 'FINANCIAL_15S', 'KEY_REFERENCE', 'AUTO_REVERSE', TRUE, 'CRITICAL', NOW()),
    ('b0000001-0000-4000-8000-000000000002', 'a0000001-0000-4000-8000-000000000002', 1,
     'transfertSource', 'DEBIT', 'FSESOURCE', 'AGENCESOURCE',
     'SYNC', 'FINANCIAL_15S', 'FINANCIAL_15S', 'KEY_REFERENCE', 'AUTO_REVERSE', TRUE, 'CRITICAL', NOW()),
    ('b0000001-0000-4000-8000-000000000003', 'a0000001-0000-4000-8000-000000000002', 2,
     'transfertDest', 'CREDIT', 'FSEDESTINATAIRE', 'AGENCEDEST',
     'SYNC', 'FINANCIAL_15S', 'FINANCIAL_15S', 'KEY_REFERENCE', 'NONE', TRUE, 'CRITICAL', NOW())
ON CONFLICT (schema_ecriture_id, ordre) DO NOTHING;

INSERT INTO parametre_schema (id, ligne_schema_ecriture_id, code_parametre, code_parametre_methode, categorie, created_at)
VALUES
    ('c0000001-0000-4000-8000-000000000001', 'b0000001-0000-4000-8000-000000000001', 'compteDebiteur', 'compteDebiteur', 'USER', NOW()),
    ('c0000001-0000-4000-8000-000000000002', 'b0000001-0000-4000-8000-000000000001', 'compteCredit', 'compteCredit', 'USER', NOW()),
    ('c0000001-0000-4000-8000-000000000003', 'b0000001-0000-4000-8000-000000000001', 'montant', 'montant', 'USER', NOW()),
    ('c0000001-0000-4000-8000-000000000004', 'b0000001-0000-4000-8000-000000000002', 'compteDebiteur', 'compteDebiteur', 'USER', NOW()),
    ('c0000001-0000-4000-8000-000000000005', 'b0000001-0000-4000-8000-000000000003', 'compteCredit', 'compteCredit', 'USER', NOW()),
    ('c0000001-0000-4000-8000-000000000006', 'b0000001-0000-4000-8000-000000000002', 'montant', 'montant', 'USER', NOW()),
    ('c0000001-0000-4000-8000-000000000007', 'b0000001-0000-4000-8000-000000000003', 'montant', 'montant', 'USER', NOW());
