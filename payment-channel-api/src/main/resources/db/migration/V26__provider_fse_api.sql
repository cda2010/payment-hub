-- Api Omnicanal (host/auth) — parent des Méthodes.
-- Distinct de provider_api qui reste l'endpoint HTTP (= Methode.nomMethode).

CREATE TABLE provider_fse_api (
    id                  UUID PRIMARY KEY,
    provider_id         UUID NOT NULL REFERENCES payment_provider(id) ON DELETE CASCADE,
    environment_id      UUID REFERENCES provider_environment(id) ON DELETE SET NULL,
    code                VARCHAR(50) NOT NULL,
    nom                 VARCHAR(150) NOT NULL,
    type_api            VARCHAR(20) NOT NULL DEFAULT 'REST',
    base_url            VARCHAR(500) NOT NULL,
    host                VARCHAR(255),
    port                VARCHAR(20),
    use_ssl             BOOLEAN NOT NULL DEFAULT FALSE,
    use_proxy           BOOLEAN NOT NULL DEFAULT FALSE,
    actif               BOOLEAN NOT NULL DEFAULT TRUE,
    created_at          TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW(),
    UNIQUE (provider_id, code)
);

CREATE INDEX idx_provider_fse_api_provider ON provider_fse_api(provider_id);

ALTER TABLE provider_api
    ADD COLUMN fse_api_id UUID REFERENCES provider_fse_api(id) ON DELETE SET NULL;

CREATE INDEX idx_provider_api_fse_api ON provider_api(fse_api_id);
