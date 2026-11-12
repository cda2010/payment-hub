-- Journey + AccessChannel sur CASH_OUT / CASH_IN (config catalogue)
-- PRODUCT → JOURNEY → CHANNEL (MOBILE, GAB, USSD, BACKOFFICE)

UPDATE catalog_de_service_version v
SET configuration = COALESCE(v.configuration, '{}'::jsonb)
    || jsonb_build_object(
        'journeyCode', 'STANDARD_CASH_OUT',
        'supportedChannels', jsonb_build_array('MOBILE', 'GAB', 'USSD', 'BACKOFFICE'),
        'defaultChannel', 'MOBILE',
        'channelDefaults', jsonb_build_object(
            'MOBILE', jsonb_build_object(),
            'GAB', jsonb_build_object(),
            'USSD', jsonb_build_object(
                'bindings', jsonb_build_array(
                    jsonb_build_object(
                        'step', 'CAPTURE_AMOUNT',
                        'kind', 'UI',
                        'formSource', 'FORM_KEY',
                        'formKey', 'cash-out-amount'
                    )
                )
            ),
            'BACKOFFICE', jsonb_build_object(
                'bindings', jsonb_build_array(
                    jsonb_build_object(
                        'step', 'SELECT_AGENT_CHANNEL',
                        'kind', 'UI',
                        'formSource', 'FORM_KEY',
                        'formKey', 'cash-out-agent'
                    )
                )
            )
        )
    ),
    updated_at = NOW()
FROM catalog_de_service s
WHERE v.catalog_de_service_id = s.id
  AND s.code = 'CASH_OUT'
  AND v.status = 'PUBLISHED';

UPDATE catalog_de_service_version v
SET configuration = COALESCE(v.configuration, '{}'::jsonb)
    || jsonb_build_object(
        'journeyCode', 'STANDARD_CASH_IN',
        'supportedChannels', jsonb_build_array('MOBILE', 'GAB', 'USSD', 'BACKOFFICE'),
        'defaultChannel', 'MOBILE',
        'channelDefaults', jsonb_build_object(
            'MOBILE', jsonb_build_object(),
            'GAB', jsonb_build_object(),
            'USSD', jsonb_build_object(),
            'BACKOFFICE', jsonb_build_object()
        )
    ),
    updated_at = NOW()
FROM catalog_de_service s
WHERE v.catalog_de_service_id = s.id
  AND s.code = 'CASH_IN'
  AND v.status = 'PUBLISHED';
