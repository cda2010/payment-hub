-- CASH_OUT : parcours branchés sur withdrawalChannel (GAB | AGENT | AGENCY).
-- BPMN : gw_withdrawalChannel (capture) + gw_withdrawalFulfill (exécution).
-- GAB / AGENCY : forms + soft PASS jusqu'aux capabilities hôtes.

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

-- Bindings UI GAB / AGENCY (sans toucher CONFIRM product-recap / agent existants)
UPDATE catalog_de_service_version v
SET configuration = jsonb_set(
        v.configuration,
        '{bindings}',
        COALESCE(v.configuration -> 'bindings', '[]'::jsonb)
            || CASE
                   WHEN EXISTS (
                       SELECT 1
                       FROM jsonb_array_elements(COALESCE(v.configuration -> 'bindings', '[]'::jsonb)) b
                       WHERE b->>'step' = 'CAPTURE_GAB'
                   ) THEN '[]'::jsonb
                   ELSE jsonb_build_array(
                           jsonb_build_object(
                                   'step', 'CAPTURE_GAB',
                                   'kind', 'UI',
                                   'actorRole', 'SOURCE',
                                   'formSource', 'FORM_KEY',
                                   'formKey', 'cash-out-gab'
                           )
                        )
               END
            || CASE
                   WHEN EXISTS (
                       SELECT 1
                       FROM jsonb_array_elements(COALESCE(v.configuration -> 'bindings', '[]'::jsonb)) b
                       WHERE b->>'step' = 'CAPTURE_AGENCY'
                   ) THEN '[]'::jsonb
                   ELSE jsonb_build_array(
                           jsonb_build_object(
                                   'step', 'CAPTURE_AGENCY',
                                   'kind', 'UI',
                                   'actorRole', 'SOURCE',
                                   'formSource', 'FORM_KEY',
                                   'formKey', 'cash-out-agency'
                           )
                        )
               END
    ),
    updated_at = NOW()
FROM catalog_de_service s
WHERE v.catalog_de_service_id = s.id
  AND s.code = 'CASH_OUT'
  AND v.workflow_key = 'cashOut'
  AND jsonb_typeof(v.configuration -> 'bindings') = 'array';

-- Soft rules : attente GAB (après voucher) + stub AGENCY ; message agent sans code
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
                WHERE r->>'step' NOT IN ('FULFILL_GAB', 'WAIT_GAB_WITHDRAWAL', 'FULFILL_AGENCY')
                UNION ALL
                SELECT jsonb_build_object(
                               'step', 'WAIT_GAB_WITHDRAWAL',
                               'type', 'PASS',
                               'params', jsonb_build_object(
                                       'message', 'En attente de retrait au GAB (saisie du code)'
                               )
                       )
                UNION ALL
                SELECT jsonb_build_object(
                               'step', 'FULFILL_AGENCY',
                               'type', 'PASS',
                               'params', jsonb_build_object(
                                       'message', 'Retrait agence — rail hôte à brancher'
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

-- copyRequestFields : withdrawalChannel + champs capture GAB/agence
UPDATE catalog_de_service_version v
SET configuration = jsonb_set(
        v.configuration,
        '{gateway,copyRequestFields}',
        (
            SELECT COALESCE(jsonb_agg(to_jsonb(f)), '[]'::jsonb)
            FROM (
                SELECT DISTINCT f
                FROM (
                    SELECT jsonb_array_elements_text(
                                   COALESCE(v.configuration #> '{gateway,copyRequestFields}', '[]'::jsonb)
                           ) AS f
                    UNION ALL
                    SELECT 'withdrawalChannel'
                    UNION ALL
                    SELECT 'gabReference'
                    UNION ALL
                    SELECT 'agencyCode'
                ) src
            ) d
        )
    ),
    updated_at = NOW()
FROM catalog_de_service s
WHERE v.catalog_de_service_id = s.id
  AND s.code = 'CASH_OUT'
  AND v.workflow_key = 'cashOut'
  AND v.configuration ? 'gateway';
