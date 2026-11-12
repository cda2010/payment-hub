-- CASH_OUT live path : EVALUATE_RULES, capabilities BPMN, retrait DEFAULT_FEE (pricing live).
-- Préserve participants / presentation (V93, V94) : on ne touche que steps, bindings, softRules.

UPDATE catalog_de_service_version v
SET configuration = jsonb_set(
        jsonb_set(
                jsonb_set(
                        jsonb_set(
                                v.configuration,
                                '{steps}',
                                '[
                                  "CAPTURE_AMOUNT",
                                  "SELECT_SOURCE",
                                  "SELECT_AGENT_CHANNEL",
                                  "CALCULATE_FEES",
                                  "EVALUATE_RULES",
                                  "CONFIRM",
                                  "AUTHENTICATE",
                                  "EXECUTE_CASHOUT",
                                  "GENERATE_VOUCHER",
                                  "WAIT_AGENT_PAYOUT",
                                  "NOTIFY"
                                ]'::jsonb
                        ),
                        '{bindings}',
                        '[
                          {"step":"CAPTURE_AMOUNT","kind":"UI","actorRole":"SOURCE","formSource":"FORM_KEY","formKey":"cash-out-amount"},
                          {"step":"SELECT_SOURCE","kind":"UI","actorRole":"SOURCE","formSource":"FORM_KEY","formKey":"cash-out-source"},
                          {"step":"SELECT_AGENT_CHANNEL","kind":"UI","actorRole":"AGENT_NETWORK","formSource":"FORM_KEY","formKey":"cash-out-agent"},
                          {"step":"EVALUATE_RULES","kind":"CAPABILITY","capability":"EVALUATE_RULES","params":{"groupCode":"CASH_OUT_STANDARD","transactionType":"CASH_OUT"}},
                          {"step":"CONFIRM","kind":"UI","actorRole":"SOURCE","formSource":"FORM_KEY","formKey":"cash-out-confirm"},
                          {"step":"enterOtp","kind":"UI","actorRole":"SOURCE","formSource":"FORM_KEY","formKey":"provider-otp-code"}
                        ]'::jsonb
                ),
                '{softRules}',
                '[
                  {"step":"CALCULATE_FEES","type":"MIN_AMOUNT","params":{"minAmountKey":"minAmount","errorCode":"CASH_OUT_MIN_AMOUNT"}},
                  {"step":"GENERATE_VOUCHER","type":"GENERATE_CODE","params":{"field":"cashOutCode","prefix":"CO","length":8}},
                  {"step":"WAIT_AGENT_PAYOUT","type":"PASS","params":{"message":"En attente de remise cash par l''agent"}}
                ]'::jsonb
        ),
        '{defaultFee}',
        'null'::jsonb
    ),
    updated_at = NOW()
FROM catalog_de_service s
WHERE v.catalog_de_service_id = s.id
  AND s.code = 'CASH_OUT'
  AND v.workflow_key = 'cashOut'
  AND jsonb_typeof(v.configuration -> 'bindings') = 'array';

UPDATE catalog_de_service_version v
SET configuration = v.configuration - 'defaultFee',
    updated_at = NOW()
FROM catalog_de_service s
WHERE v.catalog_de_service_id = s.id
  AND s.code = 'CASH_OUT'
  AND v.workflow_key = 'cashOut'
  AND v.configuration ? 'defaultFee';
