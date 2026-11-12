-- Référentiel type de service (catalogue Omnicanal-like) + lien fournisseur
CREATE TABLE IF NOT EXISTS service_type (
    code              VARCHAR(40) PRIMARY KEY,
    libelle           VARCHAR(150) NOT NULL,
    category          VARCHAR(30)  NOT NULL,
    description       VARCHAR(500),
    actif             BOOLEAN      NOT NULL DEFAULT TRUE,
    ordre_affichage   INTEGER
);

INSERT INTO service_type (code, libelle, category, description, actif, ordre_affichage) VALUES
    ('LINK', 'Lien / abonnement compte', 'ACCOUNT', 'Rattachement d''un compte wallet ou bancaire', TRUE, 10),
    ('UNLINK', 'Déliaison compte', 'ACCOUNT', 'Suppression du rattachement compte', TRUE, 20),
    ('CONSULTATION_SOLDE', 'Consultation de solde', 'INQUIRY', 'Interrogation du solde (équivalent Omnicanal CONSULTATION_SOLDE)', TRUE, 30),
    ('TRANSFERT', 'Transfert', 'TRANSFER', 'Transfert de fonds (équivalent Omnicanal TRANSFERT)', TRUE, 40),
    ('PAYEMENT', 'Paiement', 'PAYMENT', 'Paiement / checkout (équivalent Omnicanal PAIEMENT)', TRUE, 50)
ON CONFLICT (code) DO NOTHING;

CREATE TABLE IF NOT EXISTS provider_service_type (
    provider_id       UUID        NOT NULL REFERENCES payment_provider(id) ON DELETE CASCADE,
    service_type_code VARCHAR(40) NOT NULL REFERENCES service_type(code) ON DELETE RESTRICT,
    PRIMARY KEY (provider_id, service_type_code)
);

CREATE INDEX IF NOT EXISTS idx_provider_service_type_code ON provider_service_type(service_type_code);

ALTER TABLE payment_transaction
    ADD COLUMN IF NOT EXISTS service_type VARCHAR(40);

-- Backfill : intention produit dérivée de operation_type
UPDATE payment_transaction
SET service_type = CASE operation_type
    WHEN 'TRANSFER' THEN 'TRANSFERT'
    WHEN 'TRANSFERT' THEN 'TRANSFERT'
    ELSE 'PAYEMENT'
END
WHERE service_type IS NULL;

-- Providers existants : rattacher des types par défaut selon le code
INSERT INTO provider_service_type (provider_id, service_type_code)
SELECT p.id, 'PAYEMENT'
FROM payment_provider p
WHERE p.code IN ('CORIS_MONEY', 'ORANGE_MONEY', 'WAVE', 'CARD_PAYMENT', 'BILL_PAYMENT')
ON CONFLICT DO NOTHING;

INSERT INTO provider_service_type (provider_id, service_type_code)
SELECT p.id, 'TRANSFERT'
FROM payment_provider p
WHERE p.code IN ('WALLET_TRANSFER', 'BANK_TRANSFER')
ON CONFLICT DO NOTHING;

INSERT INTO provider_service_type (provider_id, service_type_code)
SELECT p.id, 'CONSULTATION_SOLDE'
FROM payment_provider p
WHERE p.code IN ('CORIS_MONEY', 'ORANGE_MONEY', 'BANK_TRANSFER')
ON CONFLICT DO NOTHING;
