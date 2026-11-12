-- La cotisation due est une action de la party, pas du seul nom d'utilisateur.
ALTER TABLE tontine_member ADD COLUMN IF NOT EXISTS party_id UUID;

CREATE INDEX IF NOT EXISTS tontine_member_party_idx ON tontine_member (party_id);
