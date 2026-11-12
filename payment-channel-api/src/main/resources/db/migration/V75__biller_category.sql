-- Catégories facturiers (navigation mobile / catalogue)

ALTER TABLE biller
    ADD COLUMN IF NOT EXISTS category_code VARCHAR(40);

CREATE INDEX IF NOT EXISTS idx_biller_category ON biller (category_code);

UPDATE biller SET category_code = 'ENERGY_WATER'
WHERE code IN ('SONABEL', 'ONEA', 'GAZ') AND (category_code IS NULL OR category_code = '');

UPDATE biller SET category_code = 'TELECOM_MEDIA'
WHERE code IN ('CANAL+', 'CANAL', 'FIBRE', 'AIRTIME', 'TV', 'ORANGE')
  AND (category_code IS NULL OR category_code = '');

UPDATE biller SET category_code = 'EDUCATION'
WHERE code IN ('SCHOOL', 'EXAM') AND (category_code IS NULL OR category_code = '');

UPDATE biller SET category_code = 'GOVERNMENT'
WHERE code IN ('IMPOTS', 'TAX', 'VIGNETTE', 'FINE')
  AND (category_code IS NULL OR category_code = '');

UPDATE biller SET category_code = 'INSURANCE_HEALTH'
WHERE code IN ('HEALTH', 'AUTO') AND (category_code IS NULL OR category_code = '');

UPDATE biller SET category_code = 'OTHER'
WHERE category_code IS NULL OR category_code = '';
