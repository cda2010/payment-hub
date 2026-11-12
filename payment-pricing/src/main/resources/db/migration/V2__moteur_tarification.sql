-- Moteur de tarification configurable (cahier des charges v1.0) :
-- catalogue de formules versionnées, grilles versionnées, tranches avec paramètres JSONB.
-- Les grilles de frais de V1 sont converties puis supprimées ; limites, taux et policies sont conservés.

CREATE TABLE formule (
    id          UUID PRIMARY KEY,
    code        VARCHAR(64)   NOT NULL,
    libelle     VARCHAR(150)  NOT NULL,
    version     INT           NOT NULL CHECK (version > 0),
    type        VARCHAR(16)   NOT NULL CHECK (type IN ('PREDEFINIE', 'EXPRESSION')),
    expression  VARCHAR(500),
    description VARCHAR(1000),
    statut      VARCHAR(16)   NOT NULL CHECK (statut IN ('ACTIVE', 'INACTIVE')),
    created_at  TIMESTAMPTZ   NOT NULL DEFAULT NOW(),
    CONSTRAINT uq_formule_code_version UNIQUE (code, version)
);

CREATE TABLE formule_parametre (
    id                 UUID PRIMARY KEY,
    formule_id         UUID          NOT NULL REFERENCES formule (id) ON DELETE CASCADE,
    code               VARCHAR(64)   NOT NULL,
    libelle            VARCHAR(150)  NOT NULL,
    type_donnee        VARCHAR(16)   NOT NULL CHECK (type_donnee IN ('DECIMAL', 'INTEGER', 'BOOLEAN', 'STRING', 'ENUM')),
    obligatoire        BOOLEAN       NOT NULL,
    valeur_defaut      VARCHAR(100),
    valeur_min         NUMERIC(24,8),
    valeur_max         NUMERIC(24,8),
    nb_decimales       INT CHECK (nb_decimales BETWEEN 0 AND 8),
    unite              VARCHAR(16),
    ordre              INT           NOT NULL,
    aide               VARCHAR(500),
    valeurs_autorisees JSONB,
    longueur_max       INT CHECK (longueur_max > 0),
    format             VARCHAR(200),
    CONSTRAINT uq_formule_parametre UNIQUE (formule_id, code)
);

CREATE TABLE grille_tarifaire (
    id                 UUID PRIMARY KEY,
    code               VARCHAR(64)   NOT NULL,
    libelle            VARCHAR(150)  NOT NULL,
    produit            VARCHAR(80)   NOT NULL,
    canal              VARCHAR(40),
    devise             VARCHAR(3)    NOT NULL,
    date_debut         DATE          NOT NULL,
    date_fin           DATE,
    version            INT           NOT NULL CHECK (version > 0),
    statut             VARCHAR(16)   NOT NULL
        CHECK (statut IN ('BROUILLON', 'A_VALIDER', 'VALIDEE', 'PUBLIEE', 'DESACTIVEE')),
    continuite_requise BOOLEAN       NOT NULL DEFAULT TRUE,
    arrondi_echelle    INT           NOT NULL DEFAULT 0 CHECK (arrondi_echelle BETWEEN 0 AND 6),
    arrondi_mode       VARCHAR(16)   NOT NULL DEFAULT 'HALF_UP'
        CHECK (arrondi_mode IN ('UP', 'DOWN', 'CEILING', 'FLOOR', 'HALF_UP', 'HALF_DOWN', 'HALF_EVEN')),
    created_at         TIMESTAMPTZ   NOT NULL DEFAULT NOW(),
    created_by         VARCHAR(100)  NOT NULL,
    updated_at         TIMESTAMPTZ   NOT NULL DEFAULT NOW(),
    updated_by         VARCHAR(100)  NOT NULL,
    CONSTRAINT uq_grille_code_version UNIQUE (code, version),
    CONSTRAINT ck_grille_dates CHECK (date_fin IS NULL OR date_fin >= date_debut)
);

CREATE INDEX idx_grille_selection ON grille_tarifaire (produit, devise, statut);

CREATE TABLE tranche_tarifaire (
    id          UUID PRIMARY KEY,
    grille_id   UUID          NOT NULL REFERENCES grille_tarifaire (id) ON DELETE CASCADE,
    ordre       INT           NOT NULL,
    montant_min NUMERIC(18,2) NOT NULL CHECK (montant_min >= 0),
    montant_max NUMERIC(18,2),
    formule_id  UUID          NOT NULL REFERENCES formule (id),
    parametres  JSONB         NOT NULL DEFAULT '{}'::jsonb,
    frais_min   NUMERIC(18,2) CHECK (frais_min >= 0),
    frais_max   NUMERIC(18,2) CHECK (frais_max >= 0),
    CONSTRAINT ck_tranche_bornes CHECK (montant_max IS NULL OR montant_max >= montant_min),
    CONSTRAINT ck_tranche_frais CHECK (frais_min IS NULL OR frais_max IS NULL OR frais_max >= frais_min)
);

CREATE INDEX idx_tranche_grille ON tranche_tarifaire (grille_id, ordre);

-- ── Catalogue initial (§3) ─────────────────────────────────────────────────

INSERT INTO formule (id, code, libelle, version, type, expression, description, statut) VALUES
    ('b1000001-0000-4000-8000-000000000001', 'FIXED', 'Frais fixes', 1, 'PREDEFINIE',
     'F = montantFixe', 'Montant de frais identique quel que soit le montant de la transaction.', 'ACTIVE'),
    ('b1000001-0000-4000-8000-000000000002', 'PERCENT_TOTAL', 'Pourcentage du montant', 1, 'PREDEFINIE',
     'F = montant × taux + fraisFixes', 'Pourcentage appliqué au montant total, frais fixes facultatifs.', 'ACTIVE'),
    ('b1000001-0000-4000-8000-000000000003', 'AX_PLUS_Y', 'Pourcentage plus frais fixes', 1, 'PREDEFINIE',
     'F = montant × taux + fraisFixes', 'Pourcentage appliqué au montant total plus des frais fixes obligatoires.', 'ACTIVE'),
    ('b1000001-0000-4000-8000-000000000004', 'EXCESS_AX_PLUS_Y', 'Pourcentage au-delà d''un seuil', 1, 'PREDEFINIE',
     'Si montant ≤ seuil : F = 0 ; sinon F = (montant − seuil) × taux + fraisFixes',
     'Seule la part du montant qui dépasse le seuil est taxée ; le seuil est indépendant des bornes de la tranche.', 'ACTIVE'),
    ('b1000001-0000-4000-8000-000000000005', 'EXCESS_CAPPED', 'Pourcentage au-delà d''un seuil, plafonné', 1, 'PREDEFINIE',
     'Si montant ≤ seuil : F = 0 ; sinon F = min((montant − seuil) × taux + fraisFixes, plafond)',
     'Comme EXCESS_AX_PLUS_Y, avec un plafond propre à la formule.', 'ACTIVE');

INSERT INTO formule_parametre
    (id, formule_id, code, libelle, type_donnee, obligatoire, valeur_defaut, valeur_min, valeur_max, nb_decimales, unite, ordre, aide)
VALUES
    ('b2000001-0000-4000-8000-000000000001', 'b1000001-0000-4000-8000-000000000001',
     'montantFixe', 'Montant fixe', 'DECIMAL', TRUE, NULL, 0, NULL, 2, 'MONTANT', 1,
     'Frais appliqués, dans la devise de la grille.'),

    ('b2000001-0000-4000-8000-000000000002', 'b1000001-0000-4000-8000-000000000002',
     'taux', 'Taux', 'DECIMAL', TRUE, NULL, 0, 1, 6, 'TAUX', 1,
     'Forme décimale : 0,015 = 1,5 %.'),
    ('b2000001-0000-4000-8000-000000000003', 'b1000001-0000-4000-8000-000000000002',
     'fraisFixes', 'Frais fixes', 'DECIMAL', FALSE, '0', 0, NULL, 2, 'MONTANT', 2,
     'Ajoutés au pourcentage ; 0 si non renseignés.'),

    ('b2000001-0000-4000-8000-000000000004', 'b1000001-0000-4000-8000-000000000003',
     'taux', 'Taux', 'DECIMAL', TRUE, NULL, 0, 1, 6, 'TAUX', 1,
     'Forme décimale : 0,015 = 1,5 %.'),
    ('b2000001-0000-4000-8000-000000000005', 'b1000001-0000-4000-8000-000000000003',
     'fraisFixes', 'Frais fixes', 'DECIMAL', TRUE, NULL, 0, NULL, 2, 'MONTANT', 2,
     'Ajoutés au pourcentage.'),

    ('b2000001-0000-4000-8000-000000000006', 'b1000001-0000-4000-8000-000000000004',
     'seuil', 'Seuil', 'DECIMAL', TRUE, NULL, 0, NULL, 2, 'MONTANT', 1,
     'Part du montant non taxée ; aucun frais si le montant ne le dépasse pas.'),
    ('b2000001-0000-4000-8000-000000000007', 'b1000001-0000-4000-8000-000000000004',
     'taux', 'Taux', 'DECIMAL', TRUE, NULL, 0, 1, 6, 'TAUX', 2,
     'Appliqué à (montant − seuil). Forme décimale : 0,015 = 1,5 %.'),
    ('b2000001-0000-4000-8000-000000000008', 'b1000001-0000-4000-8000-000000000004',
     'fraisFixes', 'Frais fixes', 'DECIMAL', FALSE, '0', 0, NULL, 2, 'MONTANT', 3,
     'Ajoutés dès que le montant dépasse le seuil.'),

    ('b2000001-0000-4000-8000-000000000009', 'b1000001-0000-4000-8000-000000000005',
     'seuil', 'Seuil', 'DECIMAL', TRUE, NULL, 0, NULL, 2, 'MONTANT', 1,
     'Part du montant non taxée ; aucun frais si le montant ne le dépasse pas.'),
    ('b2000001-0000-4000-8000-000000000010', 'b1000001-0000-4000-8000-000000000005',
     'taux', 'Taux', 'DECIMAL', TRUE, NULL, 0, 1, 6, 'TAUX', 2,
     'Appliqué à (montant − seuil). Forme décimale : 0,015 = 1,5 %.'),
    ('b2000001-0000-4000-8000-000000000011', 'b1000001-0000-4000-8000-000000000005',
     'fraisFixes', 'Frais fixes', 'DECIMAL', TRUE, NULL, 0, NULL, 2, 'MONTANT', 3,
     'Ajoutés dès que le montant dépasse le seuil.'),
    ('b2000001-0000-4000-8000-000000000012', 'b1000001-0000-4000-8000-000000000005',
     'plafond', 'Plafond', 'DECIMAL', TRUE, NULL, 0, NULL, 2, 'MONTANT', 4,
     'Frais maximum produits par la formule.');

-- ── Grilles publiées ───────────────────────────────────────────────────────
-- TRF_STD : grille de démonstration du cahier des charges (§3.2, §8.1) ; chiffres illustratifs.
-- FEE-* : conversion des grilles de frais V1 (périmètre repris des policies, taux passés en
-- forme décimale, bornes ramenées à l'unité de XOF : 50 000,01 devient 50 001).

INSERT INTO grille_tarifaire
    (id, code, libelle, produit, canal, devise, date_debut, version, statut, continuite_requise,
     arrondi_echelle, arrondi_mode, created_by, updated_by)
VALUES
    ('b3000001-0000-4000-8000-000000000001', 'TRF_STD', 'Transfert standard (démonstration)',
     'TRANSFERT', 'MOBILE_MONEY', 'XOF', DATE '2026-01-01', 1, 'PUBLIEE', TRUE, 0, 'HALF_UP', 'flyway', 'flyway'),
    ('b3000001-0000-4000-8000-000000000002', 'FEE-DOMESTIC', 'Frais transfert domestique',
     'ACCOUNT_TRANSFER', 'WEB', 'XOF', DATE '2026-01-01', 1, 'PUBLIEE', TRUE, 0, 'HALF_UP', 'flyway', 'flyway'),
    ('b3000001-0000-4000-8000-000000000003', 'FEE-RTGS', 'Frais RTGS',
     'RTGS', 'WEB', 'XOF', DATE '2026-01-01', 1, 'PUBLIEE', TRUE, 0, 'HALF_UP', 'flyway', 'flyway'),
    ('b3000001-0000-4000-8000-000000000004', 'FEE-SWIFT', 'Frais SWIFT',
     'SWIFT', 'WEB', 'XOF', DATE '2026-01-01', 1, 'PUBLIEE', TRUE, 0, 'HALF_UP', 'flyway', 'flyway'),
    ('b3000001-0000-4000-8000-000000000005', 'FEE-BILL', 'Frais paiement facture',
     'BILL_PAYMENT', NULL, 'XOF', DATE '2026-01-01', 1, 'PUBLIEE', TRUE, 0, 'HALF_UP', 'flyway', 'flyway'),
    ('b3000001-0000-4000-8000-000000000006', 'FEE-CASH-AGENT', 'Frais dépôt/retrait agent',
     'CASH_IN', NULL, 'XOF', DATE '2026-01-01', 1, 'PUBLIEE', TRUE, 0, 'HALF_UP', 'flyway', 'flyway');

INSERT INTO tranche_tarifaire
    (id, grille_id, ordre, montant_min, montant_max, formule_id, parametres, frais_min, frais_max)
VALUES
    -- TRF_STD
    ('b4000001-0000-4000-8000-000000000001', 'b3000001-0000-4000-8000-000000000001', 1, 0, 8000,
     'b1000001-0000-4000-8000-000000000001', '{"montantFixe": 0}', NULL, NULL),
    ('b4000001-0000-4000-8000-000000000002', 'b3000001-0000-4000-8000-000000000001', 2, 8001, 25000,
     'b1000001-0000-4000-8000-000000000004', '{"seuil": 8000, "taux": 0.015, "fraisFixes": 100}', NULL, NULL),
    ('b4000001-0000-4000-8000-000000000003', 'b3000001-0000-4000-8000-000000000001', 3, 25001, 100000,
     'b1000001-0000-4000-8000-000000000004', '{"seuil": 25000, "taux": 0.01, "fraisFixes": 200}', NULL, NULL),
    ('b4000001-0000-4000-8000-000000000004', 'b3000001-0000-4000-8000-000000000001', 4, 100001, 500000,
     'b1000001-0000-4000-8000-000000000005', '{"seuil": 100000, "taux": 0.005, "fraisFixes": 500, "plafond": 2000}', NULL, NULL),
    -- FEE-DOMESTIC (FIXED 100 ; FIXED_PLUS_PERCENT 200 + 0,5 % [200 ; 2 500] ; PERCENT 0,35 % [500 ; 15 000])
    ('b4000001-0000-4000-8000-000000000005', 'b3000001-0000-4000-8000-000000000002', 1, 0, 50000,
     'b1000001-0000-4000-8000-000000000001', '{"montantFixe": 100}', NULL, NULL),
    ('b4000001-0000-4000-8000-000000000006', 'b3000001-0000-4000-8000-000000000002', 2, 50001, 500000,
     'b1000001-0000-4000-8000-000000000003', '{"taux": 0.005, "fraisFixes": 200}', 200, 2500),
    ('b4000001-0000-4000-8000-000000000007', 'b3000001-0000-4000-8000-000000000002', 3, 500001, NULL,
     'b1000001-0000-4000-8000-000000000002', '{"taux": 0.0035}', 500, 15000),
    -- FEE-RTGS (FIXED 5 000)
    ('b4000001-0000-4000-8000-000000000008', 'b3000001-0000-4000-8000-000000000003', 1, 0, NULL,
     'b1000001-0000-4000-8000-000000000001', '{"montantFixe": 5000}', NULL, NULL),
    -- FEE-SWIFT (FIXED_PLUS_PERCENT 25 000 + 0,15 % [25 000 ; 150 000])
    ('b4000001-0000-4000-8000-000000000009', 'b3000001-0000-4000-8000-000000000004', 1, 0, NULL,
     'b1000001-0000-4000-8000-000000000003', '{"taux": 0.0015, "fraisFixes": 25000}', 25000, 150000),
    -- FEE-BILL (FIXED 50)
    ('b4000001-0000-4000-8000-000000000010', 'b3000001-0000-4000-8000-000000000005', 1, 0, NULL,
     'b1000001-0000-4000-8000-000000000001', '{"montantFixe": 50}', NULL, NULL),
    -- FEE-CASH-AGENT (PERCENT 1 % [100 ; 5 000])
    ('b4000001-0000-4000-8000-000000000011', 'b3000001-0000-4000-8000-000000000006', 1, 0, NULL,
     'b1000001-0000-4000-8000-000000000002', '{"taux": 0.01}', 100, 5000);

-- ── Anciennes grilles de frais ─────────────────────────────────────────────
-- Le payeur des frais reste porté par la policy (il était porté par l'ancienne grille).

ALTER TABLE pricing_policy ADD COLUMN fee_bearer VARCHAR(20) NOT NULL DEFAULT 'SENDER';

UPDATE pricing_policy p
SET fee_bearer = s.fee_bearer
FROM pricing_fee_schedule s
WHERE s.code = p.fee_schedule_code;

ALTER TABLE pricing_policy DROP COLUMN fee_schedule_code;
DROP TABLE pricing_fee_line;
DROP TABLE pricing_fee_schedule;
