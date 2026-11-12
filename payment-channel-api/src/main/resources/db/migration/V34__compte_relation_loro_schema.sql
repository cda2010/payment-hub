-- Catalogue comptes LORO / miroir (aligné Omnicanal Compte + Relation + TypeCompte)
-- + bindings ParametreSchema inter-FSE (CPTELORO / CPTEMIROIR) sans hardcode Java

CREATE TABLE IF NOT EXISTS type_compte (
    code_parametre VARCHAR(64) PRIMARY KEY,
    libelle        VARCHAR(150) NOT NULL
);

CREATE TABLE IF NOT EXISTS relation_fse (
    id              UUID PRIMARY KEY,
    fse_source      VARCHAR(20) NOT NULL,
    fse_destination VARCHAR(20) NOT NULL,
    service_type    VARCHAR(40),
    created_at      TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    CONSTRAINT uk_relation_fse_corridor UNIQUE (fse_source, fse_destination, service_type)
);

CREATE TABLE IF NOT EXISTS compte_fse (
    id               UUID PRIMARY KEY,
    numero_compte    VARCHAR(64) NOT NULL,
    type_compte_code VARCHAR(64) NOT NULL REFERENCES type_compte (code_parametre),
    fse_teneur       VARCHAR(20) NOT NULL,
    relation_id      UUID REFERENCES relation_fse (id),
    service_type     VARCHAR(40),
    created_at       TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE INDEX IF NOT EXISTS idx_compte_fse_lookup
    ON compte_fse (type_compte_code, fse_teneur, relation_id);

INSERT INTO type_compte (code_parametre, libelle)
VALUES
    ('CPTELORO', 'Compte LORO'),
    ('CPTEMIROIR', 'Compte miroir')
ON CONFLICT (code_parametre) DO NOTHING;

-- Corridor Coris Money (0011) ↔ CBI BF (0004) pour TRANSFERT
INSERT INTO relation_fse (id, fse_source, fse_destination, service_type, created_at)
VALUES
    ('d0000001-0000-4000-8000-000000000001', '0011', '0004', 'TRANSFERT', NOW())
ON CONFLICT (fse_source, fse_destination, service_type) DO NOTHING;

-- LORO CBI hébergé chez Coris Money (crédit jambe source)
-- Miroir Coris Money hébergé chez CBI (débit jambe dest)
INSERT INTO compte_fse (id, numero_compte, type_compte_code, fse_teneur, relation_id, service_type, created_at)
VALUES
    ('e0000001-0000-4000-8000-000000000001', '000000279', 'CPTELORO', '0011',
     'd0000001-0000-4000-8000-000000000001', 'TRANSFERT', NOW()),
    ('e0000001-0000-4000-8000-000000000002', '00047324101', 'CPTEMIROIR', '0004',
     'd0000001-0000-4000-8000-000000000001', 'TRANSFERT', NOW())
ON CONFLICT (id) DO NOTHING;

-- Inter-FSE : 2 jambes TRANSFERT (corrige DEBIT/CREDIT éventuels du seed V30)
UPDATE ligne_schema_ecriture
SET type_methode = 'TRANSFERT'
WHERE id IN (
    'b0000001-0000-4000-8000-000000000002',
    'b0000001-0000-4000-8000-000000000003'
);

-- Remplacer les bindings identité compte* par redirections Omnicanal
DELETE FROM parametre_schema
WHERE ligne_schema_ecriture_id IN (
    'b0000001-0000-4000-8000-000000000002',
    'b0000001-0000-4000-8000-000000000003'
);

INSERT INTO parametre_schema (id, ligne_schema_ecriture_id, code_parametre, code_parametre_methode, categorie, created_at)
VALUES
    -- Jambe source (FSESOURCE) : débit client, crédit LORO dest chez source
    ('c0000001-0000-4000-8000-000000000020', 'b0000001-0000-4000-8000-000000000002',
     'CPTSOURCE', 'CPTSOURCE', 'USER', NOW()),
    ('c0000001-0000-4000-8000-000000000021', 'b0000001-0000-4000-8000-000000000002',
     'CPTELORO', 'CPTDEST', 'SYSTEM', NOW()),
    ('c0000001-0000-4000-8000-000000000022', 'b0000001-0000-4000-8000-000000000002',
     'MONTANT', 'MONTANT', 'USER', NOW()),
    -- Jambe dest (FSEDESTINATAIRE) : débit miroir source chez dest, crédit bénéficiaire
    ('c0000001-0000-4000-8000-000000000023', 'b0000001-0000-4000-8000-000000000003',
     'CPTEMIROIR', 'CPTSOURCE', 'SYSTEM', NOW()),
    ('c0000001-0000-4000-8000-000000000024', 'b0000001-0000-4000-8000-000000000003',
     'CPTDEST', 'CPTDEST', 'USER', NOW()),
    ('c0000001-0000-4000-8000-000000000025', 'b0000001-0000-4000-8000-000000000003',
     'MONTANT', 'MONTANT', 'USER', NOW())
ON CONFLICT (id) DO NOTHING;
