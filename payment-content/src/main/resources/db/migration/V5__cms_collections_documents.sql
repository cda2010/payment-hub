-- CMS minimal : rubriques (collections) + documents (fiches / articles).

CREATE TABLE content_collection (
    id          UUID PRIMARY KEY,
    code        VARCHAR(64)  NOT NULL,
    kind        VARCHAR(32)  NOT NULL DEFAULT 'DIRECTORY',
    enabled     BOOLEAN      NOT NULL DEFAULT TRUE,
    priority    INT          NOT NULL DEFAULT 0,
    icon        VARCHAR(64),
    channels    JSONB        NOT NULL DEFAULT '[]'::jsonb,
    audiences   JSONB        NOT NULL DEFAULT '[]'::jsonb,
    schema_json JSONB        NOT NULL DEFAULT '{}'::jsonb,
    created_at  TIMESTAMPTZ  NOT NULL DEFAULT NOW(),
    updated_at  TIMESTAMPTZ  NOT NULL DEFAULT NOW(),
    CONSTRAINT uq_content_collection_code UNIQUE (code)
);

CREATE TABLE content_collection_locale (
    id              UUID PRIMARY KEY,
    collection_id   UUID         NOT NULL REFERENCES content_collection (id) ON DELETE CASCADE,
    locale          VARCHAR(8)   NOT NULL,
    title           VARCHAR(255) NOT NULL,
    description     TEXT,
    CONSTRAINT uq_content_collection_locale UNIQUE (collection_id, locale)
);

CREATE TABLE content_document (
    id              UUID PRIMARY KEY,
    collection_id   UUID         NOT NULL REFERENCES content_collection (id) ON DELETE CASCADE,
    code            VARCHAR(64)  NOT NULL,
    enabled         BOOLEAN      NOT NULL DEFAULT TRUE,
    priority        INT          NOT NULL DEFAULT 0,
    route           VARCHAR(255),
    icon            VARCHAR(64),
    image_url       VARCHAR(1024),
    image_alt       VARCHAR(255),
    attrs           JSONB        NOT NULL DEFAULT '{}'::jsonb,
    channels        JSONB        NOT NULL DEFAULT '[]'::jsonb,
    audiences       JSONB        NOT NULL DEFAULT '[]'::jsonb,
    starts_at       TIMESTAMPTZ,
    ends_at         TIMESTAMPTZ,
    created_at      TIMESTAMPTZ  NOT NULL DEFAULT NOW(),
    updated_at      TIMESTAMPTZ  NOT NULL DEFAULT NOW(),
    CONSTRAINT uq_content_document_code UNIQUE (code)
);

CREATE INDEX idx_content_document_collection
    ON content_document (collection_id, enabled, priority DESC);

CREATE TABLE content_document_locale (
    id           UUID PRIMARY KEY,
    document_id  UUID         NOT NULL REFERENCES content_document (id) ON DELETE CASCADE,
    locale       VARCHAR(8)   NOT NULL,
    title        VARCHAR(255) NOT NULL,
    summary      VARCHAR(512),
    body         TEXT,
    CONSTRAINT uq_content_document_locale UNIQUE (document_id, locale)
);

CREATE INDEX idx_content_document_locale_doc ON content_document_locale (document_id);
