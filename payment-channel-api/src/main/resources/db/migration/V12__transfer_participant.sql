-- Participants de transfert : champs par rôle (émetteur / bénéficiaire)

CREATE TABLE IF NOT EXISTS transfer_participant (
    id UUID PRIMARY KEY,
    code VARCHAR(50) NOT NULL UNIQUE,
    name VARCHAR(150) NOT NULL,
    country_code VARCHAR(5) NOT NULL DEFAULT 'BF',
    actif BOOLEAN NOT NULL DEFAULT TRUE,
    emitter_fields_json TEXT,
    beneficiary_fields_json TEXT,
    ordre_affichage INTEGER,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE INDEX IF NOT EXISTS idx_transfer_participant_country ON transfer_participant (country_code, actif);

ALTER TABLE transfer_corridor
    ADD COLUMN IF NOT EXISTS emitter_participant_id UUID REFERENCES transfer_participant (id),
    ADD COLUMN IF NOT EXISTS beneficiary_participant_id UUID REFERENCES transfer_participant (id);

CREATE INDEX IF NOT EXISTS idx_transfer_corridor_emitter ON transfer_corridor (emitter_participant_id);
CREATE INDEX IF NOT EXISTS idx_transfer_corridor_beneficiary ON transfer_corridor (beneficiary_participant_id);
