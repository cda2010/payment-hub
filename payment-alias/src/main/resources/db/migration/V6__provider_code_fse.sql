-- Le casting produit identifie le réseau par son code FSE omnicanal.
-- 0011 = Coris Money, 0004 = Coris Banque.
UPDATE account_alias
SET provider_code = '0011',
    updated_at = NOW()
WHERE upper(provider_code) = 'CORIS_MONEY';

UPDATE account_alias
SET provider_code = '0004',
    updated_at = NOW()
WHERE upper(provider_code) = 'CORIS_BANK';
