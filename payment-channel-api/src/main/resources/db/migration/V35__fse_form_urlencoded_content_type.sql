-- Omnicanal / connecteurs FSE : virements en @RequestParam (form/query), pas JSON body
UPDATE provider_api a
SET content_type = 'application/x-www-form-urlencoded'
FROM payment_provider p
WHERE a.provider_id = p.id
  AND p.code IN ('CORIS_BANK', 'CORIS_MONEY')
  AND a.code IN ('TRANSFERT', 'DEBIT', 'CREDIT', 'CONSULTATIONSOLDE', 'CONSULTATION');
