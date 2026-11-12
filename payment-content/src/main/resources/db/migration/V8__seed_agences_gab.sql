-- Rubrique + fiches agences & GAB (seed démo mobile).

INSERT INTO content_collection (
    id, code, kind, enabled, priority, icon, channels, audiences, schema_json, created_at, updated_at
) VALUES (
    'c3000000-0000-4000-8000-000000000001',
    'agences-gab',
    'DIRECTORY',
    TRUE,
    90,
    'business-outline',
    '["MOBILE","WEB"]'::jsonb,
    '["RETAIL","CORPORATE","AGENT"]'::jsonb,
    '{
      "fields": [
        {"key":"type","type":"enum","label":"Type","values":["BRANCH","ATM"]},
        {"key":"address","type":"string","label":"Adresse"},
        {"key":"city","type":"string","label":"Ville"},
        {"key":"phone","type":"string","label":"Téléphone"},
        {"key":"lat","type":"number","label":"Latitude"},
        {"key":"lng","type":"number","label":"Longitude"},
        {"key":"openNow","type":"boolean","label":"Ouvert maintenant"},
        {"key":"hours","type":"string","label":"Horaires"},
        {"key":"services","type":"string[]","label":"Services"}
      ]
    }'::jsonb,
    NOW(),
    NOW()
) ON CONFLICT (code) DO NOTHING;

INSERT INTO content_collection_locale (id, collection_id, locale, title, description) VALUES
    ('c3000000-0000-4000-8000-000000000011', 'c3000000-0000-4000-8000-000000000001', 'fr',
     'Agences & GAB', 'Localiser agences et distributeurs automatiques'),
    ('c3000000-0000-4000-8000-000000000012', 'c3000000-0000-4000-8000-000000000001', 'en',
     'Branches & ATMs', 'Find branches and cash machines')
ON CONFLICT DO NOTHING;

INSERT INTO content_document (
    id, collection_id, code, enabled, priority, icon, attrs, channels, audiences, created_at, updated_at
) VALUES
('d3000000-0000-4000-8000-000000000001', 'c3000000-0000-4000-8000-000000000001', 'ag-1', TRUE, 70, 'business-outline',
 '{"type":"BRANCH","address":"12 Avenue Chardy","city":"Abidjan","phone":"+225 27 20 30 40 50","lat":5.3197,"lng":-4.0127,"openNow":true,"hours":"08:00–16:30","services":["Retrait","Dépôt","Change","Conseiller"]}'::jsonb,
 '[]'::jsonb, '[]'::jsonb, NOW(), NOW()),
('d3000000-0000-4000-8000-000000000002', 'c3000000-0000-4000-8000-000000000001', 'ag-2', TRUE, 65, 'card-outline',
 '{"type":"ATM","address":"Carrefour Angré 8e tranche","city":"Abidjan","phone":"","lat":5.359,"lng":-3.987,"openNow":true,"hours":"24h/24","services":["Retrait","Consultation solde"]}'::jsonb,
 '[]'::jsonb, '[]'::jsonb, NOW(), NOW()),
('d3000000-0000-4000-8000-000000000003', 'c3000000-0000-4000-8000-000000000001', 'ag-3', TRUE, 40, 'business-outline',
 '{"type":"BRANCH","address":"Boulevard de la République","city":"Bouaké","phone":"+225 27 31 20 30 40","lat":7.6939,"lng":-5.0303,"openNow":false,"hours":"08:00–16:00","services":["Retrait","Dépôt","Crédit"]}'::jsonb,
 '[]'::jsonb, '[]'::jsonb, NOW(), NOW()),
('d3000000-0000-4000-8000-000000000004', 'c3000000-0000-4000-8000-000000000001', 'ag-4', TRUE, 60, 'card-outline',
 '{"type":"ATM","address":"Rue du Canal, Zone 4","city":"Abidjan","phone":"","lat":5.3012,"lng":-3.9821,"openNow":true,"hours":"24h/24","services":["Retrait"]}'::jsonb,
 '[]'::jsonb, '[]'::jsonb, NOW(), NOW()),
('d3000000-0000-4000-8000-000000000005', 'c3000000-0000-4000-8000-000000000001', 'ag-5', TRUE, 55, 'business-outline',
 '{"type":"BRANCH","address":"Avenue Houphouët-Boigny","city":"Yamoussoukro","phone":"+225 27 30 50 60 70","lat":6.8276,"lng":-5.2893,"openNow":true,"hours":"08:00–16:30","services":["Retrait","Dépôt","Virement"]}'::jsonb,
 '[]'::jsonb, '[]'::jsonb, NOW(), NOW()),
('d3000000-0000-4000-8000-000000000006', 'c3000000-0000-4000-8000-000000000001', 'ag-6', TRUE, 50, 'card-outline',
 '{"type":"ATM","address":"Zone portuaire","city":"San-Pédro","phone":"","lat":4.7485,"lng":-6.6363,"openNow":true,"hours":"24h/24","services":["Retrait","Consultation solde"]}'::jsonb,
 '[]'::jsonb, '[]'::jsonb, NOW(), NOW())
ON CONFLICT (code) DO NOTHING;

INSERT INTO content_document_locale (id, document_id, locale, title, summary, body) VALUES
('e3000000-0000-4000-8000-000000000011', 'd3000000-0000-4000-8000-000000000001', 'fr', 'Agence Plateau', 'Agence · Abidjan', 'Agence principale Plateau'),
('e3000000-0000-4000-8000-000000000012', 'd3000000-0000-4000-8000-000000000001', 'en', 'Plateau Branch', 'Branch · Abidjan', 'Main Plateau branch'),
('e3000000-0000-4000-8000-000000000021', 'd3000000-0000-4000-8000-000000000002', 'fr', 'GAB Cocody Angré', 'GAB · Abidjan', 'Distributeur Angré'),
('e3000000-0000-4000-8000-000000000022', 'd3000000-0000-4000-8000-000000000002', 'en', 'Cocody Angré ATM', 'ATM · Abidjan', 'Angré cash machine'),
('e3000000-0000-4000-8000-000000000031', 'd3000000-0000-4000-8000-000000000003', 'fr', 'Agence Bouaké Centre', 'Agence · Bouaké', 'Agence Bouaké'),
('e3000000-0000-4000-8000-000000000032', 'd3000000-0000-4000-8000-000000000003', 'en', 'Bouaké Centre Branch', 'Branch · Bouaké', 'Bouaké branch'),
('e3000000-0000-4000-8000-000000000041', 'd3000000-0000-4000-8000-000000000004', 'fr', 'GAB Marcory Zone 4', 'GAB · Abidjan', 'Distributeur Zone 4'),
('e3000000-0000-4000-8000-000000000042', 'd3000000-0000-4000-8000-000000000004', 'en', 'Marcory Zone 4 ATM', 'ATM · Abidjan', 'Zone 4 cash machine'),
('e3000000-0000-4000-8000-000000000051', 'd3000000-0000-4000-8000-000000000005', 'fr', 'Agence Yamoussoukro', 'Agence · Yamoussoukro', 'Agence Yamoussoukro'),
('e3000000-0000-4000-8000-000000000052', 'd3000000-0000-4000-8000-000000000005', 'en', 'Yamoussoukro Branch', 'Branch · Yamoussoukro', 'Yamoussoukro branch'),
('e3000000-0000-4000-8000-000000000061', 'd3000000-0000-4000-8000-000000000006', 'fr', 'GAB San-Pédro Port', 'GAB · San-Pédro', 'Distributeur portuaire'),
('e3000000-0000-4000-8000-000000000062', 'd3000000-0000-4000-8000-000000000006', 'en', 'San-Pédro Port ATM', 'ATM · San-Pédro', 'Port cash machine')
ON CONFLICT DO NOTHING;
