-- Visibilité canal × audience (aligné AccessChannel / ProductAudience du catalogue).
-- Listes JSON vides = visible partout (rétrocompat).

ALTER TABLE content_item
    ADD COLUMN IF NOT EXISTS channels  JSONB NOT NULL DEFAULT '[]'::jsonb,
    ADD COLUMN IF NOT EXISTS audiences JSONB NOT NULL DEFAULT '[]'::jsonb;

COMMENT ON COLUMN content_item.channels IS
    'AccessChannel[] — MOBILE, WEB, GAB, USSD, BACKOFFICE. Vide = tous.';
COMMENT ON COLUMN content_item.audiences IS
    'ProductAudience[] — RETAIL, CORPORATE, AGENT, MERCHANT, BACKOFFICE. Vide = toutes.';
