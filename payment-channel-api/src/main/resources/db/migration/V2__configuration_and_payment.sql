-- Lot 2-7: configuration + exécution

CREATE TABLE payment_provider (
    id                  UUID PRIMARY KEY,
    code                VARCHAR(50) NOT NULL UNIQUE,
    libelle             VARCHAR(150) NOT NULL,
    description         TEXT,
    logo_url            VARCHAR(500),
    actif               BOOLEAN NOT NULL DEFAULT TRUE,
    ordre_affichage     INT NOT NULL DEFAULT 0,
    mode_execution      VARCHAR(30) NOT NULL,
    created_at          TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW(),
    updated_at          TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW()
);

CREATE TABLE provider_environment (
    id                  UUID PRIMARY KEY,
    provider_id         UUID NOT NULL REFERENCES payment_provider(id) ON DELETE CASCADE,
    code                VARCHAR(30) NOT NULL,
    base_url            VARCHAR(500) NOT NULL,
    timeout_seconds     INT NOT NULL DEFAULT 30,
    actif               BOOLEAN NOT NULL DEFAULT TRUE,
    created_at          TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW(),
    updated_at          TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW(),
    UNIQUE (provider_id, code)
);

CREATE TABLE provider_authentication (
    id                  UUID PRIMARY KEY,
    provider_id         UUID NOT NULL REFERENCES payment_provider(id) ON DELETE CASCADE,
    environment_id      UUID NOT NULL REFERENCES provider_environment(id) ON DELETE CASCADE,
    auth_type           VARCHAR(40) NOT NULL,
    client_id           VARCHAR(255),
    client_secret_enc   TEXT,
    api_key_enc         TEXT,
    token_url           VARCHAR(500),
    username_enc        TEXT,
    password_enc        TEXT,
    custom_header_name  VARCHAR(100),
    custom_header_value_enc TEXT,
    extra_config_json   TEXT,
    created_at          TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW(),
    updated_at          TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW(),
    UNIQUE (provider_id, environment_id)
);

CREATE TABLE provider_operation (
    id                  UUID PRIMARY KEY,
    provider_id         UUID NOT NULL REFERENCES payment_provider(id) ON DELETE CASCADE,
    code                VARCHAR(50) NOT NULL,
    nom                 VARCHAR(150) NOT NULL,
    description         TEXT,
    idempotent          BOOLEAN NOT NULL DEFAULT FALSE,
    actif               BOOLEAN NOT NULL DEFAULT TRUE,
    created_at          TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW(),
    UNIQUE (provider_id, code)
);

CREATE TABLE provider_api (
    id                  UUID PRIMARY KEY,
    provider_id         UUID NOT NULL REFERENCES payment_provider(id) ON DELETE CASCADE,
    environment_id      UUID REFERENCES provider_environment(id) ON DELETE SET NULL,
    operation_id        UUID NOT NULL REFERENCES provider_operation(id) ON DELETE CASCADE,
    code                VARCHAR(50) NOT NULL,
    nom                 VARCHAR(150) NOT NULL,
    http_method         VARCHAR(10) NOT NULL,
    path                VARCHAR(500) NOT NULL,
    content_type        VARCHAR(100) DEFAULT 'application/json',
    timeout_seconds     INT NOT NULL DEFAULT 30,
    request_mapping_json TEXT,
    response_mapping_json TEXT,
    retry_enabled       BOOLEAN NOT NULL DEFAULT FALSE,
    max_attempts        INT NOT NULL DEFAULT 1,
    retry_delay_ms      INT NOT NULL DEFAULT 2000,
    backoff_multiplier  DOUBLE PRECISION NOT NULL DEFAULT 1.0,
    retry_on            VARCHAR(255),
    actif               BOOLEAN NOT NULL DEFAULT TRUE,
    created_at          TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW(),
    UNIQUE (provider_id, code)
);

CREATE TABLE payment_flow (
    id                  UUID PRIMARY KEY,
    provider_id         UUID NOT NULL REFERENCES payment_provider(id) ON DELETE CASCADE,
    code                VARCHAR(50) NOT NULL,
    nom                 VARCHAR(150) NOT NULL,
    description         TEXT,
    actif               BOOLEAN NOT NULL DEFAULT TRUE,
    created_at          TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW(),
    UNIQUE (provider_id, code)
);

CREATE TABLE payment_flow_step (
    id                  UUID PRIMARY KEY,
    flow_id             UUID NOT NULL REFERENCES payment_flow(id) ON DELETE CASCADE,
    code                VARCHAR(50) NOT NULL,
    step_type           VARCHAR(30) NOT NULL,
    ordre               INT NOT NULL,
    api_operation_code  VARCHAR(50),
    next_step_success   VARCHAR(50),
    next_step_failure   VARCHAR(50),
    configuration_json  TEXT,
    actif               BOOLEAN NOT NULL DEFAULT TRUE,
    UNIQUE (flow_id, code)
);

CREATE TABLE provider_status_mapping (
    id                  UUID PRIMARY KEY,
    provider_id         UUID NOT NULL REFERENCES payment_provider(id) ON DELETE CASCADE,
    provider_status     VARCHAR(50) NOT NULL,
    gateway_status      VARCHAR(30) NOT NULL,
    message             VARCHAR(255),
    UNIQUE (provider_id, provider_status)
);

CREATE TABLE provider_otp_config (
    id                  UUID PRIMARY KEY,
    provider_id         UUID NOT NULL UNIQUE REFERENCES payment_provider(id) ON DELETE CASCADE,
    otp_required        BOOLEAN NOT NULL DEFAULT FALSE,
    length              INT NOT NULL DEFAULT 6,
    validity_seconds    INT NOT NULL DEFAULT 120,
    max_attempts        INT NOT NULL DEFAULT 3,
    resend_allowed      BOOLEAN NOT NULL DEFAULT TRUE,
    resend_delay_seconds INT NOT NULL DEFAULT 30,
    generate_operation  VARCHAR(50),
    validate_operation  VARCHAR(50)
);

CREATE TABLE provider_webhook_config (
    id                  UUID PRIMARY KEY,
    provider_id         UUID NOT NULL REFERENCES payment_provider(id) ON DELETE CASCADE,
    path_suffix         VARCHAR(100) NOT NULL,
    auth_mode           VARCHAR(40) NOT NULL DEFAULT 'NONE',
    hmac_secret_enc     TEXT,
    api_key_enc         TEXT,
    allowed_ips         TEXT,
    actif               BOOLEAN NOT NULL DEFAULT TRUE,
    UNIQUE (provider_id, path_suffix)
);

CREATE TABLE payment_transaction (
    id                  UUID PRIMARY KEY,
    payment_id          VARCHAR(40) NOT NULL UNIQUE,
    provider_code       VARCHAR(50) NOT NULL,
    merchant_reference  VARCHAR(100),
    customer_reference  VARCHAR(100),
    phone_number        VARCHAR(30),
    amount              NUMERIC(18, 2) NOT NULL,
    currency            VARCHAR(10) NOT NULL DEFAULT 'XOF',
    status              VARCHAR(40) NOT NULL,
    current_step        VARCHAR(50),
    provider_reference  VARCHAR(100),
    idempotency_key     VARCHAR(100),
    context_json        TEXT,
    error_code          VARCHAR(50),
    error_message       VARCHAR(500),
    created_at          TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW(),
    updated_at          TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW(),
    completed_at        TIMESTAMP WITH TIME ZONE
);

CREATE UNIQUE INDEX uq_payment_idempotency ON payment_transaction(idempotency_key);
CREATE INDEX idx_payment_status ON payment_transaction(status);
CREATE INDEX idx_payment_provider ON payment_transaction(provider_code);
CREATE INDEX idx_payment_phone ON payment_transaction(phone_number);
CREATE INDEX idx_payment_customer_ref ON payment_transaction(customer_reference);
CREATE INDEX idx_payment_provider_ref ON payment_transaction(provider_reference);
CREATE INDEX idx_payment_created ON payment_transaction(created_at DESC);

CREATE TABLE payment_execution (
    id                  UUID PRIMARY KEY,
    payment_id          VARCHAR(40) NOT NULL,
    step_code           VARCHAR(50),
    operation_code      VARCHAR(50),
    provider_code       VARCHAR(50),
    request_timestamp   TIMESTAMP WITH TIME ZONE,
    response_timestamp  TIMESTAMP WITH TIME ZONE,
    http_status         INT,
    request_payload     TEXT,
    response_payload    TEXT,
    provider_code_status VARCHAR(50),
    provider_message    VARCHAR(500),
    gateway_status      VARCHAR(40),
    execution_duration_ms BIGINT,
    success             BOOLEAN,
    created_at          TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_execution_payment ON payment_execution(payment_id);

CREATE TABLE payment_event (
    id                  UUID PRIMARY KEY,
    payment_id          VARCHAR(40) NOT NULL,
    event_type          VARCHAR(50) NOT NULL,
    message             VARCHAR(500),
    details_json        TEXT,
    created_at          TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_event_payment ON payment_event(payment_id, created_at);

CREATE TABLE payment_callback (
    id                  UUID PRIMARY KEY,
    provider_code       VARCHAR(50) NOT NULL,
    payment_id          VARCHAR(40),
    external_event_id   VARCHAR(100),
    payload             TEXT,
    processed           BOOLEAN NOT NULL DEFAULT FALSE,
    created_at          TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW(),
    processed_at        TIMESTAMP WITH TIME ZONE
);

CREATE UNIQUE INDEX uq_callback_event ON payment_callback(provider_code, external_event_id);
CREATE INDEX idx_callback_payment ON payment_callback(payment_id);
