-- Produit ENROLEMENT. Ce n'est pas un paiement : pas de flow, pas de montant.
-- Le formulaire d'ouverture Coris Money est porté par le pays.

INSERT INTO categorie_service (id, code, name, description, ordre_affichage, actif, created_at, updated_at)
VALUES (
    gen_random_uuid(),
    'ENROLEMENT',
    'Enrôlement',
    'Ouverture de compte, hors paiement',
    80,
    TRUE,
    NOW(),
    NOW()
)
ON CONFLICT (code) DO NOTHING;

INSERT INTO catalog_de_service (
    id, code, name, description, status, created_at, updated_at, categorie_id
)
SELECT gen_random_uuid(),
       'ENROLEMENT',
       'Enrôlement',
       'Reconnaître un compte Coris ou ouvrir un portefeuille Coris Money',
       'PUBLISHED',
       NOW(),
       NOW(),
       c.id
FROM categorie_service c
WHERE c.code = 'ENROLEMENT'
  AND NOT EXISTS (SELECT 1 FROM catalog_de_service WHERE code = 'ENROLEMENT');

INSERT INTO catalog_de_service_version (
    id, catalog_de_service_id, version, status, workflow_key, configuration, created_at, updated_at
)
SELECT gen_random_uuid(),
       s.id,
       '1.0',
       'PUBLISHED',
       NULL,
       jsonb_build_object(
               'serviceType', 'DEFAUT',
               'journeyCode', 'ENROLEMENT',
               'workflow', 'ENROLEMENT',
               'supportedChannels', jsonb_build_array('MOBILE'),
               'defaultChannel', 'MOBILE',
               'supportedAudiences', jsonb_build_array('RETAIL'),
               'defaultAudience', 'RETAIL',
               'defautPays', 'BF',
               'pays', (
                   SELECT jsonb_object_agg(
                                  p.code,
                                  jsonb_build_object(
                                          'nom', p.nom,
                                          'devise', p.devise,
                                          'maisons', p.maisons,
                                          'ouverture', jsonb_build_object(
                                                  'maison', 'MONEY',
                                                  'champs', p.champs,
                                                  'documents', p.documents
                                          )
                                  )
                          )
                   FROM (
                            VALUES
                                ('BF', 'Burkina Faso', 'XOF',
                                 '["MONEY","BANK","MESO"]'::jsonb,
                                 $bf$[
                                   {"code":"nom","libelle":"Nom","type":"TEXT","requis":true},
                                   {"code":"prenom","libelle":"Prénom","type":"TEXT","requis":true},
                                   {"code":"dateNaissance","libelle":"Date de naissance","type":"DATE","requis":true}
                                 ]$bf$::jsonb,
                                 $bfdoc$[
                                   {"face":"RECTO","libelle":"CNIB recto","requis":true},
                                   {"face":"VERSO","libelle":"CNIB verso","requis":true}
                                 ]$bfdoc$::jsonb),
                                ('BJ', 'Bénin', 'XOF',
                                 '["MONEY","BANK","MESO"]'::jsonb,
                                 $bj$[
                                   {"code":"nom","libelle":"Nom","type":"TEXT","requis":true},
                                   {"code":"prenom","libelle":"Prénom","type":"TEXT","requis":true},
                                   {"code":"dateNaissance","libelle":"Date de naissance","type":"DATE","requis":true}
                                 ]$bj$::jsonb,
                                 $bjdoc$[
                                   {"face":"RECTO","libelle":"Pièce recto","requis":true},
                                   {"face":"VERSO","libelle":"Pièce verso","requis":true}
                                 ]$bjdoc$::jsonb),
                                ('CI', 'Côte d''Ivoire', 'XOF',
                                 '["MONEY","BANK","MESO"]'::jsonb,
                                 $ci$[
                                   {"code":"nom","libelle":"Nom","type":"TEXT","requis":true},
                                   {"code":"prenom","libelle":"Prénom","type":"TEXT","requis":true},
                                   {"code":"dateNaissance","libelle":"Date de naissance","type":"DATE","requis":true},
                                   {"code":"lieuNaissance","libelle":"Lieu de naissance","type":"TEXT","requis":true}
                                 ]$ci$::jsonb,
                                 $cidoc$[
                                   {"face":"RECTO","libelle":"CNI recto","requis":true},
                                   {"face":"VERSO","libelle":"CNI verso","requis":true}
                                 ]$cidoc$::jsonb),
                                ('SN', 'Sénégal', 'XOF',
                                 '["MONEY","BANK","MESO"]'::jsonb,
                                 $sn$[
                                   {"code":"nom","libelle":"Nom","type":"TEXT","requis":true},
                                   {"code":"prenom","libelle":"Prénom","type":"TEXT","requis":true},
                                   {"code":"dateNaissance","libelle":"Date de naissance","type":"DATE","requis":true},
                                   {"code":"numeroPiece","libelle":"Numéro de la CNI","type":"TEXT","requis":true}
                                 ]$sn$::jsonb,
                                 $sndoc$[
                                   {"face":"RECTO","libelle":"CNI recto","requis":true},
                                   {"face":"VERSO","libelle":"CNI verso","requis":true}
                                 ]$sndoc$::jsonb),
                                ('ML', 'Mali', 'XOF',
                                 '["MONEY","BANK","MESO"]'::jsonb,
                                 $ml$[
                                   {"code":"nom","libelle":"Nom","type":"TEXT","requis":true},
                                   {"code":"prenom","libelle":"Prénom","type":"TEXT","requis":true},
                                   {"code":"dateNaissance","libelle":"Date de naissance","type":"DATE","requis":true}
                                 ]$ml$::jsonb,
                                 $mldoc$[
                                   {"face":"RECTO","libelle":"Pièce recto","requis":true},
                                   {"face":"VERSO","libelle":"Pièce verso","requis":true}
                                 ]$mldoc$::jsonb),
                                ('NE', 'Niger', 'XOF',
                                 '["MONEY","BANK","MESO"]'::jsonb,
                                 $ne$[
                                   {"code":"nom","libelle":"Nom","type":"TEXT","requis":true},
                                   {"code":"prenom","libelle":"Prénom","type":"TEXT","requis":true},
                                   {"code":"dateNaissance","libelle":"Date de naissance","type":"DATE","requis":true}
                                 ]$ne$::jsonb,
                                 $nedoc$[
                                   {"face":"RECTO","libelle":"Pièce recto","requis":true},
                                   {"face":"VERSO","libelle":"Pièce verso","requis":true}
                                 ]$nedoc$::jsonb),
                                ('TG', 'Togo', 'XOF',
                                 '["MONEY","BANK","MESO"]'::jsonb,
                                 $tg$[
                                   {"code":"nom","libelle":"Nom","type":"TEXT","requis":true},
                                   {"code":"prenom","libelle":"Prénom","type":"TEXT","requis":true},
                                   {"code":"dateNaissance","libelle":"Date de naissance","type":"DATE","requis":true}
                                 ]$tg$::jsonb,
                                 $tgdoc$[
                                   {"face":"RECTO","libelle":"Pièce recto","requis":true},
                                   {"face":"VERSO","libelle":"Pièce verso","requis":true}
                                 ]$tgdoc$::jsonb),
                                ('GW', 'Guinée-Bissau', 'XOF',
                                 '["MONEY","BANK"]'::jsonb,
                                 $gw$[
                                   {"code":"nom","libelle":"Nom","type":"TEXT","requis":true},
                                   {"code":"prenom","libelle":"Prénom","type":"TEXT","requis":true},
                                   {"code":"dateNaissance","libelle":"Date de naissance","type":"DATE","requis":true}
                                 ]$gw$::jsonb,
                                 $gwdoc$[
                                   {"face":"RECTO","libelle":"Pièce recto","requis":true},
                                   {"face":"VERSO","libelle":"Pièce verso","requis":true}
                                 ]$gwdoc$::jsonb),
                                ('GN', 'Guinée', 'GNF',
                                 '["MONEY","BANK"]'::jsonb,
                                 $gn$[
                                   {"code":"nom","libelle":"Nom","type":"TEXT","requis":true},
                                   {"code":"prenom","libelle":"Prénom","type":"TEXT","requis":true},
                                   {"code":"dateNaissance","libelle":"Date de naissance","type":"DATE","requis":true},
                                   {"code":"numeroPiece","libelle":"Numéro de la pièce","type":"TEXT","requis":true}
                                 ]$gn$::jsonb,
                                 $gndoc$[
                                   {"face":"RECTO","libelle":"Carte d'identité","requis":true}
                                 ]$gndoc$::jsonb),
                                ('MA', 'Maroc', 'MAD',
                                 '["MONEY","BANK"]'::jsonb,
                                 $ma$[
                                   {"code":"nom","libelle":"Nom","type":"TEXT","requis":true},
                                   {"code":"prenom","libelle":"Prénom","type":"TEXT","requis":true},
                                   {"code":"dateNaissance","libelle":"Date de naissance","type":"DATE","requis":true},
                                   {"code":"numeroPiece","libelle":"Numéro CIN","type":"TEXT","requis":true}
                                 ]$ma$::jsonb,
                                 $madoc$[
                                   {"face":"RECTO","libelle":"CIN recto","requis":true},
                                   {"face":"VERSO","libelle":"CIN verso","requis":true}
                                 ]$madoc$::jsonb),
                                ('TD', 'Tchad', 'XAF',
                                 '["MONEY","BANK"]'::jsonb,
                                 $td$[
                                   {"code":"nom","libelle":"Nom","type":"TEXT","requis":true},
                                   {"code":"prenom","libelle":"Prénom","type":"TEXT","requis":true},
                                   {"code":"dateNaissance","libelle":"Date de naissance","type":"DATE","requis":true}
                                 ]$td$::jsonb,
                                 $tddoc$[
                                   {"face":"RECTO","libelle":"Pièce recto","requis":true},
                                   {"face":"VERSO","libelle":"Pièce verso","requis":true}
                                 ]$tddoc$::jsonb)
                        ) AS p(code, nom, devise, maisons, champs, documents)
               )
       ),
       NOW(),
       NOW()
FROM catalog_de_service s
WHERE s.code = 'ENROLEMENT'
  AND NOT EXISTS (
      SELECT 1 FROM catalog_de_service_version v
      WHERE v.catalog_de_service_id = s.id AND v.version = '1.0'
  );

UPDATE catalog_de_service s
SET status = 'PUBLISHED',
    current_version_id = v.id,
    updated_at = NOW()
FROM catalog_de_service_version v
WHERE v.catalog_de_service_id = s.id
  AND s.code = 'ENROLEMENT'
  AND v.version = '1.0';

-- Le workflow et les formulaires sont ceux que le catalogue affiche.
UPDATE catalog_de_service_version v
SET workflow_key = 'enrolement',
    configuration = v.configuration || jsonb_build_object(
        'steps', jsonb_build_array(
            'PORTEFEUILLE', 'COMPTE', 'TELEPHONE', 'CODE', 'PROPOSER', 'PAYS', 'IDENTITE', 'PIECE'
        ),
        'bindings', jsonb_build_array(
            jsonb_build_object('step', 'PORTEFEUILLE', 'kind', 'UI', 'formSource', 'FORM_KEY', 'formKey', 'enrolement-portefeuille'),
            jsonb_build_object('step', 'COMPTE', 'kind', 'UI', 'formSource', 'FORM_KEY', 'formKey', 'enrolement-compte'),
            jsonb_build_object('step', 'TELEPHONE', 'kind', 'UI', 'formSource', 'FORM_KEY', 'formKey', 'enrolement-telephone'),
            jsonb_build_object('step', 'CODE', 'kind', 'UI', 'formSource', 'FORM_KEY', 'formKey', 'enrolement-code'),
            jsonb_build_object('step', 'PROPOSER', 'kind', 'UI', 'formSource', 'FORM_KEY', 'formKey', 'enrolement-proposer'),
            jsonb_build_object('step', 'PAYS', 'kind', 'UI', 'formSource', 'FORM_KEY', 'formKey', 'enrolement-pays'),
            jsonb_build_object('step', 'IDENTITE', 'kind', 'UI', 'formSource', 'FORM_KEY', 'formKey', 'enrolement-identite'),
            jsonb_build_object('step', 'IDENTITE', 'kind', 'UI', 'formSource', 'FORM_KEY', 'formKey', 'enrolement-identite-ci', 'providerCode', 'CI'),
            jsonb_build_object('step', 'IDENTITE', 'kind', 'UI', 'formSource', 'FORM_KEY', 'formKey', 'enrolement-identite-sn', 'providerCode', 'SN'),
            jsonb_build_object('step', 'IDENTITE', 'kind', 'UI', 'formSource', 'FORM_KEY', 'formKey', 'enrolement-identite-gn', 'providerCode', 'GN'),
            jsonb_build_object('step', 'IDENTITE', 'kind', 'UI', 'formSource', 'FORM_KEY', 'formKey', 'enrolement-identite-ma', 'providerCode', 'MA'),
            jsonb_build_object('step', 'PIECE', 'kind', 'UI', 'formSource', 'FORM_KEY', 'formKey', 'enrolement-piece'),
            jsonb_build_object('step', 'PIECE', 'kind', 'UI', 'formSource', 'FORM_KEY', 'formKey', 'enrolement-piece-gn', 'providerCode', 'GN')
        )
    ),
    updated_at = NOW()
FROM catalog_de_service s
WHERE v.catalog_de_service_id = s.id
  AND s.code = 'ENROLEMENT'
  AND v.version = '1.0';
