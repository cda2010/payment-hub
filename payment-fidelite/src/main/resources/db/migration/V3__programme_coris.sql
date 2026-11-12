INSERT INTO programme (
    id, tenant_id, nom, portee, points_par_mille, points_parrain, points_filleul, statut, created_at
) VALUES (
    'c1000001-0000-4000-8000-000000000001',
    'CORIS_DEMO',
    'Programme Coris',
    'TOUS',
    1,
    200,
    80,
    'ACTIF',
    TIMESTAMPTZ '2026-11-10T00:00:00Z'
);

INSERT INTO palier (id, programme_id, code, label, seuil_points, points_par_mille) VALUES
    ('c1000001-0000-4000-8000-000000000011', 'c1000001-0000-4000-8000-000000000001', 'BRONZE', 'Bronze', 0, 1.00),
    ('c1000001-0000-4000-8000-000000000012', 'c1000001-0000-4000-8000-000000000001', 'SILVER', 'Argent', 2000, 1.50),
    ('c1000001-0000-4000-8000-000000000013', 'c1000001-0000-4000-8000-000000000001', 'GOLD', 'Or', 8000, 2.00),
    ('c1000001-0000-4000-8000-000000000014', 'c1000001-0000-4000-8000-000000000001', 'PLATINUM', 'Platine', 20000, 3.00);

INSERT INTO palier_avantage (palier_id, avantage, position) VALUES
    ('c1000001-0000-4000-8000-000000000011', '1 pt / 1 000 F de paiement', 0),
    ('c1000001-0000-4000-8000-000000000011', 'Alertes offres', 1),
    ('c1000001-0000-4000-8000-000000000012', '1,5 pt / 1 000 F', 0),
    ('c1000001-0000-4000-8000-000000000012', 'Frais transfert réduits', 1),
    ('c1000001-0000-4000-8000-000000000013', '2 pts / 1 000 F', 0),
    ('c1000001-0000-4000-8000-000000000013', 'Cashback mensuel', 1),
    ('c1000001-0000-4000-8000-000000000013', 'Support prioritaire', 2),
    ('c1000001-0000-4000-8000-000000000014', '3 pts / 1 000 F', 0),
    ('c1000001-0000-4000-8000-000000000014', 'Invitations partenaires', 1),
    ('c1000001-0000-4000-8000-000000000014', 'Conseiller dédié', 2);
