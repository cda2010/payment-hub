-- Compte agent Omnicanal 000000104 pour l'UO volume SD-VOL001-5-1 (payment-network).
-- owner_id = UUID live de SD-VOL001-5-1 dans network-postgres local (import volume).
-- Si l'UUID change après re-import réseau, réaligner owner_id ou recréer l'alias.

INSERT INTO account_alias (
    id, tenant_id, alias_code, label, alias_type, external_ref, currency,
    owner_kind, owner_id, provider_code, active
)
VALUES (
    'c1000001-0000-4000-8000-000000000104',
    'CORIS_DEMO',
    'ALIAS-SD-VOL001-5-1',
    'Compte agent SD-VOL001-5-1',
    'WALLET',
    '000000104',
    'XOF',
    'UNITE',
    'f7e4d415-a9bb-420f-96a5-3d50fab942cd',
    'CORIS_MONEY',
    TRUE
)
ON CONFLICT (tenant_id, alias_code) DO UPDATE
SET label         = EXCLUDED.label,
    alias_type    = EXCLUDED.alias_type,
    external_ref  = EXCLUDED.external_ref,
    currency      = EXCLUDED.currency,
    owner_kind    = EXCLUDED.owner_kind,
    owner_id      = EXCLUDED.owner_id,
    provider_code = EXCLUDED.provider_code,
    active        = TRUE,
    updated_at    = NOW();
