-- Historique + notifications (i18n) pour ACCOUNT_TRANSFER et BILL_PAYMENT.

-- ── ACCOUNT_TRANSFER ────────────────────────────────────────────────────────
UPDATE catalog_de_service_version v
SET configuration = COALESCE(v.configuration, '{}'::jsonb) || jsonb_build_object(
        'history', jsonb_build_object(
                'kindLabel', 'Transfert',
                'titleTemplate', '{{kind}} · {{party}} · {{channel}}',
                'subtitleTemplate', '{{statusLabel}} · {{when}}'
        ),
        'notifications', jsonb_build_array(
                jsonb_build_object(
                        'code', 'SUCCESS',
                        'channelHint', 'SMS',
                        'body',
                        'Votre transfert de {{amount}} {{currency}} vers {{beneficiaryName}} est confirmé. Réf. {{paymentId}}'
                ),
                jsonb_build_object(
                        'code', 'WAITING_APPROVAL',
                        'channelHint', 'SMS',
                        'body',
                        'Votre transfert de {{amount}} {{currency}} vers {{beneficiaryName}} est en attente de validation. Réf. {{paymentId}}'
                )
        )
     )
FROM catalog_de_service c
WHERE v.catalog_de_service_id = c.id
  AND c.code = 'ACCOUNT_TRANSFER';

-- ── BILL_PAYMENT ────────────────────────────────────────────────────────────
UPDATE catalog_de_service_version v
SET configuration = COALESCE(v.configuration, '{}'::jsonb) || jsonb_build_object(
        'history', jsonb_build_object(
                'kindLabel', 'Paiement de facture',
                'titleTemplate', '{{kind}} · {{party}} · {{ref}}',
                'subtitleTemplate', '{{statusLabel}} · {{when}}'
        ),
        'notifications', jsonb_build_array(
                jsonb_build_object(
                        'code', 'SUCCESS',
                        'channelHint', 'SMS',
                        'body',
                        'Votre paiement de {{amount}} {{currency}} à {{billerName}} (réf. {{billReference}}) est confirmé. Réf. {{paymentId}}'
                ),
                jsonb_build_object(
                        'code', 'WAITING_APPROVAL',
                        'channelHint', 'SMS',
                        'body',
                        'Votre paiement de {{amount}} {{currency}} à {{billerName}} est en attente de validation. Réf. {{paymentId}}'
                )
        )
     )
FROM catalog_de_service c
WHERE v.catalog_de_service_id = c.id
  AND c.code = 'BILL_PAYMENT';

-- ── i18n FR (amorçage depuis configuration) ─────────────────────────────────
INSERT INTO i18n_translation (id, entity_type, entity_id, field_path, lang, text_value, created_at, updated_at)
SELECT gen_random_uuid(), 'CATALOG_DE_SERVICE_VERSION', v.id,
       'configuration.history.' || k, 'fr', v.configuration->'history'->>k, NOW(), NOW()
FROM catalog_de_service_version v
         JOIN catalog_de_service c ON c.id = v.catalog_de_service_id,
     (VALUES ('kindLabel'), ('titleTemplate'), ('subtitleTemplate')) AS keys(k)
WHERE c.code IN ('ACCOUNT_TRANSFER', 'BILL_PAYMENT')
  AND jsonb_typeof(v.configuration->'history') = 'object'
  AND v.configuration->'history'->>k IS NOT NULL
ON CONFLICT DO NOTHING;

INSERT INTO i18n_translation (id, entity_type, entity_id, field_path, lang, text_value, created_at, updated_at)
SELECT gen_random_uuid(), 'CATALOG_DE_SERVICE_VERSION', v.id,
       'configuration.notifications[' || (n->>'code') || '].body', 'fr', n->>'body', NOW(), NOW()
FROM catalog_de_service_version v
         JOIN catalog_de_service c ON c.id = v.catalog_de_service_id,
     jsonb_array_elements(v.configuration->'notifications') n
WHERE c.code IN ('ACCOUNT_TRANSFER', 'BILL_PAYMENT')
  AND jsonb_typeof(v.configuration->'notifications') = 'array'
  AND n->>'code' IS NOT NULL
  AND n->>'body' IS NOT NULL
ON CONFLICT DO NOTHING;

-- ── i18n EN — ACCOUNT_TRANSFER ──────────────────────────────────────────────
INSERT INTO i18n_translation (id, entity_type, entity_id, field_path, lang, text_value, created_at, updated_at)
SELECT gen_random_uuid(), 'CATALOG_DE_SERVICE_VERSION', v.id, path, 'en', text_value, NOW(), NOW()
FROM catalog_de_service_version v
         JOIN catalog_de_service c ON c.id = v.catalog_de_service_id
         CROSS JOIN (VALUES
                             ('configuration.history.kindLabel', 'Transfer'),
                             ('configuration.history.titleTemplate', '{{kind}} · {{party}} · {{channel}}'),
                             ('configuration.history.subtitleTemplate', '{{statusLabel}} · {{when}}'),
                             ('configuration.notifications[SUCCESS].body',
                              'Your transfer of {{amount}} {{currency}} to {{beneficiaryName}} is confirmed. Ref. {{paymentId}}'),
                             ('configuration.notifications[WAITING_APPROVAL].body',
                              'Your transfer of {{amount}} {{currency}} to {{beneficiaryName}} is awaiting approval. Ref. {{paymentId}}')
     ) AS t(path, text_value)
WHERE c.code = 'ACCOUNT_TRANSFER'
ON CONFLICT DO NOTHING;

-- ── i18n EN — BILL_PAYMENT ──────────────────────────────────────────────────
INSERT INTO i18n_translation (id, entity_type, entity_id, field_path, lang, text_value, created_at, updated_at)
SELECT gen_random_uuid(), 'CATALOG_DE_SERVICE_VERSION', v.id, path, 'en', text_value, NOW(), NOW()
FROM catalog_de_service_version v
         JOIN catalog_de_service c ON c.id = v.catalog_de_service_id
         CROSS JOIN (VALUES
                             ('configuration.history.kindLabel', 'Bill payment'),
                             ('configuration.history.titleTemplate', '{{kind}} · {{party}} · {{ref}}'),
                             ('configuration.history.subtitleTemplate', '{{statusLabel}} · {{when}}'),
                             ('configuration.notifications[SUCCESS].body',
                              'Your payment of {{amount}} {{currency}} to {{billerName}} (ref. {{billReference}}) is confirmed. Ref. {{paymentId}}'),
                             ('configuration.notifications[WAITING_APPROVAL].body',
                              'Your payment of {{amount}} {{currency}} to {{billerName}} is awaiting approval. Ref. {{paymentId}}')
     ) AS t(path, text_value)
WHERE c.code = 'BILL_PAYMENT'
ON CONFLICT DO NOTHING;
