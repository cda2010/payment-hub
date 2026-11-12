-- Lignes / tranches de montant par règle (comme les tranches tarifaires).

CREATE TABLE commission_regle_ligne (
    id            UUID PRIMARY KEY,
    regle_id      UUID           NOT NULL REFERENCES commission_regle (id) ON DELETE CASCADE,
    ordre         INT            NOT NULL DEFAULT 1,
    montant_min   NUMERIC(18, 4) NOT NULL DEFAULT 0,
    montant_max   NUMERIC(18, 4),
    params_json   TEXT           NOT NULL DEFAULT '{}',
    CONSTRAINT uq_commission_regle_ligne_ordre UNIQUE (regle_id, ordre)
);

CREATE INDEX idx_commission_regle_ligne_regle ON commission_regle_ligne (regle_id);
