-- ACCOUNT_TRANSFER : le montant est saisi par l'étape CAPTURE_AMOUNT du parcours,
-- après la destination. Exiger un montant dès POST /gateway/account-transfer forçait
-- le client à le demander avant l'ouverture du parcours, et vidait l'étape dédiée.
-- `optionalAmountAtStart` est le drapeau prévu par PaymentEngine.isAmountDeferredAtStart.
--
-- jsonb_set (et non `||` au niveau racine) pour conserver le reste de `gateway`
-- (pathAlias, providersQuery, hostFlow…).
UPDATE catalog_de_service_version v
SET configuration = jsonb_set(
        COALESCE(v.configuration, '{}'::jsonb),
        '{gateway}',
        COALESCE(v.configuration -> 'gateway', '{}'::jsonb)
            || jsonb_build_object('optionalAmountAtStart', true),
        true
    ),
    updated_at = NOW()
FROM catalog_de_service s
WHERE v.catalog_de_service_id = s.id
  AND s.code = 'ACCOUNT_TRANSFER';
