-- CASH_OUT : voucher (GENERATE_VOUCHER) sur branche GAB, plus sur AGENT.
-- Idempotent si V127 déjà appliqué avec l'ancien FULFILL_GAB.

UPDATE catalog_de_service_version v
SET configuration = jsonb_set(
        v.configuration,
        '{steps}',
        '[
          "CAPTURE_AMOUNT",
          "SELECT_SOURCE",
          "SELECT_AGENT_CHANNEL",
          "CAPTURE_GAB",
          "CAPTURE_AGENCY",
          "CALCULATE_FEES",
          "EVALUATE_RULES",
          "CONFIRM",
          "AUTHENTICATE",
          "EXECUTE_CASHOUT",
          "WAIT_AGENT_PAYOUT",
          "GENERATE_VOUCHER",
          "WAIT_GAB_WITHDRAWAL",
          "FULFILL_AGENCY",
          "NOTIFY"
        ]'::jsonb
    ),
    updated_at = NOW()
FROM catalog_de_service s
WHERE v.catalog_de_service_id = s.id
  AND s.code = 'CASH_OUT'
  AND v.workflow_key = 'cashOut';

UPDATE catalog_de_service_version v
SET configuration = jsonb_set(
        v.configuration,
        '{softRules}',
        (
            SELECT COALESCE(jsonb_agg(r), '[]'::jsonb)
            FROM (
                SELECT CASE
                           WHEN r->>'step' = 'WAIT_AGENT_PAYOUT' AND r->>'type' = 'PASS'
                               THEN jsonb_build_object(
                                       'step', 'WAIT_AGENT_PAYOUT',
                                       'type', 'PASS',
                                       'params', jsonb_build_object(
                                               'message', 'En attente de remise cash par l''agent'
                                       )
                               )
                           ELSE r
                       END AS r
                FROM jsonb_array_elements(COALESCE(v.configuration -> 'softRules', '[]'::jsonb)) AS t(r)
                WHERE r->>'step' NOT IN ('FULFILL_GAB', 'WAIT_GAB_WITHDRAWAL')
                UNION ALL
                SELECT jsonb_build_object(
                               'step', 'WAIT_GAB_WITHDRAWAL',
                               'type', 'PASS',
                               'params', jsonb_build_object(
                                       'message', 'En attente de retrait au GAB (saisie du code)'
                               )
                       )
            ) s
        )
    ),
    updated_at = NOW()
FROM catalog_de_service s
WHERE v.catalog_de_service_id = s.id
  AND s.code = 'CASH_OUT'
  AND v.workflow_key = 'cashOut';
