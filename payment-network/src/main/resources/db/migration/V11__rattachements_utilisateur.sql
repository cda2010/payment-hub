-- Une personne (un login) peut être rattachée à plusieurs unités.
-- L'unicité reste par unité. Le compte Keycloak reste unique.

DROP INDEX IF EXISTS uq_utilisateur_login;

CREATE UNIQUE INDEX uq_utilisateur_unite_login
    ON unite_utilisateur (unite_id, lower(identifiant));

CREATE INDEX idx_utilisateur_login
    ON unite_utilisateur (lower(identifiant));
