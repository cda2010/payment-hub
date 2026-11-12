-- Produit d'enrôlement : reconnaître un compte Coris, puis éventuellement ouvrir un portefeuille.

CREATE TABLE enrolement_compte (
    id          UUID PRIMARY KEY,
    tenant_id   VARCHAR(64)  NOT NULL,
    maison      VARCHAR(16)  NOT NULL,
    numero      VARCHAR(40)  NOT NULL,
    party_id    UUID         NOT NULL REFERENCES identity_party (id),
    telephone   VARCHAR(30)  NOT NULL,
    CONSTRAINT enrolement_compte_uk UNIQUE (tenant_id, maison, numero)
);

CREATE INDEX enrolement_compte_numero_idx ON enrolement_compte (tenant_id, numero);

INSERT INTO enrolement_compte (id, tenant_id, maison, numero, party_id, telephone)
VALUES
    ('e1000001-0000-4000-8000-000000000001', 'CORIS_DEMO', 'MONEY', '00111000002612',
     'a1000001-0000-4000-8000-000000000006', '70123456'),
    ('e1000001-0000-4000-8000-000000000002', 'CORIS_DEMO', 'MONEY', '0011000000001',
     'a1000001-0000-4000-8000-000000000006', '70123456'),
    ('e1000001-0000-4000-8000-000000000003', 'CORIS_DEMO', 'MONEY', '70123456',
     'a1000001-0000-4000-8000-000000000006', '70123456'),
    ('e1000001-0000-4000-8000-000000000004', 'CORIS_DEMO', 'BANK', '0003000300030003',
     'a1000001-0000-4000-8000-000000000004', '70000003'),
    ('e1000001-0000-4000-8000-000000000005', 'CORIS_DEMO', 'MESO', '0004000400040004',
     'a1000001-0000-4000-8000-000000000003', '70000002');

CREATE TABLE enrolement (
    id               UUID PRIMARY KEY,
    tenant_id        VARCHAR(64)  NOT NULL,
    origine          VARCHAR(16)  NOT NULL,
    party_id         UUID,
    telephone        VARCHAR(30)  NOT NULL,
    numero_saisi     VARCHAR(40),
    statut           VARCHAR(20)  NOT NULL,
    code_hash        VARCHAR(64),
    code_expire_at   TIMESTAMPTZ,
    essais           INT          NOT NULL DEFAULT 0,
    nom              VARCHAR(80),
    prenom           VARCHAR(80),
    date_naissance   DATE,
    numero_ouvert    VARCHAR(40),
    created_at       TIMESTAMPTZ  NOT NULL,
    updated_at       TIMESTAMPTZ  NOT NULL
);

CREATE TABLE enrolement_piece (
    id              UUID PRIMARY KEY,
    enrolement_id   UUID         NOT NULL REFERENCES enrolement (id),
    face            VARCHAR(8)   NOT NULL,
    content_type    VARCHAR(80)  NOT NULL,
    contenu         BYTEA        NOT NULL,
    CONSTRAINT enrolement_piece_uk UNIQUE (enrolement_id, face)
);
