-- Phase 2: native Form.io schemas in configuration_json (keep legacy fields-only readable by engine).

UPDATE payment_flow_step
SET configuration_json = '{"formio":{"display":"form","components":[{"type":"phoneNumber","key":"phoneNumber","label":"Numéro de téléphone","input":true,"validate":{"required":true}},{"type":"button","key":"submit","label":"Submit","input":true,"hidden":true}]}}'
WHERE code = 'COLLECT_PHONE_AND_AMOUNT'
  AND configuration_json IS NOT NULL
  AND configuration_json NOT LIKE '%"formio"%';

UPDATE payment_flow_step
SET configuration_json = '{"formio":{"display":"form","components":[{"type":"textfield","key":"accountHolder","label":"Titulaire du compte","input":true,"validate":{"required":true}},{"type":"textfield","key":"iban","label":"IBAN / N° de compte","input":true,"validate":{"required":true}},{"type":"button","key":"submit","label":"Submit","input":true,"hidden":true}]}}'
WHERE code = 'COLLECT_BANK_DETAILS'
  AND configuration_json IS NOT NULL
  AND configuration_json NOT LIKE '%"formio"%';

UPDATE payment_flow_step
SET configuration_json = '{"formio":{"display":"form","components":[{"type":"select","key":"sourceRail","label":"Compte source","input":true,"dataSrc":"values","data":{"values":[{"label":"WAVE","value":"WAVE"},{"label":"CORIS_MONEY","value":"CORIS_MONEY"},{"label":"BANK_TRANSFER","value":"BANK_TRANSFER"}]},"widget":"choicesjs","validate":{"required":true}},{"type":"select","key":"destRail","label":"Compte destination","input":true,"dataSrc":"values","data":{"values":[{"label":"CORIS_MONEY","value":"CORIS_MONEY"},{"label":"WAVE","value":"WAVE"},{"label":"BANK_TRANSFER","value":"BANK_TRANSFER"}]},"widget":"choicesjs","validate":{"required":true}},{"type":"phoneNumber","key":"destPhone","label":"Téléphone / compte destinataire","input":true,"validate":{"required":true}},{"type":"number","key":"amount","label":"Montant","input":true,"validate":{"required":true}},{"type":"button","key":"submit","label":"Submit","input":true,"hidden":true}]}}'
WHERE code = 'COLLECT_TRANSFER'
  AND configuration_json IS NOT NULL
  AND configuration_json NOT LIKE '%"formio"%';

UPDATE payment_flow_step
SET configuration_json = '{"formio":{"display":"form","components":[{"type":"select","key":"billerCode","label":"Facturier","input":true,"dataSrc":"values","data":{"values":[{"label":"SONABEL","value":"SONABEL"},{"label":"ONEA","value":"ONEA"},{"label":"CANAL+","value":"CANAL+"},{"label":"IMPOTS","value":"IMPOTS"}]},"widget":"choicesjs","validate":{"required":true}},{"type":"textfield","key":"billReference","label":"N° contrat / facture","input":true,"validate":{"required":true}},{"type":"button","key":"submit","label":"Submit","input":true,"hidden":true}]}}'
WHERE code = 'COLLECT_BILL'
  AND configuration_json IS NOT NULL
  AND configuration_json NOT LIKE '%"formio"%';

UPDATE payment_flow_step
SET configuration_json = '{"formio":{"display":"form","components":[{"type":"textfield","key":"confirmPay","label":"Tapez OUI pour confirmer le paiement","input":true,"validate":{"required":true}},{"type":"button","key":"submit","label":"Submit","input":true,"hidden":true}]}}'
WHERE code = 'CONFIRM_BILL'
  AND configuration_json IS NOT NULL
  AND configuration_json NOT LIKE '%"formio"%';
