-- Environnements standard : SANDBOX, DEVELOPPEMENT, PRODUCTION
-- Ne crée que les manquants ; DEVELOPPEMENT / PRODUCTION restent inactifs.

INSERT INTO provider_environment (id, provider_id, code, base_url, timeout_seconds, actif, created_at, updated_at)
SELECT gen_random_uuid(),
       p.id,
       'SANDBOX',
       COALESCE(
           (SELECT pe.base_url FROM provider_environment pe
            WHERE pe.provider_id = p.id
            ORDER BY pe.actif DESC, pe.code
            LIMIT 1),
           'https://example.invalid'
       ),
       30,
       NOT EXISTS (
           SELECT 1 FROM provider_environment pe
           WHERE pe.provider_id = p.id AND pe.actif = TRUE
       ),
       NOW(),
       NOW()
FROM payment_provider p
WHERE NOT EXISTS (
    SELECT 1 FROM provider_environment pe
    WHERE pe.provider_id = p.id AND upper(pe.code) IN ('SANDBOX')
);

INSERT INTO provider_environment (id, provider_id, code, base_url, timeout_seconds, actif, created_at, updated_at)
SELECT gen_random_uuid(),
       p.id,
       'DEVELOPPEMENT',
       COALESCE(
           (SELECT pe.base_url FROM provider_environment pe
            WHERE pe.provider_id = p.id AND upper(pe.code) = 'SANDBOX'
            LIMIT 1),
           (SELECT pe.base_url FROM provider_environment pe
            WHERE pe.provider_id = p.id
            ORDER BY pe.actif DESC, pe.code
            LIMIT 1),
           'https://example.invalid'
       ),
       COALESCE(
           (SELECT pe.timeout_seconds FROM provider_environment pe
            WHERE pe.provider_id = p.id AND upper(pe.code) = 'SANDBOX'
            LIMIT 1),
           30
       ),
       FALSE,
       NOW(),
       NOW()
FROM payment_provider p
WHERE NOT EXISTS (
    SELECT 1 FROM provider_environment pe
    WHERE pe.provider_id = p.id AND upper(pe.code) IN ('DEVELOPPEMENT', 'DEVELOPMENT', 'DEV')
);

INSERT INTO provider_environment (id, provider_id, code, base_url, timeout_seconds, actif, created_at, updated_at)
SELECT gen_random_uuid(),
       p.id,
       'PRODUCTION',
       COALESCE(
           (SELECT pe.base_url FROM provider_environment pe
            WHERE pe.provider_id = p.id AND upper(pe.code) = 'SANDBOX'
            LIMIT 1),
           (SELECT pe.base_url FROM provider_environment pe
            WHERE pe.provider_id = p.id
            ORDER BY pe.actif DESC, pe.code
            LIMIT 1),
           'https://example.invalid'
       ),
       COALESCE(
           (SELECT pe.timeout_seconds FROM provider_environment pe
            WHERE pe.provider_id = p.id AND upper(pe.code) = 'SANDBOX'
            LIMIT 1),
           30
       ),
       FALSE,
       NOW(),
       NOW()
FROM payment_provider p
WHERE NOT EXISTS (
    SELECT 1 FROM provider_environment pe
    WHERE pe.provider_id = p.id AND upper(pe.code) IN ('PRODUCTION', 'PROD')
);
