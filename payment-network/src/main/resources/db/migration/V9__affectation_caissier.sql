-- Un caissier est affecté à un point de vente pour une période.
-- La date de fin, si elle est posée, retire l'affectation le lendemain (échéance).
-- La sortie manuelle enregistre un motif.

CREATE TABLE affectation_caissier (
    id               UUID PRIMARY KEY,
    point_vente_id   UUID         NOT NULL REFERENCES point_vente (id),
    utilisateur_id   UUID         NOT NULL REFERENCES unite_utilisateur (id),
    debut            DATE         NOT NULL,
    fin              DATE,
    motif            VARCHAR(200),
    statut           VARCHAR(10)  NOT NULL CHECK (statut IN ('ACTIF', 'CLOTUREE')),
    cree_le          TIMESTAMPTZ  NOT NULL DEFAULT NOW(),
    cree_par         VARCHAR(100) NOT NULL,
    cloturee_le      TIMESTAMPTZ,
    cloturee_par     VARCHAR(100),
    CONSTRAINT ck_affectation_periode CHECK (fin IS NULL OR fin >= debut)
);

CREATE UNIQUE INDEX uq_affectation_active
    ON affectation_caissier (point_vente_id, utilisateur_id)
    WHERE statut = 'ACTIF';

CREATE INDEX idx_affectation_point ON affectation_caissier (point_vente_id, debut);
