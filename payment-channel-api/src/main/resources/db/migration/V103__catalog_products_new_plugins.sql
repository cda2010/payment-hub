-- Catalogue produits pour les nouveaux plugins mobile (démo / parcours locaux).

DO $$
DECLARE
  r RECORD;
  cat_id UUID;
  svc_id UUID;
  ver_id UUID;
BEGIN
  SELECT id INTO cat_id FROM categorie_service WHERE code = 'PAYMENT' LIMIT 1;
  IF cat_id IS NULL THEN
    SELECT id INTO cat_id FROM categorie_service WHERE code = 'SAVINGS' LIMIT 1;
  END IF;
  IF cat_id IS NULL THEN
    SELECT id INTO cat_id FROM categorie_service LIMIT 1;
  END IF;

  FOR r IN
    SELECT * FROM (VALUES
      ('DEMANDE_ARGENT', 'Demander de l’argent', 'Créer une demande de paiement', 'DEMANDE_ARGENT_HOME'),
      ('SPLIT', 'Partage de frais', 'Diviser une dépense entre plusieurs personnes', 'SPLIT_HOME'),
      ('INSIGHTS', 'Insights dépenses', 'Analyse des dépenses et catégories', 'INSIGHTS_HOME'),
      ('CARTE_CONTROLES', 'Contrôles carte', 'Gel, plafonds et carte virtuelle', 'CARTE_CONTROLES_HOME'),
      ('MARCHAND', 'Espace marchand', 'QR till et encaissements', 'MARCHAND_HOME'),
      ('SCOLARITE', 'Frais scolaires', 'Paiement des frais de scolarité', 'SCOLARITE_HOME'),
      ('COTISATIONS', 'Cotisations & associations', 'Cotisations de clubs et associations', 'COTISATIONS_HOME'),
      ('DIASPORA', 'Recevoir un envoi', 'Réception d’envois diaspora', 'DIASPORA_HOME'),
      ('ABONNEMENTS', 'Abonnements & prélèvements', 'Gérer mandats et abonnements', 'ABONNEMENTS_HOME'),
      ('EPARGNE_CHALLENGE', 'Défi épargne', 'Objectifs et challenges d’épargne', 'EPARGNE_CHALLENGE_HOME'),
      ('ALERTES', 'Alertes intelligentes', 'Seuils et notifications métier', 'ALERTES_HOME'),
      ('SUPPORT', 'Aide & support', 'Chat support in-app', 'SUPPORT_HOME')
    ) AS t(code, name, description, journey)
  LOOP
    IF NOT EXISTS (SELECT 1 FROM catalog_de_service WHERE code = r.code) THEN
      svc_id := gen_random_uuid();
      INSERT INTO catalog_de_service (
        id, code, name, description, status, created_at, updated_at, categorie_id
      ) VALUES (
        svc_id, r.code, r.name, r.description, 'PUBLISHED', NOW(), NOW(), cat_id
      );

      ver_id := gen_random_uuid();
      INSERT INTO catalog_de_service_version (
        id, catalog_de_service_id, version, status, workflow_key, configuration, created_at, updated_at
      ) VALUES (
        ver_id, svc_id, '1.0', 'PUBLISHED', NULL,
        jsonb_build_object(
          'serviceType', 'PAYEMENT',
          'journeyCode', r.journey,
          'supportedChannels', jsonb_build_array('MOBILE'),
          'defaultChannel', 'MOBILE'
        ),
        NOW(), NOW()
      );

      UPDATE catalog_de_service
      SET current_version_id = ver_id, updated_at = NOW()
      WHERE id = svc_id;
    END IF;
  END LOOP;
END $$;
