-- Aligner retry_delay_ms avec l'entité JPA (Long / BIGINT)
ALTER TABLE provider_api
    ALTER COLUMN retry_delay_ms TYPE BIGINT;
