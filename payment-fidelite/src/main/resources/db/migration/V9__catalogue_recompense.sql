-- Le catalogue porte la récompense. Le programme ne fait que la proposer.

CREATE TABLE catalogue_recompense (
    id               UUID PRIMARY KEY,
    tenant_id        VARCHAR(64)  NOT NULL,
    code             VARCHAR(40)  NOT NULL,
    titre            VARCHAR(120) NOT NULL,
    description      VARCHAR(240) NOT NULL,
    cout             INT          NOT NULL,
    icone            VARCHAR(80)  NOT NULL,
    categorie        VARCHAR(24)  NOT NULL,
    type_recompense  VARCHAR(30)  NOT NULL DEFAULT 'CATALOGUE',
    CONSTRAINT catalogue_recompense_tenant_code_uk UNIQUE (tenant_id, code)
);

CREATE TABLE programme_catalogue_recompense (
    id                       UUID PRIMARY KEY,
    programme_id             UUID        NOT NULL REFERENCES programme (id),
    catalogue_recompense_id  UUID        NOT NULL REFERENCES catalogue_recompense (id),
    position                 INT         NOT NULL,
    stock                    INT,
    stock_restant            INT,
    valide_du                TIMESTAMPTZ,
    valide_au                TIMESTAMPTZ,
    marchand_id              UUID,
    taux_cashback            NUMERIC(12, 4),
    financeur_id             UUID,
    CONSTRAINT programme_catalogue_recompense_uk UNIQUE (programme_id, catalogue_recompense_id)
);

CREATE INDEX programme_catalogue_recompense_idx
    ON programme_catalogue_recompense (programme_id, position);

INSERT INTO catalogue_recompense (id, tenant_id, code, titre, description, cout, icone, categorie, type_recompense)
SELECT gen_random_uuid(), tenant_id, code, titre, description, cout, icone, categorie, type_recompense
FROM (
    SELECT DISTINCT ON (p.tenant_id, upper(r.code))
        p.tenant_id,
        upper(r.code) AS code,
        r.titre,
        r.description,
        r.cout,
        r.icone,
        r.categorie,
        COALESCE(r.type_recompense, 'CATALOGUE') AS type_recompense
    FROM recompense r
    JOIN programme p ON p.id = r.programme_id
    ORDER BY p.tenant_id, upper(r.code), r.position, r.id
) source;

INSERT INTO programme_catalogue_recompense (
    id, programme_id, catalogue_recompense_id, position, stock, stock_restant,
    valide_du, valide_au, marchand_id, taux_cashback, financeur_id
)
SELECT r.id, r.programme_id, c.id, r.position, r.stock, r.stock_restant,
       r.valide_du, r.valide_au, r.marchand_id, r.taux_cashback, r.financeur_id
FROM recompense r
JOIN programme p ON p.id = r.programme_id
JOIN catalogue_recompense c ON c.tenant_id = p.tenant_id AND c.code = upper(r.code);

ALTER TABLE echange DROP CONSTRAINT echange_recompense_id_fkey;

ALTER TABLE echange
    ADD CONSTRAINT echange_recompense_id_fkey
    FOREIGN KEY (recompense_id) REFERENCES programme_catalogue_recompense (id);

DROP TABLE recompense;
