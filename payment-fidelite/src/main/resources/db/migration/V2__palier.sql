CREATE TABLE palier (
    id               UUID PRIMARY KEY,
    programme_id     UUID           NOT NULL REFERENCES programme (id),
    code             VARCHAR(32)    NOT NULL,
    label            VARCHAR(80)    NOT NULL,
    seuil_points     INT            NOT NULL,
    points_par_mille NUMERIC(8, 2)  NOT NULL,
    UNIQUE (programme_id, code),
    UNIQUE (programme_id, seuil_points)
);

CREATE INDEX palier_programme_idx ON palier (programme_id, seuil_points);

CREATE TABLE palier_avantage (
    palier_id  UUID         NOT NULL REFERENCES palier (id),
    avantage   VARCHAR(160) NOT NULL,
    position   INT          NOT NULL,
    PRIMARY KEY (palier_id, position)
);
