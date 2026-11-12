-- Grille tarifaire CASH_OUT (miroir FEE-CASH-AGENT / dépôt).

INSERT INTO grille_tarifaire
    (id, code, libelle, produit, canal, devise, date_debut, version, statut,
     continuite_requise, arrondi_echelle, arrondi_mode, created_by, updated_by,
     base_calcul)
VALUES
    ('b3000001-0000-4000-8000-000000000007', 'FEE-CASH-OUT', 'Frais retrait agent',
     'CASH_OUT', NULL, 'XOF', DATE '2026-01-01', 1, 'PUBLIEE',
     TRUE, 0, 'HALF_UP', 'flyway', 'flyway',
     'TRANSACTION_AMOUNT')
ON CONFLICT (id) DO NOTHING;

INSERT INTO tranche_tarifaire
    (id, grille_id, ordre, montant_min, montant_max, formule_id, parametres, frais_min, frais_max)
VALUES
    ('b4000001-0000-4000-8000-000000000012', 'b3000001-0000-4000-8000-000000000007', 1, 0, NULL,
     'b1000001-0000-4000-8000-000000000002', '{"taux": 0.01}', 100, 5000)
ON CONFLICT (id) DO NOTHING;
