-- Lien utilisateur canal ↔ compte Keycloak (claim `sub`, stable même si le username change).
-- Les mots de passe sont gérés par Keycloak : password_hash n'est plus alimenté.
ALTER TABLE identity_channel_user ADD COLUMN IF NOT EXISTS keycloak_user_id VARCHAR(64);

CREATE UNIQUE INDEX IF NOT EXISTS uq_identity_user_keycloak
    ON identity_channel_user (keycloak_user_id)
    WHERE keycloak_user_id IS NOT NULL;
