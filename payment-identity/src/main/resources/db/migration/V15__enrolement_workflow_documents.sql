-- Le workflow garde la trace des étapes. Les pièces vivent dans payment-documents.

ALTER TABLE enrolement ADD COLUMN workflow_code VARCHAR(40) NOT NULL DEFAULT 'ENROLEMENT';
ALTER TABLE enrolement ADD COLUMN recto_document_id UUID;
ALTER TABLE enrolement ADD COLUMN verso_document_id UUID;

CREATE TABLE enrolement_passage (
    id              UUID PRIMARY KEY,
    enrolement_id   UUID        NOT NULL REFERENCES enrolement (id),
    etape           VARCHAR(20) NOT NULL,
    created_at      TIMESTAMPTZ NOT NULL
);

CREATE INDEX enrolement_passage_idx ON enrolement_passage (enrolement_id, created_at);

DROP TABLE enrolement_piece;
