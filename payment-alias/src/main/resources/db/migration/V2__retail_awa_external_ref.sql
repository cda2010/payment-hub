-- PERS-RETAIL1 (ALIAS-RETAIL-AWA) : référence wallet Coris Money réelle.
UPDATE account_alias
SET external_ref = '00111000002612',
    updated_at = NOW()
WHERE tenant_id = 'CORIS_DEMO'
  AND alias_code = 'ALIAS-RETAIL-AWA';
