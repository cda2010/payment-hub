-- Produit catalogue PAIEMENT_EN_MASSE (virements groupés — corporate WEB).

DO $$
DECLARE
  cat_id UUID;
  svc_id UUID;
  ver_id UUID;
BEGIN
  SELECT id INTO cat_id FROM categorie_service WHERE code = 'TRANSFER' LIMIT 1;
  IF cat_id IS NULL THEN
    SELECT id INTO cat_id FROM categorie_service WHERE code = 'PAYMENT' LIMIT 1;
  END IF;
  IF cat_id IS NULL THEN
    SELECT id INTO cat_id FROM categorie_service LIMIT 1;
  END IF;

  IF NOT EXISTS (SELECT 1 FROM catalog_de_service WHERE code = 'PAIEMENT_EN_MASSE') THEN
    svc_id := gen_random_uuid();
    INSERT INTO catalog_de_service (
      id, code, name, description, status, created_at, updated_at, categorie_id
    ) VALUES (
      svc_id,
      'PAIEMENT_EN_MASSE',
      'Paiement en masse',
      'Virements groupés (salaires, fournisseurs) depuis un fichier ou une liste',
      'PUBLISHED',
      NOW(),
      NOW(),
      cat_id
    );

    ver_id := gen_random_uuid();
    INSERT INTO catalog_de_service_version (
      id, catalog_de_service_id, version, status, workflow_key, configuration, created_at, updated_at
    ) VALUES (
      ver_id,
      svc_id,
      '1.0',
      'PUBLISHED',
      NULL,
      jsonb_build_object(
        'serviceType', 'TRANSFERT',
        'journeyCode', 'PAIEMENT_EN_MASSE_HOME',
        'supportedChannels', jsonb_build_array('WEB'),
        'defaultChannel', 'WEB',
        'supportedAudiences', jsonb_build_array('CORPORATE'),
        'defaultAudience', 'CORPORATE',
        'presentation', jsonb_build_object(
          'version', 1,
          'channel', 'WEB',
          'sections', jsonb_build_array()
        )
      ),
      NOW(),
      NOW()
    );

    UPDATE catalog_de_service
    SET current_version_id = ver_id, status = 'PUBLISHED', updated_at = NOW()
    WHERE id = svc_id;
  ELSE
    -- Idempotent : forcer audience CORPORATE + canal WEB sur la version courante
    UPDATE catalog_de_service_version v
    SET configuration = COALESCE(v.configuration, '{}'::jsonb)
        || jsonb_build_object(
               'supportedChannels', jsonb_build_array('WEB'),
               'defaultChannel', 'WEB',
               'supportedAudiences', jsonb_build_array('CORPORATE'),
               'defaultAudience', 'CORPORATE',
               'journeyCode', COALESCE(v.configuration ->> 'journeyCode', 'PAIEMENT_EN_MASSE_HOME'),
               'serviceType', COALESCE(v.configuration ->> 'serviceType', 'TRANSFERT')
           ),
        updated_at = NOW()
    FROM catalog_de_service s
    WHERE v.catalog_de_service_id = s.id
      AND s.code = 'PAIEMENT_EN_MASSE'
      AND v.status = 'PUBLISHED';
  END IF;
END $$;
