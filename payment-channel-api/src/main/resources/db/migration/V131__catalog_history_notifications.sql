-- Libellés d'historique + templates de notification sur le produit CASH_OUT.
-- Les textes FR sont dans configuration ; les EN dans i18n_translation.

UPDATE catalog_de_service_version v
SET configuration = COALESCE(v.configuration, '{}'::jsonb) || jsonb_build_object(
        'history', jsonb_build_object(
                'kindLabel', 'Retrait d''espèces',
                'titleTemplate', '{{kind}} · via {{agentDisplay}}',
                'subtitleTemplate', '{{statusLabel}} · {{when}}'
        ),
        'notifications', jsonb_build_array(
                jsonb_build_object(
                        'code', 'SUCCESS',
                        'channelHint', 'SMS',
                        'body',
                        'Votre retrait de {{amount}} {{currency}} chez {{agentDisplay}} est confirmé. Réf. {{paymentId}}'
                ),
                jsonb_build_object(
                        'code', 'WAITING_CODE',
                        'channelHint', 'SMS',
                        'body',
                        'Code de retrait : {{withdrawalCode}}. Présentez-le à {{agentDisplay}}.'
                )
        )
     )
FROM catalog_de_service c
WHERE v.catalog_de_service_id = c.id
  AND c.code = 'CASH_OUT';

-- Amorçage i18n FR (langue par défaut) depuis la configuration.
INSERT INTO i18n_translation (id, entity_type, entity_id, field_path, lang, text_value, created_at, updated_at)
SELECT gen_random_uuid(), 'CATALOG_DE_SERVICE_VERSION', v.id,
       'configuration.history.' || k, 'fr', v.configuration->'history'->>k, NOW(), NOW()
FROM catalog_de_service_version v
         JOIN catalog_de_service c ON c.id = v.catalog_de_service_id,
     (VALUES ('kindLabel'), ('titleTemplate'), ('subtitleTemplate')) AS keys(k)
WHERE c.code = 'CASH_OUT'
  AND jsonb_typeof(v.configuration->'history') = 'object'
  AND v.configuration->'history'->>k IS NOT NULL
ON CONFLICT DO NOTHING;

INSERT INTO i18n_translation (id, entity_type, entity_id, field_path, lang, text_value, created_at, updated_at)
SELECT gen_random_uuid(), 'CATALOG_DE_SERVICE_VERSION', v.id,
       'configuration.notifications[' || (n->>'code') || '].body', 'fr', n->>'body', NOW(), NOW()
FROM catalog_de_service_version v
         JOIN catalog_de_service c ON c.id = v.catalog_de_service_id,
     jsonb_array_elements(v.configuration->'notifications') n
WHERE c.code = 'CASH_OUT'
  AND jsonb_typeof(v.configuration->'notifications') = 'array'
  AND n->>'code' IS NOT NULL
  AND n->>'body' IS NOT NULL
ON CONFLICT DO NOTHING;

-- Traductions EN.
INSERT INTO i18n_translation (id, entity_type, entity_id, field_path, lang, text_value, created_at, updated_at)
SELECT gen_random_uuid(), 'CATALOG_DE_SERVICE_VERSION', v.id, path, 'en', text_value, NOW(), NOW()
FROM catalog_de_service_version v
         JOIN catalog_de_service c ON c.id = v.catalog_de_service_id
         CROSS JOIN (VALUES
                             ('configuration.history.kindLabel', 'Cash withdrawal'),
                             ('configuration.history.titleTemplate', '{{kind}} · via {{agentDisplay}}'),
                             ('configuration.history.subtitleTemplate', '{{statusLabel}} · {{when}}'),
                             ('configuration.notifications[SUCCESS].body',
                              'Your withdrawal of {{amount}} {{currency}} at {{agentDisplay}} is confirmed. Ref. {{paymentId}}'),
                             ('configuration.notifications[WAITING_CODE].body',
                              'Withdrawal code: {{withdrawalCode}}. Present it at {{agentDisplay}}.')
     ) AS t(path, text_value)
WHERE c.code = 'CASH_OUT'
ON CONFLICT DO NOTHING;
