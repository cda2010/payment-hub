-- Contenu éditorial (publicité / CMS léger) — slots + i18n.

CREATE TABLE content_item (
    id          UUID PRIMARY KEY,
    code        VARCHAR(64)  NOT NULL,
    slot        VARCHAR(64)  NOT NULL,
    enabled     BOOLEAN      NOT NULL DEFAULT TRUE,
    priority    INT          NOT NULL DEFAULT 0,
    plugin_id   VARCHAR(64),
    route       VARCHAR(255),
    icon        VARCHAR(64),
    tone        VARCHAR(32),
    starts_at   TIMESTAMPTZ,
    ends_at     TIMESTAMPTZ,
    created_at  TIMESTAMPTZ  NOT NULL DEFAULT NOW(),
    updated_at  TIMESTAMPTZ  NOT NULL DEFAULT NOW(),
    CONSTRAINT uq_content_item_code UNIQUE (code)
);

CREATE INDEX idx_content_item_slot_enabled ON content_item (slot, enabled, priority DESC);

CREATE TABLE content_item_locale (
    id          UUID PRIMARY KEY,
    item_id     UUID         NOT NULL REFERENCES content_item (id) ON DELETE CASCADE,
    locale      VARCHAR(8)   NOT NULL,
    eyebrow     VARCHAR(128),
    title       VARCHAR(255) NOT NULL,
    body        TEXT,
    cta         VARCHAR(128),
    CONSTRAINT uq_content_item_locale UNIQUE (item_id, locale)
);

CREATE INDEX idx_content_item_locale_item ON content_item_locale (item_id);
