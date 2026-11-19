-- Séance de caisse : numéro d'ouverture, fonds physique et solde du compte d'opération.
-- Le compte d'opération reste l'alias étiqueté CPTEOPERATION. Son solde de situation est ici.

ALTER TABLE caisse DROP CONSTRAINT IF EXISTS caisse_statut_check;
ALTER TABLE caisse ALTER COLUMN statut TYPE VARCHAR(16);

ALTER TABLE caisse ADD COLUMN numero INTEGER;
ALTER TABLE caisse ADD COLUMN fonds_attendu NUMERIC(18, 2) NOT NULL DEFAULT 0;
ALTER TABLE caisse ADD COLUMN fonds_declare NUMERIC(18, 2) NOT NULL DEFAULT 0;
ALTER TABLE caisse ADD COLUMN ecart_ouverture NUMERIC(18, 2) NOT NULL DEFAULT 0;
ALTER TABLE caisse ADD COLUMN solde_compte_ouverture NUMERIC(18, 2) NOT NULL DEFAULT 0;
ALTER TABLE caisse ADD COLUMN fonds_physique_cloture NUMERIC(18, 2);
ALTER TABLE caisse ADD COLUMN solde_compte_fermeture NUMERIC(18, 2);
ALTER TABLE caisse ADD COLUMN ecart_fermeture NUMERIC(18, 2);
ALTER TABLE caisse ADD COLUMN depots NUMERIC(18, 2) NOT NULL DEFAULT 0;
ALTER TABLE caisse ADD COLUMN retraits NUMERIC(18, 2) NOT NULL DEFAULT 0;
ALTER TABLE caisse ADD COLUMN motif VARCHAR(200);
ALTER TABLE caisse ADD COLUMN regularisee_le TIMESTAMPTZ;
ALTER TABLE caisse ADD COLUMN regularisee_par VARCHAR(100);

WITH nums AS (
    SELECT id, ROW_NUMBER() OVER (PARTITION BY point_vente_id ORDER BY ouverte_le, id) AS n
    FROM caisse
)
UPDATE caisse
SET numero = nums.n
FROM nums
WHERE caisse.id = nums.id;

ALTER TABLE caisse ALTER COLUMN numero SET NOT NULL;

ALTER TABLE caisse ADD CONSTRAINT caisse_statut_check
    CHECK (statut IN ('OUVERTE', 'FERMEE', 'EN_VALIDATION', 'A_REGULARISER', 'REGULARISEE', 'ANNULEE'));

CREATE UNIQUE INDEX uq_caisse_numero ON caisse (point_vente_id, numero);

CREATE TABLE position_guichet (
    point_vente_id UUID PRIMARY KEY REFERENCES point_vente (id),
    solde_compte   NUMERIC(18, 2) NOT NULL DEFAULT 0,
    fonds_attendu  NUMERIC(18, 2) NOT NULL DEFAULT 0,
    devise         VARCHAR(8)     NOT NULL DEFAULT 'XOF'
);

INSERT INTO position_guichet (point_vente_id, solde_compte, fonds_attendu, devise)
SELECT point_vente_id,
       COALESCE(SUM(CASE
           WHEN sens = 'APPROVISIONNEMENT' THEN montant
           WHEN sens = 'REMONTEE' THEN -montant
           ELSE 0 END), 0),
       0,
       'XOF'
FROM mouvement_alias
GROUP BY point_vente_id;

CREATE TABLE operation_caisse (
    id         UUID PRIMARY KEY,
    caisse_id  UUID           NOT NULL REFERENCES caisse (id),
    sens       VARCHAR(10)    NOT NULL CHECK (sens IN ('DEPOT', 'RETRAIT')),
    montant    NUMERIC(18, 2) NOT NULL CHECK (montant > 0),
    reference  VARCHAR(80),
    cree_le    TIMESTAMPTZ    NOT NULL DEFAULT NOW(),
    cree_par   VARCHAR(100)   NOT NULL
);

CREATE INDEX idx_operation_caisse ON operation_caisse (caisse_id, cree_le);
