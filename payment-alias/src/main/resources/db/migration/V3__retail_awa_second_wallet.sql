-- Second wallet PERS-RETAIL1 (awa).
INSERT INTO account_alias (
    id, tenant_id, alias_code, label, alias_type, external_ref, currency, owner_kind, owner_id, provider_code, active
)
VALUES (
    'c1000001-0000-4000-8000-000000000008',
    'CORIS_DEMO',
    'ALIAS-RETAIL-AWA-2',
    'Wallet Awa 2',
    'WALLET',
    '0011000000001',
    'XOF',
    'PARTY',
    'a1000001-0000-4000-8000-000000000006',
    'CORIS_MONEY',
    TRUE
)
ON CONFLICT (tenant_id, alias_code) DO UPDATE
SET external_ref = EXCLUDED.external_ref,
    label = EXCLUDED.label,
    active = TRUE,
    updated_at = NOW();
