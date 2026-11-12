-- ACCOUNT_TRANSFER : le parcours suit l'ordre client « à qui, combien, avec quel compte ».
--
-- Ordre : bénéficiaire → compte bénéficiaire → montant → compte à débiter →
-- solde → plafonds → frais → récap → authentification → exécution → confirmation.
--
-- Le compte à débiter précède le calcul des frais : TransferCatalogService.resolveFeeRule
-- choisit une grille SAME_PROVIDER ou CROSS_PROVIDER en comparant l'émetteur au
-- bénéficiaire, un récap construit avant ce choix afficherait un tarif invérifiable.
--
-- SELECT_RAIL est retiré : l'étape redemandait `destRail` déjà saisi en
-- CAPTURE_DESTINATION, avec une liste d'options divergente.
--
-- Le séquencement réel vient du BPMN `accountTransfer` (account-transfer.bpmn20.xml) ;
-- `configuration.steps` en est le miroir déclaratif, lu par l'admin catalogue.
-- Les identifiants d'étapes conservés sont inchangés : les bindings de formulaires
-- (V44, V51, V52, V53) et le récap (V64) restent valides.
UPDATE catalog_de_service_version v
SET configuration = COALESCE(v.configuration, '{}'::jsonb) || jsonb_build_object(
        'steps', jsonb_build_array(
                'CAPTURE_DESTINATION',
                'VALIDATE_ACCOUNT',
                'CAPTURE_AMOUNT',
                'SELECT_SOURCE_ACCOUNT',
                'CHECK_BALANCE',
                'CHECK_LIMITS',
                'CALCULATE_FEES',
                'CONFIRM',
                'AUTHENTICATE',
                'DEBIT',
                'EXECUTE_TRANSFER',
                'WAIT_CONFIRMATION',
                'NOTIFY'
        )
     ),
    updated_at = NOW()
FROM catalog_de_service s
WHERE v.catalog_de_service_id = s.id
  AND s.code = 'ACCOUNT_TRANSFER';

-- Le compte à débiter est désormais connu avant le récap (V64 le construisait sans).
UPDATE catalog_de_service_version v
SET configuration = jsonb_set(
        v.configuration,
        '{recap,fields}',
        jsonb_build_array(
                jsonb_build_object('key', 'amount', 'label', 'Montant'),
                jsonb_build_object('key', 'currency', 'label', 'Devise'),
                jsonb_build_object('key', 'fees', 'label', 'Frais'),
                jsonb_build_object('key', 'sourceRail', 'label', 'Source'),
                jsonb_build_object('key', 'sourceAccount', 'label', 'Compte à débiter'),
                jsonb_build_object('key', 'destRail', 'label', 'Destination')
        )
     ),
    updated_at = NOW()
FROM catalog_de_service s
WHERE v.catalog_de_service_id = s.id
  AND s.code = 'ACCOUNT_TRANSFER'
  AND v.configuration -> 'recap' IS NOT NULL;
