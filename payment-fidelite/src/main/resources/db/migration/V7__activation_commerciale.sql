-- Durée de vie des points, campagnes, missions, segments, récompenses étendues,
-- conversion, coalition marchande et notifications. Les points déjà en solde
-- deviennent un lot sans échéance : ils ne sont pas expirés rétroactivement.

ALTER TABLE programme ADD COLUMN validite_mois INT;
ALTER TABLE programme ADD CONSTRAINT programme_validite_mois_ck
    CHECK (validite_mois IS NULL OR validite_mois IN (3, 6, 12));

ALTER TABLE loyalty_rule_version ADD COLUMN starts_at TIMESTAMPTZ;
ALTER TABLE loyalty_rule_version ADD COLUMN ends_at TIMESTAMPTZ;

ALTER TABLE recompense ADD COLUMN type_recompense VARCHAR(30) NOT NULL DEFAULT 'CATALOGUE';
ALTER TABLE recompense ADD COLUMN stock INT;
ALTER TABLE recompense ADD COLUMN stock_restant INT;
ALTER TABLE recompense ADD COLUMN valide_du TIMESTAMPTZ;
ALTER TABLE recompense ADD COLUMN valide_au TIMESTAMPTZ;
ALTER TABLE recompense ADD COLUMN marchand_id UUID;
ALTER TABLE recompense ADD COLUMN taux_cashback NUMERIC(12, 4);
ALTER TABLE recompense ADD COLUMN financeur_id UUID;

ALTER TABLE echange ADD COLUMN valeur_xof NUMERIC(19, 2);

CREATE TABLE point_lot (
    id                 UUID PRIMARY KEY,
    tenant_id          VARCHAR(64)  NOT NULL,
    account_id         UUID         NOT NULL REFERENCES loyalty_account (id),
    original_points    BIGINT       NOT NULL,
    remaining_points   BIGINT       NOT NULL,
    earned_at          TIMESTAMPTZ  NOT NULL,
    expires_at         TIMESTAMPTZ,
    business_reference VARCHAR(150) NOT NULL,
    CONSTRAINT point_lot_points_ck CHECK (
        original_points > 0 AND remaining_points >= 0 AND remaining_points <= original_points
    ),
    CONSTRAINT point_lot_reference_uk UNIQUE (tenant_id, business_reference)
);

CREATE INDEX point_lot_fefo_idx ON point_lot (account_id, expires_at, earned_at);
CREATE INDEX point_lot_expiration_idx ON point_lot (expires_at) WHERE remaining_points > 0;

INSERT INTO point_lot (
    id, tenant_id, account_id, original_points, remaining_points, earned_at, expires_at, business_reference
)
SELECT gen_random_uuid(), tenant_id, id, balance, balance, now(), NULL, 'OUVERTURE:' || id::text
FROM loyalty_account
WHERE balance > 0;

CREATE TABLE campagne (
    id                  UUID PRIMARY KEY,
    tenant_id           VARCHAR(64)    NOT NULL,
    programme_id        UUID           NOT NULL REFERENCES programme (id),
    code                VARCHAR(50)    NOT NULL,
    nom                 VARCHAR(160)   NOT NULL,
    debut               TIMESTAMPTZ    NOT NULL,
    fin                 TIMESTAMPTZ    NOT NULL,
    multiplicateur      NUMERIC(8, 2)  NOT NULL DEFAULT 1,
    bonus_fixe          BIGINT         NOT NULL DEFAULT 0,
    budget_points       BIGINT,
    points_attribues    BIGINT         NOT NULL DEFAULT 0,
    priorite            INT            NOT NULL DEFAULT 0,
    groupe              VARCHAR(40),
    weekend_seulement   BOOLEAN        NOT NULL DEFAULT FALSE,
    profils             VARCHAR(400),
    regions             VARCHAR(400),
    unite_id            UUID,
    produits            VARCHAR(400),
    services            VARCHAR(400),
    canaux              VARCHAR(200),
    financeur_id        UUID,
    segment_requis      VARCHAR(40),
    actif               BOOLEAN        NOT NULL DEFAULT TRUE,
    CONSTRAINT campagne_code_uk UNIQUE (tenant_id, code),
    CONSTRAINT campagne_dates_ck CHECK (fin > debut),
    CONSTRAINT campagne_mult_ck CHECK (multiplicateur > 0),
    CONSTRAINT campagne_budget_ck CHECK (budget_points IS NULL OR budget_points >= 0),
    CONSTRAINT campagne_attribues_ck CHECK (points_attribues >= 0)
);

CREATE TABLE mission (
    id              UUID PRIMARY KEY,
    tenant_id       VARCHAR(64)    NOT NULL,
    programme_id    UUID           NOT NULL REFERENCES programme (id),
    code            VARCHAR(50)    NOT NULL,
    nom             VARCHAR(160)   NOT NULL,
    metrique        VARCHAR(20)    NOT NULL,
    cible           NUMERIC(19, 2) NOT NULL,
    fenetre_jours   INT            NOT NULL,
    filtre_produit  VARCHAR(80),
    filtre_service  VARCHAR(80),
    filtre_canal    VARCHAR(80),
    points          BIGINT         NOT NULL,
    actif           BOOLEAN        NOT NULL DEFAULT TRUE,
    CONSTRAINT mission_code_uk UNIQUE (tenant_id, code),
    CONSTRAINT mission_metrique_ck CHECK (metrique IN ('COUNT', 'AMOUNT', 'DISTINCT', 'PREMIER')),
    CONSTRAINT mission_cible_ck CHECK (cible > 0),
    CONSTRAINT mission_fenetre_ck CHECK (fenetre_jours >= 0),
    CONSTRAINT mission_points_ck CHECK (points > 0)
);

CREATE TABLE mission_progres (
    id             UUID PRIMARY KEY,
    mission_id     UUID           NOT NULL REFERENCES mission (id),
    party_id       UUID           NOT NULL,
    fenetre_debut  DATE           NOT NULL,
    compteur       INT            NOT NULL DEFAULT 0,
    montant        NUMERIC(19, 2) NOT NULL DEFAULT 0,
    services       VARCHAR(400),
    accomplie      BOOLEAN        NOT NULL DEFAULT FALSE,
    CONSTRAINT mission_progres_uk UNIQUE (mission_id, party_id, fenetre_debut)
);

CREATE TABLE segment_client (
    id         UUID PRIMARY KEY,
    tenant_id  VARCHAR(64) NOT NULL,
    party_id   UUID        NOT NULL,
    code       VARCHAR(40) NOT NULL,
    calcule_at TIMESTAMPTZ NOT NULL,
    CONSTRAINT segment_client_uk UNIQUE (tenant_id, party_id, code)
);

CREATE TABLE consentement (
    id         UUID PRIMARY KEY,
    tenant_id  VARCHAR(64) NOT NULL,
    party_id   UUID        NOT NULL,
    push       BOOLEAN     NOT NULL DEFAULT FALSE,
    sms        BOOLEAN     NOT NULL DEFAULT FALSE,
    email      BOOLEAN     NOT NULL DEFAULT FALSE,
    whatsapp   BOOLEAN     NOT NULL DEFAULT FALSE,
    CONSTRAINT consentement_party_uk UNIQUE (tenant_id, party_id)
);

CREATE TABLE notification_fidelite (
    id           UUID PRIMARY KEY,
    tenant_id    VARCHAR(64)  NOT NULL,
    party_id     UUID         NOT NULL,
    programme_id UUID,
    type         VARCHAR(40)  NOT NULL,
    canal        VARCHAR(20)  NOT NULL,
    message      VARCHAR(400) NOT NULL,
    statut       VARCHAR(20)  NOT NULL,
    dedup_key    VARCHAR(180) NOT NULL,
    created_at   TIMESTAMPTZ  NOT NULL,
    CONSTRAINT notification_fidelite_dedup_uk UNIQUE (tenant_id, dedup_key, canal)
);

CREATE TABLE plafond_points (
    id           UUID PRIMARY KEY,
    tenant_id    VARCHAR(64) NOT NULL,
    programme_id UUID        NOT NULL,
    party_id     UUID        NOT NULL,
    portee       VARCHAR(20) NOT NULL,
    periode      VARCHAR(40) NOT NULL,
    points       BIGINT      NOT NULL,
    CONSTRAINT plafond_points_uk UNIQUE (tenant_id, programme_id, party_id, portee, periode),
    CONSTRAINT plafond_points_ck CHECK (points >= 0)
);

CREATE TABLE taux_conversion (
    id                   UUID PRIMARY KEY,
    tenant_id            VARCHAR(64) NOT NULL,
    source_programme_id  UUID        NOT NULL REFERENCES programme (id),
    cible_programme_id   UUID        REFERENCES programme (id),
    points_source        INT         NOT NULL,
    points_cible         INT         NOT NULL,
    plafond_mois         BIGINT      NOT NULL,
    actif                BOOLEAN     NOT NULL DEFAULT TRUE,
    CONSTRAINT taux_conversion_ck CHECK (points_source > 0 AND points_cible > 0 AND plafond_mois > 0)
);

CREATE TABLE avantage_monetaire (
    id                 UUID PRIMARY KEY,
    tenant_id          VARCHAR(64)    NOT NULL,
    programme_id       UUID           NOT NULL,
    party_id           UUID           NOT NULL,
    points             BIGINT         NOT NULL,
    valeur_xof         NUMERIC(19, 2) NOT NULL,
    business_reference VARCHAR(150)   NOT NULL,
    created_at         TIMESTAMPTZ    NOT NULL,
    CONSTRAINT avantage_monetaire_ref_uk UNIQUE (tenant_id, business_reference)
);

CREATE TABLE coalition_membre (
    id           UUID PRIMARY KEY,
    programme_id UUID           NOT NULL REFERENCES programme (id),
    marchand_id  UUID           NOT NULL,
    part_pct     NUMERIC(6, 2)  NOT NULL,
    CONSTRAINT coalition_membre_uk UNIQUE (programme_id, marchand_id),
    CONSTRAINT coalition_part_ck CHECK (part_pct > 0 AND part_pct <= 100)
);

CREATE TABLE contribution (
    id                 UUID PRIMARY KEY,
    tenant_id          VARCHAR(64) NOT NULL,
    programme_id       UUID        NOT NULL,
    marchand_id        UUID,
    party_id           UUID        NOT NULL,
    sens               VARCHAR(20) NOT NULL,
    points             BIGINT      NOT NULL,
    business_reference VARCHAR(150) NOT NULL,
    created_at         TIMESTAMPTZ NOT NULL,
    CONSTRAINT contribution_sens_ck CHECK (sens IN ('EMISSION', 'USAGE')),
    CONSTRAINT contribution_points_ck CHECK (points > 0),
    CONSTRAINT contribution_ref_uk UNIQUE (tenant_id, business_reference)
);
