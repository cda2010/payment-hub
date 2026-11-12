-- Le pays devient la racine de l'arbre : PAYS > FSE > AGENCE > MASTER/MARCHAND > SOUS_DISTRIBUTEUR.
-- Un pays est une unité d'organisation de type PAYS. Les 14 pays UEMOA et CEMAC sont fournis ici et ne se créent
-- pas par l'API ; code = ISO 3166 alpha-2, identifiant a0000000-…-000000000<code ISO numérique>.

ALTER TABLE unite_organisation DROP CONSTRAINT unite_organisation_type_check;
ALTER TABLE unite_organisation ADD CONSTRAINT ck_unite_type
    CHECK (type IN ('PAYS', 'FSE', 'AGENCE', 'MASTER', 'MARCHAND', 'SOUS_DISTRIBUTEUR', 'REGROUPEMENT'));
ALTER TABLE unite_organisation DROP CONSTRAINT ck_unite_parent;

INSERT INTO unite_organisation (id, code, libelle, type, zone, parent_id, ancrage_id, statut, niveau, chemin, created_by, updated_by) VALUES
    ('a0000000-0000-0000-0000-000000000204', 'BJ', 'Bénin',              'PAYS', 'UEMOA', NULL, NULL, 'ACTIF', 1, '/a0000000-0000-0000-0000-000000000204/', 'flyway', 'flyway'),
    ('a0000000-0000-0000-0000-000000000854', 'BF', 'Burkina Faso',       'PAYS', 'UEMOA', NULL, NULL, 'ACTIF', 1, '/a0000000-0000-0000-0000-000000000854/', 'flyway', 'flyway'),
    ('a0000000-0000-0000-0000-000000000384', 'CI', 'Côte d''Ivoire',     'PAYS', 'UEMOA', NULL, NULL, 'ACTIF', 1, '/a0000000-0000-0000-0000-000000000384/', 'flyway', 'flyway'),
    ('a0000000-0000-0000-0000-000000000624', 'GW', 'Guinée-Bissau',      'PAYS', 'UEMOA', NULL, NULL, 'ACTIF', 1, '/a0000000-0000-0000-0000-000000000624/', 'flyway', 'flyway'),
    ('a0000000-0000-0000-0000-000000000466', 'ML', 'Mali',               'PAYS', 'UEMOA', NULL, NULL, 'ACTIF', 1, '/a0000000-0000-0000-0000-000000000466/', 'flyway', 'flyway'),
    ('a0000000-0000-0000-0000-000000000562', 'NE', 'Niger',              'PAYS', 'UEMOA', NULL, NULL, 'ACTIF', 1, '/a0000000-0000-0000-0000-000000000562/', 'flyway', 'flyway'),
    ('a0000000-0000-0000-0000-000000000686', 'SN', 'Sénégal',            'PAYS', 'UEMOA', NULL, NULL, 'ACTIF', 1, '/a0000000-0000-0000-0000-000000000686/', 'flyway', 'flyway'),
    ('a0000000-0000-0000-0000-000000000768', 'TG', 'Togo',               'PAYS', 'UEMOA', NULL, NULL, 'ACTIF', 1, '/a0000000-0000-0000-0000-000000000768/', 'flyway', 'flyway'),
    ('a0000000-0000-0000-0000-000000000120', 'CM', 'Cameroun',           'PAYS', 'CEMAC', NULL, NULL, 'ACTIF', 1, '/a0000000-0000-0000-0000-000000000120/', 'flyway', 'flyway'),
    ('a0000000-0000-0000-0000-000000000140', 'CF', 'Centrafrique',       'PAYS', 'CEMAC', NULL, NULL, 'ACTIF', 1, '/a0000000-0000-0000-0000-000000000140/', 'flyway', 'flyway'),
    ('a0000000-0000-0000-0000-000000000178', 'CG', 'Congo',              'PAYS', 'CEMAC', NULL, NULL, 'ACTIF', 1, '/a0000000-0000-0000-0000-000000000178/', 'flyway', 'flyway'),
    ('a0000000-0000-0000-0000-000000000266', 'GA', 'Gabon',              'PAYS', 'CEMAC', NULL, NULL, 'ACTIF', 1, '/a0000000-0000-0000-0000-000000000266/', 'flyway', 'flyway'),
    ('a0000000-0000-0000-0000-000000000226', 'GQ', 'Guinée équatoriale', 'PAYS', 'CEMAC', NULL, NULL, 'ACTIF', 1, '/a0000000-0000-0000-0000-000000000226/', 'flyway', 'flyway'),
    ('a0000000-0000-0000-0000-000000000148', 'TD', 'Tchad',              'PAYS', 'CEMAC', NULL, NULL, 'ACTIF', 1, '/a0000000-0000-0000-0000-000000000148/', 'flyway', 'flyway');

-- Les FSE existants passent sous un pays. À la date de cette migration, seuls les FSE de démonstration existent :
-- FSE-UEMOA au Burkina Faso, FSE-CEMAC au Cameroun ; un autre FSE irait au Burkina Faso (UEMOA) ou au Cameroun (CEMAC).
UPDATE unite_organisation
   SET parent_id = CASE zone WHEN 'UEMOA' THEN 'a0000000-0000-0000-0000-000000000854'::uuid
                             ELSE 'a0000000-0000-0000-0000-000000000120'::uuid END,
       updated_at = NOW(),
       updated_by = 'flyway'
 WHERE type = 'FSE' AND parent_id IS NULL;

-- Toute l'ancienne arborescence descend d'un niveau : le pays de son FSE racine est ajouté en tête du chemin.
UPDATE unite_organisation u
   SET chemin = '/' || f.parent_id || u.chemin,
       niveau = u.niveau + 1
  FROM unite_organisation f
 WHERE u.type NOT IN ('PAYS', 'REGROUPEMENT')
   AND f.id = split_part(u.chemin, '/', 2)::uuid;

ALTER TABLE unite_organisation ADD CONSTRAINT ck_unite_parent
    CHECK ((type IN ('PAYS', 'REGROUPEMENT')) = (parent_id IS NULL));
ALTER TABLE unite_organisation ADD CONSTRAINT ck_unite_pays_code
    CHECK (type <> 'PAYS' OR code ~ '^[A-Z]{2}$');

-- Démonstration : un regroupement de pays, qui couvre tout le réseau de ses membres.
INSERT INTO unite_organisation (id, code, libelle, type, zone, parent_id, ancrage_id, statut, niveau, chemin, created_by, updated_by) VALUES
    ('10000000-0000-0000-0000-000000000042', 'RG-PAYS-SAHEL', 'Pays du Sahel', 'REGROUPEMENT', 'UEMOA',
     NULL, NULL, 'ACTIF', 0, '/10000000-0000-0000-0000-000000000042/', 'flyway', 'flyway');

INSERT INTO regroupement_membre (id, regroupement_id, unite_id, ajoute_par) VALUES
    ('30000000-0000-0000-0000-000000000003', '10000000-0000-0000-0000-000000000042', 'a0000000-0000-0000-0000-000000000854', 'flyway'),
    ('30000000-0000-0000-0000-000000000004', '10000000-0000-0000-0000-000000000042', 'a0000000-0000-0000-0000-000000000466', 'flyway'),
    ('30000000-0000-0000-0000-000000000005', '10000000-0000-0000-0000-000000000042', 'a0000000-0000-0000-0000-000000000562', 'flyway');
