-- Offre retail : autoriser CASH_IN / CASH_OUT (menu mobile client).
-- Les plugins mobile filtrent sur l'offre Identity (depot-mobile→CASH_IN, cash-withdrawal→CASH_OUT).

INSERT INTO identity_profile_service (id, profile_id, service_code, libelle, active)
VALUES
    ('f2000001-0000-4000-8000-000000000031', 'f1000001-0000-4000-8000-000000000001',
     'CASH_IN', 'Dépôt (cash-in)', TRUE),
    ('f2000001-0000-4000-8000-000000000032', 'f1000001-0000-4000-8000-000000000001',
     'CASH_OUT', 'Retrait (cash-out)', TRUE)
ON CONFLICT (profile_id, service_code) DO UPDATE
SET libelle = EXCLUDED.libelle, active = TRUE;

INSERT INTO identity_profile_grid (id, profile_id, produit_code, grille_code, canal_code, devise)
VALUES
    ('f3000001-0000-4000-8000-000000000031', 'f1000001-0000-4000-8000-000000000101',
     'CASH_IN', 'FEE-CASH-AGENT', '', 'XOF'),
    ('f3000001-0000-4000-8000-000000000032', 'f1000001-0000-4000-8000-000000000101',
     'CASH_OUT', 'FEE-CASH-OUT', '', 'XOF')
ON CONFLICT (profile_id, produit_code, canal_code) DO UPDATE
SET grille_code = EXCLUDED.grille_code, devise = EXCLUDED.devise;
