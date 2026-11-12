-- Titulaires des deux comptes Coris Money utilisés par ACCOUNT_TRANSFER.
-- Le nom affiché est un rôle, pas une identité civile.

INSERT INTO identity_party (id, tenant_id, code, kind, display_name, external_ref, active)
VALUES
    ('a1000001-0000-4000-8000-000000000309', 'CORIS_DEMO', 'PERS-CM-SRC', 'PERSON',
     'Compte source Coris Money', '0011000003090', TRUE),
    ('a1000001-0000-4000-8000-000000159031', 'CORIS_DEMO', 'PERS-CM-DST', 'PERSON',
     'Compte destinataire Coris Money', '0011100119801590311', TRUE)
ON CONFLICT (tenant_id, code) DO UPDATE
SET display_name = EXCLUDED.display_name,
    external_ref = EXCLUDED.external_ref,
    active = TRUE,
    updated_at = NOW();
