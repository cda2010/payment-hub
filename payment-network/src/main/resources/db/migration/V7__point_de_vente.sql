-- Point de vente d'une unité, ses utilisateurs (un admin, des employés) et la séance de caisse.
-- L'argent reste dans les alias : CPTEOPERATION est une étiquette, pas une table de comptes.

CREATE TABLE unite_utilisateur (
    id           UUID PRIMARY KEY,
    unite_id     UUID         NOT NULL REFERENCES unite_organisation (id),
    identifiant  VARCHAR(80)  NOT NULL,
    nom          VARCHAR(150) NOT NULL,
    role         VARCHAR(32)  NOT NULL,
    statut       VARCHAR(10)  NOT NULL CHECK (statut IN ('ACTIF', 'INACTIF')),
    created_at   TIMESTAMPTZ  NOT NULL DEFAULT NOW(),
    created_by   VARCHAR(100) NOT NULL,
    CONSTRAINT uq_unite_utilisateur UNIQUE (unite_id, identifiant)
);

CREATE UNIQUE INDEX uq_unite_admin_actif
    ON unite_utilisateur (unite_id)
    WHERE role = 'ADMIN' AND statut = 'ACTIF';

CREATE TABLE point_vente (
    id          UUID PRIMARY KEY,
    unite_id    UUID         NOT NULL REFERENCES unite_organisation (id),
    code        VARCHAR(40)  NOT NULL,
    libelle     VARCHAR(150) NOT NULL,
    statut      VARCHAR(10)  NOT NULL CHECK (statut IN ('ACTIF', 'INACTIF')),
    created_at  TIMESTAMPTZ  NOT NULL DEFAULT NOW(),
    created_by  VARCHAR(100) NOT NULL,
    updated_at  TIMESTAMPTZ  NOT NULL DEFAULT NOW(),
    updated_by  VARCHAR(100) NOT NULL,
    CONSTRAINT uq_point_vente_code UNIQUE (code)
);

CREATE INDEX idx_point_vente_unite ON point_vente (unite_id);

CREATE TABLE caisse (
    id               UUID PRIMARY KEY,
    point_vente_id   UUID         NOT NULL REFERENCES point_vente (id),
    utilisateur_id   UUID         NOT NULL REFERENCES unite_utilisateur (id),
    statut           VARCHAR(10)  NOT NULL CHECK (statut IN ('OUVERTE', 'FERMEE')),
    ouverte_le       TIMESTAMPTZ  NOT NULL,
    fermee_le        TIMESTAMPTZ,
    ouverte_par      VARCHAR(100) NOT NULL,
    fermee_par       VARCHAR(100)
);

CREATE UNIQUE INDEX uq_caisse_ouverte
    ON caisse (point_vente_id)
    WHERE statut = 'OUVERTE';

CREATE UNIQUE INDEX uq_caissier_occupe
    ON caisse (utilisateur_id)
    WHERE statut = 'OUVERTE';

CREATE TABLE mouvement_alias (
    id                UUID PRIMARY KEY,
    unite_id          UUID           NOT NULL REFERENCES unite_organisation (id),
    point_vente_id    UUID           NOT NULL REFERENCES point_vente (id),
    utilisateur_id    UUID           NOT NULL REFERENCES unite_utilisateur (id),
    sens              VARCHAR(20)    NOT NULL CHECK (sens IN ('APPROVISIONNEMENT', 'REMONTEE')),
    alias_source_id   UUID           NOT NULL,
    alias_cible_id    UUID           NOT NULL,
    etiquette_source  VARCHAR(40)    NOT NULL,
    etiquette_cible   VARCHAR(40)    NOT NULL,
    montant           NUMERIC(18, 2) NOT NULL CHECK (montant > 0),
    devise            VARCHAR(8)     NOT NULL,
    cree_le           TIMESTAMPTZ    NOT NULL DEFAULT NOW(),
    cree_par          VARCHAR(100)   NOT NULL
);

CREATE INDEX idx_mouvement_point ON mouvement_alias (point_vente_id, cree_le);
