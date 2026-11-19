-- Appareil de confiance d'une personne. Le code secret n'est pas stocké ici.

CREATE TABLE IF NOT EXISTS identity_appareil (
    id            UUID PRIMARY KEY,
    tenant_id     VARCHAR(64)  NOT NULL,
    party_id      UUID         NOT NULL REFERENCES identity_party (id),
    username      VARCHAR(100) NOT NULL,
    appareil_id   VARCHAR(80)  NOT NULL,
    nom           VARCHAR(120) NOT NULL,
    plateforme    VARCHAR(16)  NOT NULL,
    statut        VARCHAR(16)  NOT NULL,
    session_id    VARCHAR(80),
    created_at    TIMESTAMPTZ  NOT NULL DEFAULT NOW(),
    updated_at    TIMESTAMPTZ  NOT NULL DEFAULT NOW(),
    last_seen_at  TIMESTAMPTZ  NOT NULL DEFAULT NOW(),
    CONSTRAINT uq_identity_appareil UNIQUE (tenant_id, appareil_id)
);

CREATE INDEX IF NOT EXISTS idx_identity_appareil_party ON identity_appareil (party_id);
