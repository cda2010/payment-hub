-- Affectation d'un profil d'offre à une unité d'organisation (payment-network).
-- unite_id est une référence logique (pas de FK inter-bases) : comme ownerId UNITE dans payment-alias.

CREATE TABLE identity_unite_profile (
    id            UUID PRIMARY KEY,
    unite_id      UUID         NOT NULL,
    profile_id    UUID         NOT NULL REFERENCES identity_profile (id),
    date_debut    DATE         NOT NULL DEFAULT CURRENT_DATE,
    date_fin      DATE,
    active        BOOLEAN      NOT NULL DEFAULT TRUE,
    created_at    TIMESTAMPTZ  NOT NULL DEFAULT NOW(),
    CONSTRAINT uq_identity_unite_profile UNIQUE (unite_id, profile_id)
);

CREATE INDEX idx_identity_unite_profile_unite ON identity_unite_profile (unite_id);
CREATE INDEX idx_identity_unite_profile_profile ON identity_unite_profile (profile_id);

COMMENT ON TABLE identity_unite_profile IS
    'Profils d''offre d''un membre du réseau (UNITE). L''offre résolue = union des profils actifs.';
