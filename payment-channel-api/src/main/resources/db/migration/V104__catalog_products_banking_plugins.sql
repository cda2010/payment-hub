-- Catalogue produits plugins banque (démo mobile).

DO $$
DECLARE
  r RECORD;
  cat_id UUID;
  svc_id UUID;
  ver_id UUID;
BEGIN
  SELECT id INTO cat_id FROM categorie_service WHERE code = 'ACCOUNT' LIMIT 1;
  IF cat_id IS NULL THEN
    SELECT id INTO cat_id FROM categorie_service WHERE code = 'PAYMENT' LIMIT 1;
  END IF;
  IF cat_id IS NULL THEN
    SELECT id INTO cat_id FROM categorie_service LIMIT 1;
  END IF;

  FOR r IN
    SELECT * FROM (VALUES
      ('RELEVES', 'Relevés bancaires', 'E-relevés et téléchargement PDF', 'RELEVES_HOME'),
      ('RIB', 'RIB & IBAN', 'Coordonnées bancaires et partage', 'RIB_HOME'),
      ('DECOUVERT', 'Découvert', 'Autorisation et utilisation du découvert', 'DECOUVERT_HOME'),
      ('LIVRET', 'Livret & intérêts', 'Taux, intérêts et simulation', 'LIVRET_HOME'),
      ('CHEQUIER_NUM', 'Chéquier numérique', 'Demande et opposition de chèques', 'CHEQUIER_NUM_HOME'),
      ('MULTI_CARTES', 'Mes cartes', 'Cartes physiques et virtuelles', 'MULTI_CARTES_HOME'),
      ('VALIDATION_3DS', 'Validation paiements', 'Approbation 3DS / SCA', 'VALIDATION_3DS_HOME'),
      ('WALLET_PAY', 'Paiement mobile', 'Apple Pay / Google Pay', 'WALLET_PAY_HOME'),
      ('PLAFONDS_CARTE', 'Plafonds carte', 'Plafonds ATM, POS et e-commerce', 'PLAFONDS_CARTE_HOME'),
      ('CREDIT_PERSO', 'Crédit personnel', 'Simulation et demande de prêt', 'CREDIT_PERSO_HOME'),
      ('CREDIT_IMMO', 'Crédit immobilier', 'Suivi des échéances immobilières', 'CREDIT_IMMO_HOME'),
      ('MICRO_CREDIT', 'Micro-crédit', 'Score et offre de micro-crédit', 'MICRO_CREDIT_HOME'),
      ('PLACEMENTS', 'Placements', 'Catalogue OPCVM et souscription', 'PLACEMENTS_HOME'),
      ('KYC', 'Mon dossier KYC', 'Identité et mise à jour dossier', 'KYC_HOME'),
      ('RDV_AGENCE', 'Rendez-vous agence', 'Prise de rendez-vous en agence', 'RDV_AGENCE_HOME'),
      ('RECLAMATIONS', 'Réclamations', 'Tickets de réclamation formels', 'RECLAMATIONS_HOME'),
      ('SECURITE_COMPTE', 'Sécurité & appareils', 'Appareils, biométrie et consentements', 'SECURITE_COMPTE_HOME'),
      ('AGENCES_GAB', 'Agences & GAB', 'Localiser agences et distributeurs', 'AGENCES_GAB_HOME'),
      ('DEPOT_MOBILE', 'Dépôt mobile', 'Dépôt cash ou chèque photo', 'DEPOT_MOBILE_HOME'),
      ('RETRAIT_CODE', 'Retrait sans carte', 'Code de retrait GAB', 'RETRAIT_CODE_HOME')
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
