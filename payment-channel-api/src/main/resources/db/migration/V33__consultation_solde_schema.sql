-- Schéma d'écriture CONSULTATION_SOLDE (même FSE → CONSULTATIONSOLDE) — parité Omnicanal

INSERT INTO schema_ecriture (id, nom_schema, service_type, pattern_code, workflow_name, workflow_version, engine, actif, created_at)
VALUES
    ('a0000001-0000-4000-8000-000000000003', 'Consultation solde même FSE', 'CONSULTATION_SOLDE', 'SAME_FSE_ANY', 'providerBalanceInquiry', 1, 'flowable', TRUE, NOW())
ON CONFLICT (service_type, pattern_code) DO NOTHING;

INSERT INTO ligne_schema_ecriture (
    id, schema_ecriture_id, ordre, step_id, type_methode, api_a_utiliser, agence_imputer,
    execution_mode, timeout_policy, timeout_duration, idempotency_policy, compensation_policy, blocking, criticality, created_at
)
VALUES
    ('b0000001-0000-4000-8000-000000000010', 'a0000001-0000-4000-8000-000000000003', 1,
     'getBalance', 'CONSULTATIONSOLDE', 'FSESOURCE', 'AGENCESOURCE',
     'SYNC', 'STANDARD_5S', 'STANDARD_5S', 'NONE', 'NONE', TRUE, 'CRITICAL', NOW())
ON CONFLICT (schema_ecriture_id, ordre) DO NOTHING;

INSERT INTO parametre_schema (id, ligne_schema_ecriture_id, code_parametre, code_parametre_methode, categorie, created_at)
VALUES
    ('c0000001-0000-4000-8000-000000000010', 'b0000001-0000-4000-8000-000000000010', 'numeroCompte', 'CPTSOURCE', 'USER', NOW()),
    ('c0000001-0000-4000-8000-000000000011', 'b0000001-0000-4000-8000-000000000010', 'compteDebiteur', 'CPTSOURCE', 'USER', NOW()),
    ('c0000001-0000-4000-8000-000000000012', 'b0000001-0000-4000-8000-000000000010', 'accountId', 'CPTSOURCE', 'USER', NOW()),
    ('c0000001-0000-4000-8000-000000000013', 'b0000001-0000-4000-8000-000000000010', 'codeFse', 'codeFse', 'SYSTEM', NOW());
