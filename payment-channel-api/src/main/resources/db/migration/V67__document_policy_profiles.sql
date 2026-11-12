-- DocumentPolicy : profils de pièces (DMN → documentProfileCode)

CREATE TABLE IF NOT EXISTS document_profile (
    id               UUID PRIMARY KEY,
    code             VARCHAR(80)  NOT NULL UNIQUE,
    name             VARCHAR(150) NOT NULL,
    description      TEXT,
    definition_json  TEXT         NOT NULL,
    actif            BOOLEAN      NOT NULL DEFAULT TRUE,
    created_at       TIMESTAMPTZ  NOT NULL DEFAULT NOW(),
    updated_at       TIMESTAMPTZ  NOT NULL DEFAULT NOW()
);

INSERT INTO document_profile (id, code, name, description, definition_json, actif, created_at, updated_at)
VALUES
(
    'd1000000-0000-4000-8000-000000000001',
    'NONE',
    'Aucun document',
    'Sous seuil / KYC déjà satisfait',
    '{"required":[],"optional":[],"requiresApproval":false}',
    TRUE, NOW(), NOW()
),
(
    'd1000000-0000-4000-8000-000000000002',
    'BASIC_ID',
    'Pièce d''identité',
    'Seuil bas — CNI / passeport',
    '{"required":[{"code":"ID_FRONT","label":"Pièce d''identité (recto)","mimeTypes":["image/jpeg","image/png","application/pdf"],"maxSizeMb":5}],"optional":[{"code":"ID_BACK","label":"Pièce d''identité (verso)","mimeTypes":["image/jpeg","image/png","application/pdf"],"maxSizeMb":5}],"requiresApproval":false}',
    TRUE, NOW(), NOW()
),
(
    'd1000000-0000-4000-8000-000000000003',
    'STANDARD',
    'Identité + domicile',
    'Seuil moyen',
    '{"required":[{"code":"ID_FRONT","label":"Pièce d''identité (recto)","mimeTypes":["image/jpeg","image/png","application/pdf"],"maxSizeMb":5},{"code":"PROOF_OF_ADDRESS","label":"Justificatif de domicile","mimeTypes":["image/jpeg","image/png","application/pdf"],"maxSizeMb":5}],"optional":[],"requiresApproval":false}',
    TRUE, NOW(), NOW()
),
(
    'd1000000-0000-4000-8000-000000000004',
    'ENHANCED',
    'Dossier renforcé',
    'Seuil élevé — origine des fonds + revue éventuelle',
    '{"required":[{"code":"ID_FRONT","label":"Pièce d''identité (recto)","mimeTypes":["image/jpeg","image/png","application/pdf"],"maxSizeMb":5},{"code":"PROOF_OF_ADDRESS","label":"Justificatif de domicile","mimeTypes":["image/jpeg","image/png","application/pdf"],"maxSizeMb":5},{"code":"PROOF_OF_FUNDS","label":"Justificatif d''origine des fonds","mimeTypes":["image/jpeg","image/png","application/pdf"],"maxSizeMb":10}],"optional":[{"code":"PURPOSE_STATEMENT","label":"Déclaration de motif","mimeTypes":["application/pdf","text/plain"],"maxSizeMb":2}],"requiresApproval":true}',
    TRUE, NOW(), NOW()
),
(
    'd1000000-0000-4000-8000-000000000005',
    'PROVIDER_STRICT',
    'Exigences provider strictes',
    'Certains opérateurs remittance (RIA, Western Union) — ID + relation bénéficiaire',
    '{"required":[{"code":"ID_FRONT","label":"Pièce d''identité (recto)","mimeTypes":["image/jpeg","image/png","application/pdf"],"maxSizeMb":5},{"code":"RELATIONSHIP_PROOF","label":"Preuve de lien avec le bénéficiaire","mimeTypes":["image/jpeg","image/png","application/pdf"],"maxSizeMb":5},{"code":"PURPOSE_STATEMENT","label":"Motif du transfert","mimeTypes":["application/pdf","text/plain"],"maxSizeMb":2}],"optional":[],"requiresApproval":true}',
    TRUE, NOW(), NOW()
)
ON CONFLICT (code) DO UPDATE
SET name = EXCLUDED.name,
    description = EXCLUDED.description,
    definition_json = EXCLUDED.definition_json,
    actif = TRUE,
    updated_at = NOW();
