-- Indicatif téléphonique du pays (ex. +226). Idempotent : la colonne peut déjà exister.

ALTER TABLE pays ADD COLUMN IF NOT EXISTS indicatif VARCHAR(8);

UPDATE pays SET indicatif = v.indicatif
FROM (VALUES
    ('BF', '+226'),
    ('BJ', '+229'),
    ('CI', '+225'),
    ('GW', '+245'),
    ('GN', '+224'),
    ('ML', '+223'),
    ('MA', '+212'),
    ('NE', '+227'),
    ('SN', '+221'),
    ('TD', '+235'),
    ('TG', '+228')
) AS v(code, indicatif)
WHERE pays.code = v.code
  AND (pays.indicatif IS NULL OR pays.indicatif = '');
