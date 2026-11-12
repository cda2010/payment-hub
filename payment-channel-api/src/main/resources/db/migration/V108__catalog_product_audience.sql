-- Product audience (segment client) — orthogonal à accessChannel (MOBILE/WEB/…).
-- Clés JSON : supportedAudiences, defaultAudience
-- Sans politique → visible pour toutes les audiences (compat).

-- Défaut RETAIL pour tous les produits PUBLISHED qui n'ont pas encore d'audience
UPDATE catalog_de_service_version v
SET configuration = configuration
    || jsonb_build_object(
           'supportedAudiences', jsonb_build_array('RETAIL'),
           'defaultAudience', 'RETAIL'
       )
WHERE v.status = 'PUBLISHED'
  AND (configuration -> 'supportedAudiences') IS NULL
  AND (configuration -> 'supported_audiences') IS NULL;

-- Produits dual RETAIL + CORPORATE (virements / factures / programmés / bénéficiaires)
UPDATE catalog_de_service_version v
SET configuration = configuration
    || jsonb_build_object(
           'supportedAudiences', jsonb_build_array('RETAIL', 'CORPORATE'),
           'defaultAudience', 'RETAIL'
       )
FROM catalog_de_service s
WHERE v.catalog_de_service_id = s.id
  AND v.status = 'PUBLISHED'
  AND upper(s.code) IN (
      'ACCOUNT_TRANSFER',
      'BILL_PAYMENT',
      'SCHEDULED_PAYMENT',
      'TRANSFER_BENEFICIARY',
      'BENEFICIARY',
      'CHECKOUT'
  );

-- Corporate-first (RTGS / SWIFT / statements-like)
UPDATE catalog_de_service_version v
SET configuration = configuration
    || jsonb_build_object(
           'supportedAudiences', jsonb_build_array('CORPORATE', 'RETAIL'),
           'defaultAudience', 'CORPORATE'
       )
FROM catalog_de_service s
WHERE v.catalog_de_service_id = s.id
  AND v.status = 'PUBLISHED'
  AND upper(s.code) IN ('RTGS', 'SWIFT');

-- Marchand
UPDATE catalog_de_service_version v
SET configuration = configuration
    || jsonb_build_object(
           'supportedAudiences', jsonb_build_array('MERCHANT', 'RETAIL'),
           'defaultAudience', 'MERCHANT'
       )
FROM catalog_de_service s
WHERE v.catalog_de_service_id = s.id
  AND v.status = 'PUBLISHED'
  AND upper(s.code) IN ('MARCHAND', 'MARKETPLACE');

-- Agent / cash réseau
UPDATE catalog_de_service_version v
SET configuration = configuration
    || jsonb_build_object(
           'supportedAudiences', jsonb_build_array('AGENT', 'RETAIL'),
           'defaultAudience', 'RETAIL'
       )
FROM catalog_de_service s
WHERE v.catalog_de_service_id = s.id
  AND v.status = 'PUBLISHED'
  AND upper(s.code) IN ('CASH_IN', 'CASH_OUT');
