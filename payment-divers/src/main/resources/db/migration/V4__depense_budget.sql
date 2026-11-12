-- Une dépense = un paiement réussi classé. Le budget ne stocke que le plafond.

CREATE TABLE depense (
    id              UUID PRIMARY KEY,
    payment_id      VARCHAR(64) NOT NULL UNIQUE,
    party_id        UUID        NOT NULL,
    tenant_id       VARCHAR(64),
    categorie_code  VARCHAR(40) NOT NULL REFERENCES categorie_depense (code),
    amount          NUMERIC(19, 4) NOT NULL,
    currency        VARCHAR(10) NOT NULL,
    merchant_name   VARCHAR(160),
    product_code    VARCHAR(80),
    occurred_at     TIMESTAMPTZ NOT NULL
);

CREATE INDEX depense_party_month_idx ON depense (party_id, occurred_at);
CREATE INDEX depense_party_categorie_idx ON depense (party_id, categorie_code, occurred_at);

CREATE TABLE budget (
    id              UUID PRIMARY KEY,
    party_id        UUID        NOT NULL,
    name            VARCHAR(120) NOT NULL,
    categorie_code  VARCHAR(40) NOT NULL REFERENCES categorie_depense (code),
    limit_amount    NUMERIC(19, 4) NOT NULL,
    created_at      TIMESTAMPTZ NOT NULL,
    UNIQUE (party_id, categorie_code)
);

CREATE INDEX budget_party_idx ON budget (party_id);
