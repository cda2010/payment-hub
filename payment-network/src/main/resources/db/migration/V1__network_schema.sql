-- Réseau de distribution : unités d'organisation (arbre FSE > AGENCE > MASTER/MARCHAND > SOUS_DISTRIBUTEUR, par zone)
-- et regroupements hors hiérarchie, qui couvrent leurs membres et toute leur descendance.

CREATE TABLE unite_organisation (
    id          UUID PRIMARY KEY,
    code        VARCHAR(40)   NOT NULL,
    libelle     VARCHAR(150)  NOT NULL,
    type        VARCHAR(20)   NOT NULL
        CHECK (type IN ('FSE', 'AGENCE', 'MASTER', 'MARCHAND', 'SOUS_DISTRIBUTEUR', 'REGROUPEMENT')),
    zone        VARCHAR(8)    NOT NULL CHECK (zone IN ('UEMOA', 'CEMAC')),
    parent_id   UUID REFERENCES unite_organisation (id),
    ancrage_id  UUID REFERENCES unite_organisation (id),
    statut      VARCHAR(10)   NOT NULL CHECK (statut IN ('ACTIF', 'INACTIF')),
    -- 1 pour un FSE, +1 par niveau ; 0 pour un regroupement (hors arbre).
    niveau      INT           NOT NULL CHECK (niveau >= 0),
    -- Identifiants depuis la racine, l'unité comprise : /idFse/idAgence/idMaster/
    chemin      VARCHAR(400)  NOT NULL,
    version     BIGINT        NOT NULL DEFAULT 0,
    created_at  TIMESTAMPTZ   NOT NULL DEFAULT NOW(),
    created_by  VARCHAR(100)  NOT NULL,
    updated_at  TIMESTAMPTZ   NOT NULL DEFAULT NOW(),
    updated_by  VARCHAR(100)  NOT NULL,
    CONSTRAINT uq_unite_code UNIQUE (code),
    -- Seuls un FSE (racine) et un regroupement (hors arbre) n'ont pas de parent.
    CONSTRAINT ck_unite_parent CHECK ((type IN ('FSE', 'REGROUPEMENT')) = (parent_id IS NULL)),
    CONSTRAINT ck_unite_ancrage CHECK (type = 'REGROUPEMENT' OR ancrage_id IS NULL),
    CONSTRAINT ck_unite_chemin CHECK (chemin LIKE '/%/')
);

CREATE INDEX idx_unite_parent ON unite_organisation (parent_id);
CREATE INDEX idx_unite_zone_type ON unite_organisation (zone, type);
CREATE INDEX idx_unite_chemin ON unite_organisation (chemin varchar_pattern_ops);

CREATE TABLE regroupement_membre (
    id               UUID PRIMARY KEY,
    regroupement_id  UUID          NOT NULL REFERENCES unite_organisation (id),
    unite_id         UUID          NOT NULL REFERENCES unite_organisation (id),
    ajoute_le        TIMESTAMPTZ   NOT NULL DEFAULT NOW(),
    ajoute_par       VARCHAR(100)  NOT NULL,
    CONSTRAINT uq_regroupement_membre UNIQUE (regroupement_id, unite_id),
    CONSTRAINT ck_membre_distinct CHECK (regroupement_id <> unite_id)
);

CREATE INDEX idx_membre_unite ON regroupement_membre (unite_id);

-- ── Jeu de démonstration ───────────────────────────────────────────────────
-- UEMOA : un FSE, trois agences (dont deux au nord), masters, marchands, sous-distributeurs,
-- et le regroupement « Agences du Nord » ancré sur le FSE. CEMAC : une branche complète.

INSERT INTO unite_organisation (id, code, libelle, type, zone, parent_id, ancrage_id, statut, niveau, chemin, created_by, updated_by) VALUES
    ('10000000-0000-0000-0000-000000000001', 'FSE-UEMOA', 'Coris Money UEMOA', 'FSE', 'UEMOA', NULL, NULL, 'ACTIF', 1,
     '/10000000-0000-0000-0000-000000000001/', 'flyway', 'flyway'),

    ('10000000-0000-0000-0000-000000000011', 'AG-OUAHIGOUYA', 'Agence Ouahigouya', 'AGENCE', 'UEMOA',
     '10000000-0000-0000-0000-000000000001', NULL, 'ACTIF', 2,
     '/10000000-0000-0000-0000-000000000001/10000000-0000-0000-0000-000000000011/', 'flyway', 'flyway'),
    ('10000000-0000-0000-0000-000000000012', 'AG-KAYA', 'Agence Kaya', 'AGENCE', 'UEMOA',
     '10000000-0000-0000-0000-000000000001', NULL, 'ACTIF', 2,
     '/10000000-0000-0000-0000-000000000001/10000000-0000-0000-0000-000000000012/', 'flyway', 'flyway'),
    ('10000000-0000-0000-0000-000000000013', 'AG-BOBO', 'Agence Bobo Centre', 'AGENCE', 'UEMOA',
     '10000000-0000-0000-0000-000000000001', NULL, 'ACTIF', 2,
     '/10000000-0000-0000-0000-000000000001/10000000-0000-0000-0000-000000000013/', 'flyway', 'flyway'),

    ('10000000-0000-0000-0000-000000000021', 'MS-SAHEL', 'Master Sahel Distribution', 'MASTER', 'UEMOA',
     '10000000-0000-0000-0000-000000000011', NULL, 'ACTIF', 3,
     '/10000000-0000-0000-0000-000000000001/10000000-0000-0000-0000-000000000011/10000000-0000-0000-0000-000000000021/', 'flyway', 'flyway'),
    ('10000000-0000-0000-0000-000000000022', 'MC-MARCHE-OHG', 'Marché central Ouahigouya', 'MARCHAND', 'UEMOA',
     '10000000-0000-0000-0000-000000000011', NULL, 'ACTIF', 3,
     '/10000000-0000-0000-0000-000000000001/10000000-0000-0000-0000-000000000011/10000000-0000-0000-0000-000000000022/', 'flyway', 'flyway'),
    ('10000000-0000-0000-0000-000000000023', 'MS-KAYA', 'Master Kaya Services', 'MASTER', 'UEMOA',
     '10000000-0000-0000-0000-000000000012', NULL, 'ACTIF', 3,
     '/10000000-0000-0000-0000-000000000001/10000000-0000-0000-0000-000000000012/10000000-0000-0000-0000-000000000023/', 'flyway', 'flyway'),
    ('10000000-0000-0000-0000-000000000024', 'MC-BOBO-STATION', 'Station Bobo Centre', 'MARCHAND', 'UEMOA',
     '10000000-0000-0000-0000-000000000013', NULL, 'ACTIF', 3,
     '/10000000-0000-0000-0000-000000000001/10000000-0000-0000-0000-000000000013/10000000-0000-0000-0000-000000000024/', 'flyway', 'flyway'),

    ('10000000-0000-0000-0000-000000000031', 'SD-SAHEL-01', 'Sous-distributeur Sahel 01', 'SOUS_DISTRIBUTEUR', 'UEMOA',
     '10000000-0000-0000-0000-000000000021', NULL, 'ACTIF', 4,
     '/10000000-0000-0000-0000-000000000001/10000000-0000-0000-0000-000000000011/10000000-0000-0000-0000-000000000021/10000000-0000-0000-0000-000000000031/', 'flyway', 'flyway'),
    ('10000000-0000-0000-0000-000000000032', 'SD-SAHEL-02', 'Sous-distributeur Sahel 02', 'SOUS_DISTRIBUTEUR', 'UEMOA',
     '10000000-0000-0000-0000-000000000021', NULL, 'ACTIF', 4,
     '/10000000-0000-0000-0000-000000000001/10000000-0000-0000-0000-000000000011/10000000-0000-0000-0000-000000000021/10000000-0000-0000-0000-000000000032/', 'flyway', 'flyway'),

    ('10000000-0000-0000-0000-000000000041', 'RG-AGENCES-NORD', 'Agences du Nord', 'REGROUPEMENT', 'UEMOA',
     NULL, '10000000-0000-0000-0000-000000000001', 'ACTIF', 0,
     '/10000000-0000-0000-0000-000000000041/', 'flyway', 'flyway'),

    ('20000000-0000-0000-0000-000000000001', 'FSE-CEMAC', 'Coris Money CEMAC', 'FSE', 'CEMAC', NULL, NULL, 'ACTIF', 1,
     '/20000000-0000-0000-0000-000000000001/', 'flyway', 'flyway'),
    ('20000000-0000-0000-0000-000000000011', 'AG-DOUALA', 'Agence Douala Akwa', 'AGENCE', 'CEMAC',
     '20000000-0000-0000-0000-000000000001', NULL, 'ACTIF', 2,
     '/20000000-0000-0000-0000-000000000001/20000000-0000-0000-0000-000000000011/', 'flyway', 'flyway'),
    ('20000000-0000-0000-0000-000000000021', 'MS-LITTORAL', 'Master Littoral', 'MASTER', 'CEMAC',
     '20000000-0000-0000-0000-000000000011', NULL, 'ACTIF', 3,
     '/20000000-0000-0000-0000-000000000001/20000000-0000-0000-0000-000000000011/20000000-0000-0000-0000-000000000021/', 'flyway', 'flyway'),
    ('20000000-0000-0000-0000-000000000031', 'SD-LITTORAL-01', 'Sous-distributeur Littoral 01', 'SOUS_DISTRIBUTEUR', 'CEMAC',
     '20000000-0000-0000-0000-000000000021', NULL, 'ACTIF', 4,
     '/20000000-0000-0000-0000-000000000001/20000000-0000-0000-0000-000000000011/20000000-0000-0000-0000-000000000021/20000000-0000-0000-0000-000000000031/', 'flyway', 'flyway');

INSERT INTO regroupement_membre (id, regroupement_id, unite_id, ajoute_par) VALUES
    ('30000000-0000-0000-0000-000000000001', '10000000-0000-0000-0000-000000000041', '10000000-0000-0000-0000-000000000011', 'flyway'),
    ('30000000-0000-0000-0000-000000000002', '10000000-0000-0000-0000-000000000041', '10000000-0000-0000-0000-000000000012', 'flyway');
