-- Retire `gateway.optionalAmountAtStart` posé par V85 : la règle du montant à la
-- création (CatalogAmountPolicy) reconnaît désormais l'étape CAPTURE_AMOUNT où
-- qu'elle se trouve dans `steps`, et non plus seulement en première position.
--
-- Le parcours déclaré par V86 suffit donc à dire que le montant arrive en cours
-- de route : un seul endroit à tenir à jour plutôt que deux.
UPDATE catalog_de_service_version v
SET configuration = jsonb_set(
        v.configuration,
        '{gateway}',
        (v.configuration -> 'gateway') - 'optionalAmountAtStart',
        true
    ),
    updated_at = NOW()
FROM catalog_de_service s
WHERE v.catalog_de_service_id = s.id
  AND s.code = 'ACCOUNT_TRANSFER'
  AND v.configuration -> 'gateway' ? 'optionalAmountAtStart';
