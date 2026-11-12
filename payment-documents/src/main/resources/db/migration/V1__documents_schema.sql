-- Coffre documentaire : métadonnées ; le binaire est hors DB (disque / object storage).

CREATE TABLE stored_document (
    id              UUID PRIMARY KEY,
    tenant_id       VARCHAR(64)  NOT NULL,
    code            VARCHAR(80)  NOT NULL,
    owner_kind      VARCHAR(32)  NOT NULL,
    owner_id        VARCHAR(128),
    payment_id      VARCHAR(128),
    original_name   VARCHAR(512) NOT NULL,
    content_type    VARCHAR(128) NOT NULL,
    size_bytes      BIGINT       NOT NULL,
    storage_key     VARCHAR(512) NOT NULL,
    checksum_sha256 VARCHAR(64),
    status          VARCHAR(32)  NOT NULL DEFAULT 'UPLOADED',
    created_by      VARCHAR(128),
    created_at      TIMESTAMPTZ  NOT NULL DEFAULT NOW(),
    updated_at      TIMESTAMPTZ  NOT NULL DEFAULT NOW(),
    deleted_at      TIMESTAMPTZ
);

CREATE INDEX idx_stored_document_payment ON stored_document (payment_id) WHERE deleted_at IS NULL;
CREATE INDEX idx_stored_document_owner ON stored_document (owner_kind, owner_id) WHERE deleted_at IS NULL;
CREATE INDEX idx_stored_document_tenant_code ON stored_document (tenant_id, code) WHERE deleted_at IS NULL;
CREATE UNIQUE INDEX uq_stored_document_storage_key ON stored_document (storage_key);
