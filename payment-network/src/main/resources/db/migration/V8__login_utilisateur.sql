-- Le login d'une personne d'unité est un identifiant dédié, distinct du téléphone.
-- Le téléphone et l'e-mail servent uniquement à lui envoyer le mot de passe temporaire.

ALTER TABLE unite_utilisateur
    ADD COLUMN telephone VARCHAR(20),
    ADD COLUMN email VARCHAR(180),
    ADD COLUMN canal VARCHAR(8);

ALTER TABLE unite_utilisateur
    ADD CONSTRAINT ck_unite_utilisateur_canal
        CHECK (canal IS NULL OR canal IN ('SMS', 'EMAIL'));

CREATE UNIQUE INDEX uq_utilisateur_login ON unite_utilisateur (lower(identifiant));
