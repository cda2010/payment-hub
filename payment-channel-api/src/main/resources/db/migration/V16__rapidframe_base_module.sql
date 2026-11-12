-- RapidFrame meta schema (admin only) — tables alignées sur com.rapidframe.meta.*

CREATE SCHEMA IF NOT EXISTS base_module;

CREATE TABLE IF NOT EXISTS base_module.rf_meta_form (
    id              BIGSERIAL PRIMARY KEY,
    name            VARCHAR(255) NOT NULL UNIQUE,
    title           VARCHAR(255),
    model           VARCHAR(500),
    module_name     VARCHAR(100),
    version         VARCHAR(50) DEFAULT '1.0.0',
    is_custom       BOOLEAN NOT NULL DEFAULT FALSE,
    extends_name    VARCHAR(255),
    on_save_behavior VARCHAR(20),
    definition_json TEXT,
    definition_xml  TEXT,
    created_on      TIMESTAMP NOT NULL,
    updated_on      TIMESTAMP,
    created_by      VARCHAR(100),
    updated_by      VARCHAR(100)
);

CREATE TABLE IF NOT EXISTS base_module.rf_meta_grid (
    id              BIGSERIAL PRIMARY KEY,
    name            VARCHAR(255) NOT NULL UNIQUE,
    title           VARCHAR(255),
    model           VARCHAR(500),
    module_name     VARCHAR(100),
    version         VARCHAR(50) DEFAULT '1.0.0',
    is_custom       BOOLEAN NOT NULL DEFAULT FALSE,
    extends_name    VARCHAR(255),
    definition_json TEXT,
    definition_xml  TEXT,
    created_on      TIMESTAMP NOT NULL,
    updated_on      TIMESTAMP,
    created_by      VARCHAR(100),
    updated_by      VARCHAR(100)
);

CREATE TABLE IF NOT EXISTS base_module.rf_meta_menu (
    id              BIGSERIAL PRIMARY KEY,
    name            VARCHAR(255) NOT NULL UNIQUE,
    title           VARCHAR(255),
    model           VARCHAR(500),
    module_name     VARCHAR(100),
    version         VARCHAR(50) DEFAULT '1.0.0',
    is_custom       BOOLEAN NOT NULL DEFAULT FALSE,
    extends_name    VARCHAR(255),
    parent_id       BIGINT,
    parent_name     VARCHAR(255),
    action          VARCHAR(255),
    icon            VARCHAR(100),
    order_index     INTEGER,
    is_left_menu    BOOLEAN DEFAULT TRUE,
    is_mobile_menu  BOOLEAN DEFAULT FALSE,
    is_hidden       BOOLEAN DEFAULT FALSE,
    definition_json TEXT,
    definition_xml  TEXT,
    created_on      TIMESTAMP NOT NULL,
    updated_on      TIMESTAMP,
    created_by      VARCHAR(100),
    updated_by      VARCHAR(100)
);

CREATE TABLE IF NOT EXISTS base_module.rf_meta_action_view (
    id              BIGSERIAL PRIMARY KEY,
    name            VARCHAR(255) NOT NULL UNIQUE,
    title           VARCHAR(255),
    model           VARCHAR(500),
    module_name     VARCHAR(100),
    version         VARCHAR(50) DEFAULT '1.0.0',
    is_custom       BOOLEAN NOT NULL DEFAULT FALSE,
    extends_name    VARCHAR(255),
    icon            VARCHAR(100),
    definition_json TEXT,
    definition_xml  TEXT,
    created_on      TIMESTAMP NOT NULL,
    updated_on      TIMESTAMP,
    created_by      VARCHAR(100),
    updated_by      VARCHAR(100)
);

CREATE TABLE IF NOT EXISTS base_module.rf_meta_model (
    id                  BIGSERIAL PRIMARY KEY,
    name                VARCHAR(255) NOT NULL UNIQUE,
    title               VARCHAR(255),
    model               VARCHAR(500),
    module_name         VARCHAR(100),
    version             VARCHAR(50) DEFAULT '1.0.0',
    is_custom           BOOLEAN NOT NULL DEFAULT FALSE,
    extends_name        VARCHAR(255),
    class_name          VARCHAR(500),
    table_name          VARCHAR(255),
    repo_package        VARCHAR(500),
    controller_package  VARCHAR(500),
    repository          VARCHAR(50),
    definition_json     TEXT,
    created_on          TIMESTAMP NOT NULL,
    updated_on          TIMESTAMP,
    created_by          VARCHAR(100),
    updated_by          VARCHAR(100)
);

CREATE TABLE IF NOT EXISTS base_module.rf_meta_form_extension (
    id              BIGSERIAL PRIMARY KEY,
    name            VARCHAR(255) NOT NULL UNIQUE,
    title           VARCHAR(255),
    model           VARCHAR(500),
    module_name     VARCHAR(100),
    version         VARCHAR(50) DEFAULT '1.0.0',
    is_custom       BOOLEAN NOT NULL DEFAULT FALSE,
    extends_name    VARCHAR(255),
    target_form     VARCHAR(255),
    extension_id    VARCHAR(255),
    definition_json TEXT,
    created_on      TIMESTAMP NOT NULL,
    updated_on      TIMESTAMP,
    created_by      VARCHAR(100),
    updated_by      VARCHAR(100)
);

CREATE TABLE IF NOT EXISTS base_module.rf_meta_json_record (
    id          BIGSERIAL PRIMARY KEY,
    model       VARCHAR(500) NOT NULL,
    record_id   BIGINT NOT NULL,
    attrs_json  TEXT,
    created_on  TIMESTAMP NOT NULL,
    updated_on  TIMESTAMP,
    CONSTRAINT uk_rf_meta_json_record_model_id UNIQUE (model, record_id)
);

-- Entités RF sans schema=base_module (public)
CREATE TABLE IF NOT EXISTS rf_meta_selection (
    id              BIGSERIAL PRIMARY KEY,
    name            VARCHAR(255) NOT NULL UNIQUE,
    title           VARCHAR(255),
    model           VARCHAR(500),
    module_name     VARCHAR(100),
    version         VARCHAR(50) DEFAULT '1.0.0',
    is_custom       BOOLEAN NOT NULL DEFAULT FALSE,
    extends_name    VARCHAR(255),
    definition_json TEXT,
    created_on      TIMESTAMP NOT NULL,
    updated_on      TIMESTAMP,
    created_by      VARCHAR(100),
    updated_by      VARCHAR(100)
);

CREATE TABLE IF NOT EXISTS rf_meta_action_group (
    id              BIGSERIAL PRIMARY KEY,
    name            VARCHAR(255) NOT NULL UNIQUE,
    title           VARCHAR(255),
    model           VARCHAR(500),
    module_name     VARCHAR(100),
    version         VARCHAR(50) DEFAULT '1.0.0',
    is_custom       BOOLEAN NOT NULL DEFAULT FALSE,
    extends_name    VARCHAR(255),
    definition_json TEXT,
    created_on      TIMESTAMP NOT NULL,
    updated_on      TIMESTAMP,
    created_by      VARCHAR(100),
    updated_by      VARCHAR(100)
);

CREATE TABLE IF NOT EXISTS rf_meta_action_validate (
    id              BIGSERIAL PRIMARY KEY,
    name            VARCHAR(255) NOT NULL UNIQUE,
    title           VARCHAR(255),
    model           VARCHAR(500),
    module_name     VARCHAR(100),
    version         VARCHAR(50) DEFAULT '1.0.0',
    is_custom       BOOLEAN NOT NULL DEFAULT FALSE,
    extends_name    VARCHAR(255),
    definition_json TEXT,
    created_on      TIMESTAMP NOT NULL,
    updated_on      TIMESTAMP,
    created_by      VARCHAR(100),
    updated_by      VARCHAR(100)
);
