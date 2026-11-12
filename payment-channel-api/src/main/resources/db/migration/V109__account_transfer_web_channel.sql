-- Canal WEB pour les virements corporate (portail web-app-corporate).
-- ACCOUNT_TRANSFER / RTGS / SWIFT : mêmes parcours que MOBILE, exposés sur WEB.

UPDATE catalog_de_service_version v
SET configuration = jsonb_set(
        COALESCE(v.configuration, '{}'::jsonb),
        '{supportedChannels}',
        (
            SELECT COALESCE(jsonb_agg(DISTINCT val ORDER BY val), '[]'::jsonb)
            FROM (
                     SELECT jsonb_array_elements_text(
                                    CASE
                                        WHEN jsonb_typeof(v.configuration -> 'supportedChannels') = 'array'
                                            AND jsonb_array_length(v.configuration -> 'supportedChannels') > 0
                                            THEN v.configuration -> 'supportedChannels'
                                        ELSE '["MOBILE", "GAB", "USSD", "BACKOFFICE"]'::jsonb
                                        END
                            ) AS val
                     UNION
                     SELECT 'WEB'
                 ) channels
        )
     ),
    updated_at = NOW()
FROM catalog_de_service s
WHERE v.catalog_de_service_id = s.id
  AND s.code IN ('ACCOUNT_TRANSFER', 'RTGS', 'SWIFT')
  AND v.status = 'PUBLISHED'
  AND NOT (
      jsonb_typeof(v.configuration -> 'supportedChannels') = 'array'
          AND (v.configuration -> 'supportedChannels') ? 'WEB'
      );
