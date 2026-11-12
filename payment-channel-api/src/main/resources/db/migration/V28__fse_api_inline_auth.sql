-- Auth inline sur Api Omnicanal (comme Omnicanal : auth sur Api, pas sur Environnement)

ALTER TABLE provider_fse_api
    ADD COLUMN auth_type                VARCHAR(50),
    ADD COLUMN client_id                VARCHAR(255),
    ADD COLUMN client_secret_enc        TEXT,
    ADD COLUMN api_key_enc              TEXT,
    ADD COLUMN token_url                VARCHAR(500),
    ADD COLUMN username_enc             TEXT,
    ADD COLUMN password_enc             TEXT,
    ADD COLUMN custom_header_name       VARCHAR(150),
    ADD COLUMN custom_header_value_enc  TEXT,
    ADD COLUMN extra_config_json        TEXT;

-- Backfill depuis provider_authentication (migration Omnicanal existante)
UPDATE provider_fse_api fa
SET auth_type               = pa.auth_type,
    client_id               = pa.client_id,
    client_secret_enc       = pa.client_secret_enc,
    api_key_enc             = pa.api_key_enc,
    token_url               = pa.token_url,
    username_enc            = pa.username_enc,
    password_enc            = pa.password_enc,
    custom_header_name      = pa.custom_header_name,
    custom_header_value_enc = pa.custom_header_value_enc,
    extra_config_json       = pa.extra_config_json
FROM provider_authentication pa
WHERE fa.provider_id = pa.provider_id
  AND fa.environment_id = pa.environment_id
  AND fa.auth_type IS NULL;
