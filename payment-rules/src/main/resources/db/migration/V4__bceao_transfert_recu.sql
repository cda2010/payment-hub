-- Instruction BCEAO n°020, art. 31 : un transfert reçu augmente la détention.
-- Le plafond de 4 000 000 XOF s'applique au bénéficiaire.
-- Le rechargement mensuel de 15 000 000 XOF ne s'applique pas à ce crédit.
-- Un virement entre instruments du même client ne change pas la somme détenue.

UPDATE rule_version
SET conditions_json = '{"transactionTypes":["CASH_IN","ACCOUNT_TRANSFER"],"kycLevels":["STANDARD","ENHANCED","FULL"],"excludeSubjectRoles":["DISTRIBUTEUR","ACCEPTEUR","DISTRIBUTOR","ACCEPTOR"],"skipSameCustomer":true}'
WHERE id = 'c2000001-0000-4000-8000-000000000001';

UPDATE rule_definition
SET description = 'Client identifié : 4 000 000 XOF auprès du même émetteur, tous instruments confondus. S''applique au cash-in et au transfert reçu. Instruction BCEAO n°020, art. 31.'
WHERE code = 'BCEAO_EME_SOLDE_MAX';

INSERT INTO rule_group (id, code, libelle, description, transaction_type, product_code, evaluation_strategy, status)
VALUES ('c3000001-0000-4000-8000-000000000001', 'ACCOUNT_TRANSFER_CREDIT',
        'Détention du bénéficiaire',
        'Plafond BCEAO art. 31 sur le crédit reçu. Le rechargement mensuel n''est pas évalué.',
        'ACCOUNT_TRANSFER', 'ACCOUNT_TRANSFER', 'STOP_ON_REJECT', 'ACTIVE')
ON CONFLICT (code) DO NOTHING;

INSERT INTO rule_group_item (id, group_id, rule_version_id, sort_order)
VALUES ('c4000001-0000-4000-8000-000000000003', 'c3000001-0000-4000-8000-000000000001',
        'c2000001-0000-4000-8000-000000000001', 10)
ON CONFLICT (id) DO NOTHING;
