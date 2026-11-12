-- Presentation catalogue CHECKOUT : même principe qu'ACCOUNT_TRANSFER (V88).
-- Les moyens de paiement viennent du casting participants (rôle PAYER),
-- affichés en SHORTCUT_SLIDE et liés à payProvider.
-- Bindings UI provider-specific pour enterOtp (OTP wallets) — forme ACCOUNT_TRANSFER.

UPDATE catalog_de_service_version v
SET configuration = COALESCE(v.configuration, '{}'::jsonb) || jsonb_build_object(
        'presentation', jsonb_build_object(
                'version', 1,
                'sections', jsonb_build_array(
                        jsonb_build_object(
                                'id', 'payMethods',
                                'displayKind', 'SHORTCUT_SLIDE',
                                'dataSource', 'CATALOG_PARTICIPANTS',
                                'origin', 'LOCAL',
                                'title', 'Moyen de paiement',
                                'bindsTo', 'payProvider',
                                'params', jsonb_build_object('role', 'PAYER')
                        )
                )
        )
     ),
    updated_at = NOW()
FROM catalog_de_service s
WHERE v.catalog_de_service_id = s.id
  AND s.code = 'CHECKOUT'
  AND (v.configuration -> 'presentation') IS NULL;

-- Bindings UI par providerCode pour la saisie OTP (idempotent)
UPDATE catalog_de_service_version v
SET configuration = jsonb_set(
        v.configuration,
        '{bindings}',
        COALESCE(v.configuration -> 'bindings', '[]'::jsonb)
            || jsonb_build_array(
                jsonb_build_object(
                        'step', 'enterOtp', 'kind', 'UI', 'actorRole', 'PAYER',
                        'providerCode', 'CORIS_MONEY', 'formSource', 'FORM_KEY',
                        'formKey', 'provider-otp-code'),
                jsonb_build_object(
                        'step', 'enterOtp', 'kind', 'UI', 'actorRole', 'PAYER',
                        'providerCode', 'ORANGE_MONEY', 'formSource', 'FORM_KEY',
                        'formKey', 'provider-otp-code'),
                jsonb_build_object(
                        'step', 'generateOtp', 'kind', 'INVOKE', 'actorRole', 'PAYER',
                        'providerCode', 'CORIS_MONEY',
                        'capability', 'GENERATE_OTP', 'operation', 'GENERATE_OTP'),
                jsonb_build_object(
                        'step', 'EXECUTE_PAYMENT', 'kind', 'INVOKE', 'actorRole', 'PAYER',
                        'providerCode', 'CORIS_MONEY',
                        'capability', 'CREATE_PAYMENT_SESSION', 'operation', 'CREATE_PAYMENT'),
                jsonb_build_object(
                        'step', 'EXECUTE_PAYMENT', 'kind', 'INVOKE', 'actorRole', 'PAYER',
                        'providerCode', 'ORANGE_MONEY',
                        'capability', 'CREATE_PAYMENT_SESSION', 'operation', 'CREATE_PAYMENT'),
                jsonb_build_object(
                        'step', 'EXECUTE_PAYMENT', 'kind', 'INVOKE', 'actorRole', 'PAYER',
                        'providerCode', 'WAVE',
                        'capability', 'CREATE_PAYMENT_SESSION', 'operation', 'CREATE_PAYMENT'),
                jsonb_build_object(
                        'step', 'EXECUTE_PAYMENT', 'kind', 'INVOKE', 'actorRole', 'PAYER',
                        'providerCode', 'CARD_PAYMENT',
                        'capability', 'CREATE_PAYMENT_SESSION', 'operation', 'CREATE_PAYMENT')
           )
     ),
    updated_at = NOW()
FROM catalog_de_service s
WHERE v.catalog_de_service_id = s.id
  AND s.code = 'CHECKOUT'
  AND NOT EXISTS (
        SELECT 1
        FROM jsonb_array_elements(COALESCE(v.configuration -> 'bindings', '[]'::jsonb)) b
        WHERE b ->> 'providerCode' = 'CORIS_MONEY'
          AND b ->> 'step' = 'enterOtp'
          AND b ->> 'formKey' = 'provider-otp-code'
    );
