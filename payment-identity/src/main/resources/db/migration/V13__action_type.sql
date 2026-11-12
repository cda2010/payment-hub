-- Catalogue des types « À faire » : le type porte le libellé, l'icône et la route.
-- party_action ne conserve que l'instance (party, agrégat, sous-titre variable, échéance).

CREATE TABLE IF NOT EXISTS action_type (
    code            VARCHAR(64) PRIMARY KEY,
    title           VARCHAR(255) NOT NULL,
    subtitle        VARCHAR(512),
    icon            VARCHAR(64),
    route           VARCHAR(255) NOT NULL,
    route_fallback  VARCHAR(255),
    priority        INT          NOT NULL DEFAULT 100
);

INSERT INTO action_type (code, title, subtitle, icon, route, route_fallback, priority)
VALUES
    ('FIRST_OP', 'Tutoriel première opération', 'Guide rapide transfert ou facture',
        'school-outline', '/tabs/transfer', NULL, 10),
    ('APPROVAL_PENDING', 'Validation en attente', 'Votre opération attend une approbation',
        'shield-checkmark-outline', '/list', NULL, 15),
    ('KYC_RENEW', 'Pièce bientôt expirée', 'Mettez à jour votre dossier KYC',
        'id-card-outline', '/kyc', NULL, 20),
    ('DOC_REQUIRED', 'Pièces justificatives manquantes', 'Déposez les documents demandés',
        'document-attach-outline', '/journey/{aggregateId}', '/tabs/home', 25),
    ('COTISATION_DUE', '2 cotisations à régler', 'Associations et clubs',
        'people-circle-outline', '/cotisations', NULL, 30),
    ('CASHOUT_CODE', '1 code retrait actif', 'À utiliser avant expiration',
        'keypad-outline', '/retrait-code-active', NULL, 40)
ON CONFLICT (code) DO UPDATE SET
    title = EXCLUDED.title,
    subtitle = EXCLUDED.subtitle,
    icon = EXCLUDED.icon,
    route = EXCLUDED.route,
    route_fallback = EXCLUDED.route_fallback,
    priority = EXCLUDED.priority;

DO $$
BEGIN
    IF NOT EXISTS (SELECT 1 FROM pg_constraint WHERE conname = 'fk_party_action_type') THEN
        ALTER TABLE party_action
            ADD CONSTRAINT fk_party_action_type
            FOREIGN KEY (action_type) REFERENCES action_type (code);
    END IF;
END $$;

DROP INDEX IF EXISTS idx_party_action_party_status;

ALTER TABLE party_action DROP COLUMN IF EXISTS title;
ALTER TABLE party_action DROP COLUMN IF EXISTS icon;
ALTER TABLE party_action DROP COLUMN IF EXISTS route;
ALTER TABLE party_action DROP COLUMN IF EXISTS priority;

CREATE INDEX IF NOT EXISTS idx_party_action_party_status
    ON party_action (party_id, status, created_at ASC);
