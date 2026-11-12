-- CASH_IN live path : EVALUATE_RULES, capabilities BPMN, retrait DEFAULT_FEE (pricing live).
-- Les serviceTasks portent pay:capability ; le catalogue garde UI + softRules + params EVALUATE_RULES.

UPDATE catalog_de_service_version v
SET configuration = jsonb_set(
        jsonb_set(
                jsonb_set(
                        jsonb_set(
                                v.configuration,
                                '{steps}',
                                '[
                                  "CAPTURE_AMOUNT",
                                  "SELECT_DESTINATION",
                                  "SELECT_AGENT_CHANNEL",
                                  "CALCULATE_FEES",
                                  "EVALUATE_RULES",
                                  "CONFIRM",
                                  "AUTHENTICATE",
                                  "GENERATE_VOUCHER",
                                  "WAIT_AGENT_COLLECT",
                                  "EXECUTE_CASHIN",
                                  "NOTIFY"
                                ]'::jsonb
                        ),
                        '{bindings}',
                        '[
                          {"step":"CAPTURE_AMOUNT","kind":"UI","actorRole":"DESTINATION","formSource":"FORM_KEY","formKey":"cash-in-amount"},
                          {"step":"SELECT_DESTINATION","kind":"UI","actorRole":"DESTINATION","formSource":"FORM_KEY","formKey":"cash-in-destination"},
                          {"step":"SELECT_AGENT_CHANNEL","kind":"UI","actorRole":"AGENT_NETWORK","formSource":"FORM_KEY","formKey":"cash-in-agent"},
                          {"step":"EVALUATE_RULES","kind":"CAPABILITY","capability":"EVALUATE_RULES","params":{"groupCode":"CASH_IN_STANDARD","transactionType":"CASH_IN"}},
                          {"step":"CONFIRM","kind":"UI","actorRole":"DESTINATION","formSource":"RECAP","formKey":"product-recap"},
                          {"step":"enterOtp","kind":"UI","actorRole":"DESTINATION","formSource":"FORM_KEY","formKey":"provider-otp-code"}
                        ]'::jsonb
                ),
                '{softRules}',
                '[
                  {"step":"CALCULATE_FEES","type":"MIN_AMOUNT","params":{"minAmountKey":"minAmount","errorCode":"CASH_IN_MIN_AMOUNT"}},
                  {"step":"GENERATE_VOUCHER","type":"GENERATE_CODE","params":{"field":"cashInCode","prefix":"CI","length":8}},
                  {"step":"WAIT_AGENT_COLLECT","type":"PASS","params":{"message":"En attente d''encaissement cash par l''agent"}}
                ]'::jsonb
        ),
        '{defaultFee}',
        'null'::jsonb
    ),
    updated_at = NOW()
FROM catalog_de_service s
WHERE v.catalog_de_service_id = s.id
  AND s.code = 'CASH_IN'
  AND v.workflow_key = 'cashIn'
  AND jsonb_typeof(v.configuration -> 'bindings') = 'array';

-- Retirer la clé defaultFee si elle existe encore (jsonb_set met null)
UPDATE catalog_de_service_version v
SET configuration = v.configuration - 'defaultFee',
    updated_at = NOW()
FROM catalog_de_service s
WHERE v.catalog_de_service_id = s.id
  AND s.code = 'CASH_IN'
  AND v.workflow_key = 'cashIn'
  AND v.configuration ? 'defaultFee';
