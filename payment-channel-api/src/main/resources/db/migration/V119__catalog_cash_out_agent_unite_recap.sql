-- CASH_OUT : CONFIRM en RECAP + champs UO / réseau agent (vérif payment-network).

UPDATE catalog_de_service_version v
SET configuration = jsonb_set(
        jsonb_set(
                v.configuration,
                '{bindings}',
                (
                    SELECT COALESCE(jsonb_agg(
                                           CASE
                                               WHEN b->>'step' = 'CONFIRM' THEN
                                                   jsonb_build_object(
                                                           'step', 'CONFIRM',
                                                           'kind', 'UI',
                                                           'actorRole', 'SOURCE',
                                                           'formSource', 'RECAP',
                                                           'formKey', 'product-recap'
                                                   )
                                               ELSE b
                                               END
                                   ), '[]'::jsonb)
                    FROM jsonb_array_elements(v.configuration -> 'bindings') b
                )
        ),
        '{recap}',
        jsonb_build_object(
                'enabled', true,
                'title', 'Récapitulatif cash-out',
                'confirmField', 'confirmRecap',
                'fields', jsonb_build_array(
                        jsonb_build_object('key', 'amount', 'label', 'Montant'),
                        jsonb_build_object('key', 'currency', 'label', 'Devise'),
                        jsonb_build_object('key', 'fees', 'label', 'Frais'),
                        jsonb_build_object('key', 'sourceRail', 'label', 'Compte source'),
                        jsonb_build_object('key', 'agentNetwork', 'label', 'Réseau agent'),
                        jsonb_build_object('key', 'agentUniteDisplay', 'label', 'Unité / point'),
                        jsonb_build_object('key', 'agentCode', 'label', 'Code unité'),
                        jsonb_build_object('key', 'agentUniteType', 'label', 'Type unité'),
                        jsonb_build_object('key', 'agentFseCode', 'label', 'FSE'),
                        jsonb_build_object('key', 'agentZone', 'label', 'Zone')
                )
        )
    ),
    updated_at = NOW()
FROM catalog_de_service s
WHERE v.catalog_de_service_id = s.id
  AND s.code = 'CASH_OUT'
  AND v.workflow_key = 'cashOut'
  AND jsonb_typeof(v.configuration -> 'bindings') = 'array';
