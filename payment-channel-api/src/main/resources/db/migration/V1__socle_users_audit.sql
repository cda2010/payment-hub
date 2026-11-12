-- Lot 1: socle — utilisateurs, rôles, audit

CREATE TABLE gateway_user (
    id              UUID PRIMARY KEY,
    username        VARCHAR(100) NOT NULL UNIQUE,
    email           VARCHAR(255) NOT NULL UNIQUE,
    password_hash   VARCHAR(255) NOT NULL,
    full_name       VARCHAR(255),
    enabled         BOOLEAN NOT NULL DEFAULT TRUE,
    locked          BOOLEAN NOT NULL DEFAULT FALSE,
    created_at      TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW(),
    updated_at      TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW(),
    last_login_at   TIMESTAMP WITH TIME ZONE
);

CREATE TABLE gateway_role (
    id          UUID PRIMARY KEY,
    code        VARCHAR(50) NOT NULL UNIQUE,
    description VARCHAR(255)
);

CREATE TABLE gateway_user_role (
    user_id UUID NOT NULL REFERENCES gateway_user(id) ON DELETE CASCADE,
    role_id UUID NOT NULL REFERENCES gateway_role(id) ON DELETE CASCADE,
    PRIMARY KEY (user_id, role_id)
);

CREATE TABLE audit_log (
    id              UUID PRIMARY KEY,
    actor_username  VARCHAR(100),
    action          VARCHAR(100) NOT NULL,
    resource_type   VARCHAR(100),
    resource_id     VARCHAR(100),
    details         TEXT,
    ip_address      VARCHAR(64),
    trace_id        VARCHAR(64),
    created_at      TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_audit_log_created_at ON audit_log(created_at DESC);
CREATE INDEX idx_audit_log_resource ON audit_log(resource_type, resource_id);
CREATE INDEX idx_audit_log_actor ON audit_log(actor_username);

INSERT INTO gateway_role (id, code, description) VALUES
    ('11111111-1111-1111-1111-111111111001', 'ADMIN', 'Administrateur complet'),
    ('11111111-1111-1111-1111-111111111002', 'CONFIG_MANAGER', 'Gestion de la configuration fournisseurs'),
    ('11111111-1111-1111-1111-111111111003', 'OPERATOR', 'Opérateur paiements'),
    ('11111111-1111-1111-1111-111111111004', 'SUPPORT', 'Support / consultation transactions'),
    ('11111111-1111-1111-1111-111111111005', 'AUDITOR', 'Lecture seule audit et supervision');
