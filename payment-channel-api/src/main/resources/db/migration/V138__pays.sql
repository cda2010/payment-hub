-- Référentiel pays RapidFrame. Le nom reprend pays.nom_pays de l'omnicanal
-- pour servir de liste au formulaire fournisseur.

CREATE TABLE IF NOT EXISTS pays (
    id UUID PRIMARY KEY,
    code VARCHAR(8) NOT NULL UNIQUE,
    nom VARCHAR(80) NOT NULL UNIQUE,
    devise VARCHAR(10) NOT NULL DEFAULT 'XOF',
    actif BOOLEAN NOT NULL DEFAULT TRUE,
    ordre INTEGER NOT NULL DEFAULT 0,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

INSERT INTO pays (id, code, nom, devise, actif, ordre)
VALUES
    ('a1000001-0000-4000-8000-000000000001', 'BF', 'Burkina Faso', 'XOF', TRUE, 10),
    ('a1000001-0000-4000-8000-000000000002', 'BJ', 'Benin', 'XOF', TRUE, 20),
    ('a1000001-0000-4000-8000-000000000003', 'CI', 'Côte d''ivoire', 'XOF', TRUE, 30),
    ('a1000001-0000-4000-8000-000000000004', 'GW', 'Guinée-Bissau', 'XOF', TRUE, 40),
    ('a1000001-0000-4000-8000-000000000005', 'GN', 'Guinée-Conakry', 'XOF', TRUE, 50),
    ('a1000001-0000-4000-8000-000000000006', 'ML', 'Mali', 'XOF', TRUE, 60),
    ('a1000001-0000-4000-8000-000000000007', 'MA', 'Maroc', 'XOF', TRUE, 70),
    ('a1000001-0000-4000-8000-000000000008', 'NE', 'Niger', 'XOF', TRUE, 80),
    ('a1000001-0000-4000-8000-000000000009', 'SN', 'Senegal', 'XOF', TRUE, 90),
    ('a1000001-0000-4000-8000-00000000000a', 'TD', 'Tchad', 'XOF', TRUE, 100),
    ('a1000001-0000-4000-8000-00000000000b', 'TG', 'Togo', 'XOF', TRUE, 110)
ON CONFLICT (code) DO NOTHING;
