-- Zones : de deux valeurs figées (UEMOA, CEMAC) à un référentiel administrable (code, libellé, devise, statut).
-- Les unités référencent la zone par son code, qui ne change jamais ; une zone se désactive mais ne se supprime pas.

CREATE TABLE zone (
    code        VARCHAR(10)   PRIMARY KEY CONSTRAINT ck_zone_code CHECK (code ~ '^[A-Z][A-Z0-9_]{1,9}$'),
    libelle     VARCHAR(100)  NOT NULL,
    devise      VARCHAR(3)    NOT NULL CONSTRAINT ck_zone_devise CHECK (devise ~ '^[A-Z]{3}$'),
    statut      VARCHAR(10)   NOT NULL DEFAULT 'ACTIF' CONSTRAINT ck_zone_statut CHECK (statut IN ('ACTIF', 'INACTIF')),
    version     BIGINT        NOT NULL DEFAULT 0,
    created_at  TIMESTAMPTZ   NOT NULL DEFAULT NOW(),
    created_by  VARCHAR(100)  NOT NULL,
    updated_at  TIMESTAMPTZ   NOT NULL DEFAULT NOW(),
    updated_by  VARCHAR(100)  NOT NULL
);

INSERT INTO zone (code, libelle, devise, created_by, updated_by) VALUES
    ('UEMOA', 'Union économique et monétaire ouest-africaine', 'XOF', 'flyway', 'flyway'),
    ('CEMAC', 'Communauté économique et monétaire de l''Afrique centrale', 'XAF', 'flyway', 'flyway');

ALTER TABLE unite_organisation DROP CONSTRAINT unite_organisation_zone_check;
ALTER TABLE unite_organisation ALTER COLUMN zone TYPE VARCHAR(10);
ALTER TABLE unite_organisation ADD CONSTRAINT fk_unite_zone FOREIGN KEY (zone) REFERENCES zone (code);
