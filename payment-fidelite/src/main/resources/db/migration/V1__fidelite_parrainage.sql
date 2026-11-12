CREATE TABLE programme (
    id                UUID PRIMARY KEY,
    tenant_id         VARCHAR(64)  NOT NULL,
    nom               VARCHAR(160) NOT NULL,
    portee            VARCHAR(16)  NOT NULL,
    unite_id          UUID,
    unite_nom         VARCHAR(160),
    points_par_mille  INT          NOT NULL,
    points_parrain    INT          NOT NULL,
    points_filleul    INT          NOT NULL,
    statut            VARCHAR(16)  NOT NULL,
    created_at        TIMESTAMPTZ  NOT NULL
);

CREATE INDEX programme_tenant_idx ON programme (tenant_id, statut);
CREATE INDEX programme_unite_idx ON programme (unite_id);

CREATE TABLE programme_profil (
    programme_id  UUID        NOT NULL REFERENCES programme (id),
    profile_code  VARCHAR(64) NOT NULL,
    PRIMARY KEY (programme_id, profile_code)
);

CREATE TABLE adhesion (
    id            UUID PRIMARY KEY,
    programme_id  UUID        NOT NULL REFERENCES programme (id),
    party_id      UUID        NOT NULL,
    created_at    TIMESTAMPTZ NOT NULL,
    UNIQUE (programme_id, party_id)
);

CREATE INDEX adhesion_party_idx ON adhesion (party_id);

CREATE TABLE parrainage (
    id                 UUID PRIMARY KEY,
    programme_id       UUID        NOT NULL REFERENCES programme (id),
    parrain_party_id   UUID        NOT NULL,
    filleul_party_id   UUID,
    code               VARCHAR(16) NOT NULL UNIQUE,
    statut             VARCHAR(16) NOT NULL,
    created_at         TIMESTAMPTZ NOT NULL,
    used_at            TIMESTAMPTZ
);

CREATE TABLE mouvement_points (
    id            UUID PRIMARY KEY,
    programme_id  UUID        NOT NULL REFERENCES programme (id),
    party_id      UUID        NOT NULL,
    points        INT         NOT NULL,
    motif         VARCHAR(160) NOT NULL,
    created_at    TIMESTAMPTZ NOT NULL
);

CREATE INDEX mouvement_party_idx ON mouvement_points (programme_id, party_id);
