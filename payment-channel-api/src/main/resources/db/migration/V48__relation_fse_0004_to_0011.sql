-- Corridor inverse Coris Bank (0004) → Coris Money (0011)
-- Complète relation_fse / compte_fse (LORO/MIROIR) déjà présents pour 0011→0004.

INSERT INTO relation_fse (id, fse_source, fse_destination, service_type, created_at)
SELECT 'd0000001-0000-4000-8000-000000000002', '0004', '0011', 'TRANSFERT', NOW()
WHERE NOT EXISTS (
    SELECT 1 FROM relation_fse
    WHERE fse_source = '0004' AND fse_destination = '0011'
      AND COALESCE(service_type, '') = 'TRANSFERT'
);

-- Banque source : crédite le miroir CBI (pont vers Money) — typé CPTELORO pour le schéma DIFF_FSE
INSERT INTO compte_fse (id, numero_compte, type_compte_code, fse_teneur, relation_id, service_type, created_at)
SELECT 'e0000001-0000-4000-8000-000000000003',
       '00047324101',
       'CPTELORO',
       '0004',
       r.id,
       'TRANSFERT',
       NOW()
FROM relation_fse r
WHERE r.fse_source = '0004' AND r.fse_destination = '0011'
  AND NOT EXISTS (
      SELECT 1 FROM compte_fse c
      WHERE c.type_compte_code = 'CPTELORO' AND c.fse_teneur = '0004'
        AND c.relation_id = r.id
  );

-- Money dest : débite le LORO CBI chez Money — typé CPTEMIROIR pour le schéma DIFF_FSE
INSERT INTO compte_fse (id, numero_compte, type_compte_code, fse_teneur, relation_id, service_type, created_at)
SELECT 'e0000001-0000-4000-8000-000000000004',
       '000000279',
       'CPTEMIROIR',
       '0011',
       r.id,
       'TRANSFERT',
       NOW()
FROM relation_fse r
WHERE r.fse_source = '0004' AND r.fse_destination = '0011'
  AND NOT EXISTS (
      SELECT 1 FROM compte_fse c
      WHERE c.type_compte_code = 'CPTEMIROIR' AND c.fse_teneur = '0011'
        AND c.relation_id = r.id
  );
