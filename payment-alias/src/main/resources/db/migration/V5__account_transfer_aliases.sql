-- Alias WALLET des deux comptes Coris Money (FSE 0011) pour ACCOUNT_TRANSFER.
-- external_ref = numéro de compte. owner_id = party du client retail connecté (PERS-RETAIL1 / awa).

INSERT INTO account_alias (
    id, tenant_id, alias_code, label, alias_type, external_ref, currency,
    owner_kind, owner_id, provider_code, active
)
VALUES
    ('c1000001-0000-4000-8000-000000000309', 'CORIS_DEMO', 'ALIAS-CM-0011000003090',
     'Compte source Coris Money', 'WALLET', '0011000003090', 'XOF',
     'PARTY', 'a1000001-0000-4000-8000-000000000006', 'CORIS_MONEY', TRUE),
    ('c1000001-0000-4000-8000-000000159031', 'CORIS_DEMO', 'ALIAS-CM-0011100119801590311',
     'Compte destinataire Coris Money', 'WALLET', '0011100119801590311', 'XOF',
     'PARTY', 'a1000001-0000-4000-8000-000000000006', 'CORIS_MONEY', TRUE)
ON CONFLICT (tenant_id, alias_code) DO UPDATE
SET label = EXCLUDED.label,
    alias_type = EXCLUDED.alias_type,
    external_ref = EXCLUDED.external_ref,
    currency = EXCLUDED.currency,
    owner_kind = EXCLUDED.owner_kind,
    owner_id = EXCLUDED.owner_id,
    provider_code = EXCLUDED.provider_code,
    active = TRUE,
    updated_at = NOW();
