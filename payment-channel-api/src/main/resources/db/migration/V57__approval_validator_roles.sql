-- Rôles validateurs ACCOUNT_TRANSFER (DMN / approval_profile)
INSERT INTO gateway_role (id, code, description)
SELECT 'b1000000-0000-4000-8000-000000000001', 'CHEF_AGENCE', 'Validateur transfert niveau 1'
WHERE NOT EXISTS (SELECT 1 FROM gateway_role WHERE code = 'CHEF_AGENCE');

INSERT INTO gateway_role (id, code, description)
SELECT 'b1000000-0000-4000-8000-000000000002', 'RESPONSABLE_OPERATIONS', 'Validateur transfert niveau 2'
WHERE NOT EXISTS (SELECT 1 FROM gateway_role WHERE code = 'RESPONSABLE_OPERATIONS');

INSERT INTO gateway_role (id, code, description)
SELECT 'b1000000-0000-4000-8000-000000000003', 'DIRECTEUR', 'Validateur transfert niveau 3'
WHERE NOT EXISTS (SELECT 1 FROM gateway_role WHERE code = 'DIRECTEUR');
