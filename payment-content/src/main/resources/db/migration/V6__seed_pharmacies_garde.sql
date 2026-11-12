-- Rubrique + fiches pharmacies de garde (seed démo mobile).

INSERT INTO content_collection (
    id, code, kind, enabled, priority, icon, channels, audiences, schema_json, created_at, updated_at
) VALUES (
    'c1000000-0000-4000-8000-000000000001',
    'pharmacies-garde',
    'DIRECTORY',
    TRUE,
    100,
    'medkit-outline',
    '["MOBILE","WEB"]'::jsonb,
    '["RETAIL","CORPORATE","AGENT"]'::jsonb,
    '{
      "fields": [
        {"key":"address","type":"string","label":"Adresse"},
        {"key":"city","type":"string","label":"Ville"},
        {"key":"district","type":"string","label":"Quartier"},
        {"key":"phone","type":"string","label":"Téléphone"},
        {"key":"lat","type":"number","label":"Latitude"},
        {"key":"lng","type":"number","label":"Longitude"},
        {"key":"onDuty","type":"boolean","label":"De garde"},
        {"key":"duty","type":"enum","label":"Type de garde","values":["TONIGHT","WEEKEND","DAY"]},
        {"key":"openUntil","type":"string","label":"Ouvert jusqu''à"},
        {"key":"services","type":"string[]","label":"Services"}
      ]
    }'::jsonb,
    NOW(),
    NOW()
) ON CONFLICT (code) DO NOTHING;

INSERT INTO content_collection_locale (id, collection_id, locale, title, description) VALUES
    ('c1000000-0000-4000-8000-000000000011', 'c1000000-0000-4000-8000-000000000001', 'fr',
     'Pharmacies de garde', 'Pharmacies ouvertes de nuit ou le week-end'),
    ('c1000000-0000-4000-8000-000000000012', 'c1000000-0000-4000-8000-000000000001', 'en',
     'On-duty pharmacies', 'Pharmacies open at night or on weekends')
ON CONFLICT DO NOTHING;

INSERT INTO content_document (
    id, collection_id, code, enabled, priority, icon, attrs, channels, audiences, created_at, updated_at
) VALUES
('d1000000-0000-4000-8000-000000000001', 'c1000000-0000-4000-8000-000000000001', 'ph-1', TRUE, 70, 'medkit-outline',
 '{"address":"Avenue Chardy, Imm. Alpha 2000","city":"Abidjan","district":"Plateau","phone":"+225 27 20 21 22 23","lat":5.3201,"lng":-4.0165,"onDuty":true,"duty":"TONIGHT","openUntil":"07:00","services":["Garde 24h","Ordonnance","Conseil"]}'::jsonb,
 '[]'::jsonb, '[]'::jsonb, NOW(), NOW()),
('d1000000-0000-4000-8000-000000000002', 'c1000000-0000-4000-8000-000000000001', 'ph-2', TRUE, 60, 'medkit-outline',
 '{"address":"Carrefour Angré 8e tranche","city":"Abidjan","district":"Cocody","phone":"+225 27 22 45 67 89","lat":5.3612,"lng":-3.9841,"onDuty":true,"duty":"TONIGHT","openUntil":"07:00","services":["Garde de nuit","Parapharmacie"]}'::jsonb,
 '[]'::jsonb, '[]'::jsonb, NOW(), NOW()),
('d1000000-0000-4000-8000-000000000003', 'c1000000-0000-4000-8000-000000000001', 'ph-3', TRUE, 50, 'medkit-outline',
 '{"address":"Boulevard VGE, près du canal","city":"Abidjan","district":"Marcory","phone":"+225 27 21 30 40 50","lat":5.2988,"lng":-3.9812,"onDuty":true,"duty":"WEEKEND","openUntil":"22:00","services":["Garde week-end","Ordonnance"]}'::jsonb,
 '[]'::jsonb, '[]'::jsonb, NOW(), NOW()),
('d1000000-0000-4000-8000-000000000004', 'c1000000-0000-4000-8000-000000000001', 'ph-4', TRUE, 20, 'medkit-outline',
 '{"address":"Avenue Principal Sicogi","city":"Abidjan","district":"Yopougon","phone":"+225 27 23 11 22 33","lat":5.336,"lng":-4.085,"onDuty":false,"duty":"DAY","openUntil":"20:00","services":["Jour","Conseil"]}'::jsonb,
 '[]'::jsonb, '[]'::jsonb, NOW(), NOW()),
('d1000000-0000-4000-8000-000000000005', 'c1000000-0000-4000-8000-000000000001', 'ph-5', TRUE, 55, 'medkit-outline',
 '{"address":"Boulevard de la République","city":"Bouaké","district":"Centre","phone":"+225 27 31 40 50 60","lat":7.6901,"lng":-5.0308,"onDuty":true,"duty":"TONIGHT","openUntil":"07:00","services":["Garde 24h","Ordonnance"]}'::jsonb,
 '[]'::jsonb, '[]'::jsonb, NOW(), NOW()),
('d1000000-0000-4000-8000-000000000006', 'c1000000-0000-4000-8000-000000000001', 'ph-6', TRUE, 45, 'medkit-outline',
 '{"address":"Avenue Houphouët-Boigny","city":"Yamoussoukro","district":"Centre","phone":"+225 27 30 60 70 80","lat":6.827,"lng":-5.289,"onDuty":true,"duty":"WEEKEND","openUntil":"22:00","services":["Garde week-end"]}'::jsonb,
 '[]'::jsonb, '[]'::jsonb, NOW(), NOW()),
('d1000000-0000-4000-8000-000000000007', 'c1000000-0000-4000-8000-000000000001', 'ph-7', TRUE, 40, 'medkit-outline',
 '{"address":"Quartier Port, face dock 3","city":"San-Pédro","district":"Port","phone":"+225 27 34 10 20 30","lat":4.749,"lng":-6.635,"onDuty":true,"duty":"TONIGHT","openUntil":"07:00","services":["Garde de nuit","Urgences"]}'::jsonb,
 '[]'::jsonb, '[]'::jsonb, NOW(), NOW())
ON CONFLICT (code) DO NOTHING;

INSERT INTO content_document_locale (id, document_id, locale, title, summary, body) VALUES
('e1000000-0000-4000-8000-000000000011', 'd1000000-0000-4000-8000-000000000001', 'fr', 'Pharmacie du Plateau', 'Plateau · Abidjan', 'Garde de nuit — Plateau'),
('e1000000-0000-4000-8000-000000000012', 'd1000000-0000-4000-8000-000000000001', 'en', 'Plateau Pharmacy', 'Plateau · Abidjan', 'Night duty — Plateau'),
('e1000000-0000-4000-8000-000000000021', 'd1000000-0000-4000-8000-000000000002', 'fr', 'Pharmacie Angré Sainte-Marie', 'Cocody · Abidjan', 'Garde de nuit — Angré'),
('e1000000-0000-4000-8000-000000000022', 'd1000000-0000-4000-8000-000000000002', 'en', 'Angré Sainte-Marie Pharmacy', 'Cocody · Abidjan', 'Night duty — Angré'),
('e1000000-0000-4000-8000-000000000031', 'd1000000-0000-4000-8000-000000000003', 'fr', 'Pharmacie Marcory Zone 4', 'Marcory · Abidjan', 'Garde week-end — Zone 4'),
('e1000000-0000-4000-8000-000000000032', 'd1000000-0000-4000-8000-000000000003', 'en', 'Marcory Zone 4 Pharmacy', 'Marcory · Abidjan', 'Weekend duty — Zone 4'),
('e1000000-0000-4000-8000-000000000041', 'd1000000-0000-4000-8000-000000000004', 'fr', 'Pharmacie Yopougon Sicogi', 'Yopougon · Abidjan', 'Ouverte en journée'),
('e1000000-0000-4000-8000-000000000042', 'd1000000-0000-4000-8000-000000000004', 'en', 'Yopougon Sicogi Pharmacy', 'Yopougon · Abidjan', 'Open daytime'),
('e1000000-0000-4000-8000-000000000051', 'd1000000-0000-4000-8000-000000000005', 'fr', 'Pharmacie Bouaké Centre', 'Centre · Bouaké', 'Garde de nuit — Bouaké'),
('e1000000-0000-4000-8000-000000000052', 'd1000000-0000-4000-8000-000000000005', 'en', 'Bouaké Centre Pharmacy', 'Centre · Bouaké', 'Night duty — Bouaké'),
('e1000000-0000-4000-8000-000000000061', 'd1000000-0000-4000-8000-000000000006', 'fr', 'Pharmacie Yamoussoukro Mairie', 'Centre · Yamoussoukro', 'Garde week-end'),
('e1000000-0000-4000-8000-000000000062', 'd1000000-0000-4000-8000-000000000006', 'en', 'Yamoussoukro City Hall Pharmacy', 'Centre · Yamoussoukro', 'Weekend duty'),
('e1000000-0000-4000-8000-000000000071', 'd1000000-0000-4000-8000-000000000007', 'fr', 'Pharmacie San-Pédro Port', 'Port · San-Pédro', 'Garde de nuit — Port'),
('e1000000-0000-4000-8000-000000000072', 'd1000000-0000-4000-8000-000000000007', 'en', 'San-Pédro Port Pharmacy', 'Port · San-Pédro', 'Night duty — Port')
ON CONFLICT DO NOTHING;
