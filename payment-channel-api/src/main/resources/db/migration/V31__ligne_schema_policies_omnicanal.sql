-- Residual LigneSchemaEcrit policies (aligné Omnicanal LigneSchemaEcrit)

ALTER TABLE ligne_schema_ecriture
    ADD COLUMN IF NOT EXISTS callback_policy VARCHAR(64),
    ADD COLUMN IF NOT EXISTS status_method VARCHAR(64),
    ADD COLUMN IF NOT EXISTS retry_initial_delay_ms BIGINT,
    ADD COLUMN IF NOT EXISTS retry_multiplier DOUBLE PRECISION,
    ADD COLUMN IF NOT EXISTS retry_max_delay_ms BIGINT,
    ADD COLUMN IF NOT EXISTS retryable_codes VARCHAR(512),
    ADD COLUMN IF NOT EXISTS compensation_step_code VARCHAR(128),
    ADD COLUMN IF NOT EXISTS compensation_reverse_desc BOOLEAN,
    ADD COLUMN IF NOT EXISTS compensation_max_attempts INT,
    ADD COLUMN IF NOT EXISTS compensation_manual_review BOOLEAN,
    ADD COLUMN IF NOT EXISTS depends_on_step_code VARCHAR(128),
    ADD COLUMN IF NOT EXISTS dependency_condition VARCHAR(48),
    ADD COLUMN IF NOT EXISTS dependency_required BOOLEAN,
    ADD COLUMN IF NOT EXISTS error_policies TEXT,
    ADD COLUMN IF NOT EXISTS default_error_action VARCHAR(48),
    ADD COLUMN IF NOT EXISTS check_status_method_code VARCHAR(64),
    ADD COLUMN IF NOT EXISTS max_retries INT;

ALTER TABLE parametre_schema
    ADD COLUMN IF NOT EXISTS nom_parametre VARCHAR(150);
