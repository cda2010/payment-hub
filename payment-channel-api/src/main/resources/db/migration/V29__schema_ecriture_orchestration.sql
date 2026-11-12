-- Phase D : Schémas d'écriture Omnicanal (SchemaEcrit + dépendances)

CREATE TABLE schema_ecriture (
    id              UUID PRIMARY KEY,
    nom_schema      VARCHAR(150) NOT NULL,
    service_type    VARCHAR(40)  NOT NULL,
    pattern_code    VARCHAR(64)  NOT NULL,
    workflow_name   VARCHAR(128),
    workflow_version INT         NOT NULL DEFAULT 1,
    engine          VARCHAR(32)  NOT NULL DEFAULT 'flowable',
    error_policy_code VARCHAR(64),
    actif           BOOLEAN      NOT NULL DEFAULT TRUE,
    created_at      TIMESTAMPTZ  NOT NULL DEFAULT NOW(),
    CONSTRAINT uk_schema_ecriture_service_pattern UNIQUE (service_type, pattern_code)
);

CREATE TABLE ligne_schema_ecriture (
    id                  UUID PRIMARY KEY,
    schema_ecriture_id  UUID         NOT NULL REFERENCES schema_ecriture(id) ON DELETE CASCADE,
    ordre               INT          NOT NULL,
    step_id             VARCHAR(128),
    type_methode        VARCHAR(64)  NOT NULL,
    api_a_utiliser      VARCHAR(32)  NOT NULL,
    agence_imputer      VARCHAR(32),
    execution_mode      VARCHAR(32)  DEFAULT 'SYNC',
    timeout_policy      VARCHAR(64),
    timeout_duration    VARCHAR(64),
    retry_policy        VARCHAR(64),
    retry_max_attempts  INT,
    idempotency_policy  VARCHAR(64),
    compensation_policy VARCHAR(64),
    compensation_mode   VARCHAR(48),
    error_policy_code   VARCHAR(64),
    blocking            BOOLEAN,
    criticality         VARCHAR(32),
    created_at          TIMESTAMPTZ  NOT NULL DEFAULT NOW(),
    CONSTRAINT uk_ligne_schema_ordre UNIQUE (schema_ecriture_id, ordre)
);

CREATE INDEX idx_ligne_schema_ecriture_schema ON ligne_schema_ecriture(schema_ecriture_id);
CREATE INDEX idx_ligne_schema_ecriture_step ON ligne_schema_ecriture(schema_ecriture_id, step_id);

CREATE TABLE parametre_schema (
    id                      UUID PRIMARY KEY,
    ligne_schema_ecriture_id UUID        NOT NULL REFERENCES ligne_schema_ecriture(id) ON DELETE CASCADE,
    code_parametre          VARCHAR(100) NOT NULL,
    code_parametre_methode  VARCHAR(100),
    nom_externe             VARCHAR(100),
    type_parametre          VARCHAR(50),
    categorie               VARCHAR(50)  NOT NULL DEFAULT 'USER',
    valeur_defaut           VARCHAR(500),
    created_at              TIMESTAMPTZ  NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_parametre_schema_ligne ON parametre_schema(ligne_schema_ecriture_id);

CREATE TABLE interface_utilisateur (
    id                      UUID PRIMARY KEY,
    provider_id             UUID         NOT NULL REFERENCES payment_provider(id) ON DELETE CASCADE,
    code_fse                VARCHAR(20)  NOT NULL,
    service_type            VARCHAR(40)  NOT NULL,
    type_methode            VARCHAR(64)  NOT NULL,
    type_envoi              VARCHAR(20)  NOT NULL DEFAULT 'REQUEST',
    provider_operation_id   UUID         REFERENCES provider_operation(id),
    provider_api_id         UUID         REFERENCES provider_api(id),
    code_interface          VARCHAR(50),
    nom_interface           VARCHAR(150),
    actif                   BOOLEAN      NOT NULL DEFAULT TRUE,
    created_at              TIMESTAMPTZ  NOT NULL DEFAULT NOW(),
    CONSTRAINT uk_interface_utilisateur UNIQUE (code_fse, service_type, type_methode, type_envoi)
);

CREATE INDEX idx_interface_utilisateur_provider ON interface_utilisateur(provider_id);
CREATE INDEX idx_interface_utilisateur_lookup ON interface_utilisateur(code_fse, type_methode, type_envoi);
