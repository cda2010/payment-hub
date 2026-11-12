-- P2–P4 : bindings[] UI / INVOKE / SOFT / WAIT sur versions catalogue
UPDATE catalog_de_service_version v
SET configuration = COALESCE(v.configuration, '{}'::jsonb) || jsonb_build_object(
        'bindings', jsonb_build_array(
                jsonb_build_object('step', 'SELECT_SOURCE_ACCOUNT', 'kind', 'UI', 'actorRole', 'SOURCE',
                                   'formSource', 'FORM_KEY', 'formKey', 'account-transfer-source'),
                jsonb_build_object('step', 'CAPTURE_DESTINATION', 'kind', 'UI', 'actorRole', 'DESTINATION',
                                   'formSource', 'FORM_KEY', 'formKey', 'account-transfer-destination'),
                jsonb_build_object('step', 'VALIDATE_ACCOUNT', 'kind', 'SOFT', 'actorRole', 'DESTINATION',
                                   'operation', 'VALIDATE_ACCOUNT'),
                jsonb_build_object('step', 'CAPTURE_AMOUNT', 'kind', 'UI', 'actorRole', 'SOURCE',
                                   'formSource', 'FORM_KEY', 'formKey', 'account-transfer-amount'),
                jsonb_build_object('step', 'CHECK_BALANCE', 'kind', 'SOFT', 'actorRole', 'SOURCE',
                                   'operation', 'BALANCE_CHECK'),
                jsonb_build_object('step', 'CHECK_LIMITS', 'kind', 'SOFT', 'actorRole', 'SOURCE',
                                   'operation', 'LIMITS_CHECK'),
                jsonb_build_object('step', 'CALCULATE_FEES', 'kind', 'SOFT', 'operation', 'CALCULATE_FEES'),
                jsonb_build_object('step', 'SELECT_RAIL', 'kind', 'UI', 'actorRole', 'SOURCE',
                                   'formSource', 'FORM_KEY', 'formKey', 'account-transfer-rail'),
                jsonb_build_object('step', 'CONFIRM', 'kind', 'UI', 'actorRole', 'SOURCE',
                                   'formSource', 'FORM_KEY', 'formKey', 'account-transfer-confirm'),
                jsonb_build_object('step', 'generateOtp', 'kind', 'INVOKE', 'actorRole', 'SOURCE',
                                   'operation', 'GENERATE_OTP'),
                jsonb_build_object('step', 'enterOtp', 'kind', 'UI', 'actorRole', 'SOURCE',
                                   'formSource', 'FORM_KEY', 'formKey', 'provider-otp-code'),
                jsonb_build_object('step', 'DEBIT', 'kind', 'SOFT', 'actorRole', 'SOURCE',
                                   'operation', 'DEBIT'),
                jsonb_build_object('step', 'EXECUTE_TRANSFER', 'kind', 'INVOKE', 'actorRole', 'SOURCE',
                                   'operation', 'TRANSFER_EXECUTE'),
                jsonb_build_object('step', 'WAIT_CONFIRMATION', 'kind', 'WAIT'),
                jsonb_build_object('step', 'NOTIFY', 'kind', 'SOFT', 'operation', 'NOTIFY')
        )
     ),
    updated_at = NOW()
FROM catalog_de_service s
WHERE v.catalog_de_service_id = s.id
  AND s.code = 'ACCOUNT_TRANSFER';

UPDATE catalog_de_service_version v
SET configuration = COALESCE(v.configuration, '{}'::jsonb) || jsonb_build_object(
        'bindings', jsonb_build_array(
                jsonb_build_object('step', 'lookupBill', 'kind', 'INVOKE', 'actorRole', 'BILLER',
                                   'operation', 'LOOKUP_BILL'),
                jsonb_build_object('step', 'LOOKUP_BILL', 'kind', 'UI', 'actorRole', 'BILLER',
                                   'formSource', 'BILLER_PRODUCT', 'billerScreen', 'LOOKUP'),
                jsonb_build_object('step', 'confirmBill', 'kind', 'UI', 'actorRole', 'BILLER',
                                   'formSource', 'BILLER_PRODUCT', 'billerScreen', 'CONFIRM'),
                jsonb_build_object('step', 'CONFIRM', 'kind', 'UI', 'actorRole', 'BILLER',
                                   'formSource', 'BILLER_PRODUCT', 'billerScreen', 'CONFIRM'),
                jsonb_build_object('step', 'generateOtp', 'kind', 'INVOKE', 'actorRole', 'PAYER',
                                   'operation', 'GENERATE_OTP'),
                jsonb_build_object('step', 'enterOtp', 'kind', 'UI', 'actorRole', 'PAYER',
                                   'formSource', 'FORM_KEY', 'formKey', 'provider-otp-code'),
                jsonb_build_object('step', 'confirmPayment', 'kind', 'INVOKE', 'actorRole', 'PAYER',
                                   'operation', 'CONFIRM_PAYMENT'),
                jsonb_build_object('step', 'payBill', 'kind', 'INVOKE', 'actorRole', 'BILLER',
                                   'operation', 'PAY_BILL')
        )
     ),
    updated_at = NOW()
FROM catalog_de_service s
WHERE v.catalog_de_service_id = s.id
  AND s.code = 'BILL_PAYMENT';
