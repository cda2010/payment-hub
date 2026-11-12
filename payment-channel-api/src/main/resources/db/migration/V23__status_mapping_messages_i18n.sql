-- Messages de mapping multi-langue : { "fr": "...", "en": "..." }
ALTER TABLE provider_status_mapping
    ADD COLUMN IF NOT EXISTS messages JSONB;

UPDATE provider_status_mapping
SET messages = CASE
    WHEN message IS NULL OR btrim(message::text) = '' THEN NULL
    WHEN btrim(message::text) LIKE '{%' THEN message::jsonb
    ELSE jsonb_build_object('fr', message, 'en', message)
END
WHERE messages IS NULL;

ALTER TABLE provider_status_mapping
    DROP COLUMN IF EXISTS message;
