CREATE TABLE recompense (
    id            UUID PRIMARY KEY,
    programme_id  UUID         NOT NULL REFERENCES programme (id),
    code          VARCHAR(40)  NOT NULL,
    titre         VARCHAR(120) NOT NULL,
    description   VARCHAR(240) NOT NULL,
    cout          INT          NOT NULL,
    icone         VARCHAR(80)  NOT NULL,
    categorie     VARCHAR(24)  NOT NULL,
    position      INT          NOT NULL,
    UNIQUE (programme_id, code)
);

CREATE INDEX recompense_programme_idx ON recompense (programme_id, position);

CREATE TABLE echange (
    id             UUID PRIMARY KEY,
    recompense_id  UUID        NOT NULL REFERENCES recompense (id),
    programme_id   UUID        NOT NULL REFERENCES programme (id),
    party_id       UUID        NOT NULL,
    points         INT         NOT NULL,
    created_at     TIMESTAMPTZ NOT NULL
);

CREATE INDEX echange_party_idx ON echange (programme_id, party_id);

INSERT INTO recompense (id, programme_id, code, titre, description, cout, icone, categorie, position) VALUES
    ('c1000001-0000-4000-8000-000000000021', 'c1000001-0000-4000-8000-000000000001', 'RW-FEE', 'Transfert sans frais', '1 transfert wallet gratuit', 500, 'swap-horizontal-outline', 'fee', 0),
    ('c1000001-0000-4000-8000-000000000022', 'c1000001-0000-4000-8000-000000000001', 'RW-CASH-1K', 'Cashback 1 000 F', 'Crédité sur votre wallet', 1000, 'cash-outline', 'cashback', 1),
    ('c1000001-0000-4000-8000-000000000023', 'c1000001-0000-4000-8000-000000000001', 'RW-CASH-5K', 'Cashback 5 000 F', 'Crédité sur votre wallet', 4500, 'wallet-outline', 'cashback', 2),
    ('c1000001-0000-4000-8000-000000000024', 'c1000001-0000-4000-8000-000000000001', 'RW-AIRTIME', 'Crédit téléphonique', '1 000 F de crédit mobile', 800, 'phone-portrait-outline', 'partner', 3),
    ('c1000001-0000-4000-8000-000000000025', 'c1000001-0000-4000-8000-000000000001', 'RW-BILL', 'Bon facture', '2 000 F sur une facture SONABEL / ONEA', 1800, 'flash-outline', 'partner', 4),
    ('c1000001-0000-4000-8000-000000000026', 'c1000001-0000-4000-8000-000000000001', 'RW-GIFT', 'Carte cadeau partenaire', 'Bon d’achat 10 000 F', 9000, 'gift-outline', 'gift', 5);
