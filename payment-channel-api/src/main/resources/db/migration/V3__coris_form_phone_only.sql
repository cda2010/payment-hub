-- Ne redemander que le téléphone : le montant est saisi à l'initialisation du paiement
UPDATE payment_flow_step
SET configuration_json = '{"fields":[{"name":"phoneNumber","label":"Numéro de téléphone","type":"tel","required":true}]}'
WHERE code = 'COLLECT_PHONE_AND_AMOUNT';
