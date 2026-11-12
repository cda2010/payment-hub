-- Rubrique articles / actualités (NEWS) — schéma distinct des pharmacies.

INSERT INTO content_collection (
    id, code, kind, enabled, priority, icon, channels, audiences, schema_json, created_at, updated_at
) VALUES (
    'c2000000-0000-4000-8000-000000000001',
    'actualites',
    'NEWS',
    TRUE,
    90,
    'newspaper-outline',
    '["MOBILE","WEB"]'::jsonb,
    '["RETAIL","CORPORATE","AGENT"]'::jsonb,
    '{
      "fields": [
        {"key":"author","type":"string","label":"Auteur"},
        {"key":"category","type":"enum","label":"Catégorie","values":["BANQUE","PRODUITS","SECURITE","COMMUNAUTE"]},
        {"key":"publishedAt","type":"string","label":"Date de parution (ISO)"},
        {"key":"coverUrl","type":"string","label":"URL couverture"},
        {"key":"tags","type":"string[]","label":"Tags"}
      ]
    }'::jsonb,
    NOW(),
    NOW()
) ON CONFLICT (code) DO NOTHING;

INSERT INTO content_collection_locale (id, collection_id, locale, title, description) VALUES
    ('c2000000-0000-4000-8000-000000000011', 'c2000000-0000-4000-8000-000000000001', 'fr',
     'Actualités', 'Articles et brèves Coris'),
    ('c2000000-0000-4000-8000-000000000012', 'c2000000-0000-4000-8000-000000000001', 'en',
     'News', 'Coris articles and briefs')
ON CONFLICT DO NOTHING;

INSERT INTO content_document (
    id, collection_id, code, enabled, priority, icon, attrs, channels, audiences, created_at, updated_at
) VALUES
('d2000000-0000-4000-8000-000000000001', 'c2000000-0000-4000-8000-000000000001', 'news-mobile-banking', TRUE, 30, 'newspaper-outline',
 '{"author":"Équipe Coris","category":"BANQUE","publishedAt":"2026-10-15","coverUrl":"","tags":["mobile","nouveautés"]}'::jsonb,
 '[]'::jsonb, '[]'::jsonb, NOW(), NOW()),
('d2000000-0000-4000-8000-000000000002', 'c2000000-0000-4000-8000-000000000001', 'news-fraude', TRUE, 20, 'newspaper-outline',
 '{"author":"Sécurité","category":"SECURITE","publishedAt":"2026-10-10","coverUrl":"","tags":["sécurité","conseils"]}'::jsonb,
 '[]'::jsonb, '[]'::jsonb, NOW(), NOW())
ON CONFLICT (code) DO NOTHING;

INSERT INTO content_document_locale (id, document_id, locale, title, summary, body) VALUES
('e2000000-0000-4000-8000-000000000011', 'd2000000-0000-4000-8000-000000000001', 'fr',
 'Nouveautés de l''app mobile',
 'Découvrez les dernières fonctionnalités.',
 'Nous avons enrichi l''accueil avec des publicités personnalisées et de nouveaux services.'),
('e2000000-0000-4000-8000-000000000012', 'd2000000-0000-4000-8000-000000000001', 'en',
 'Mobile app updates',
 'Discover the latest features.',
 'We enriched the home screen with personalized promos and new services.'),
('e2000000-0000-4000-8000-000000000021', 'd2000000-0000-4000-8000-000000000002', 'fr',
 'Protégez-vous contre la fraude',
 'Les bons réflexes à adopter.',
 'Ne communiquez jamais votre code PIN. Coris ne vous demandera jamais vos secrets par SMS.'),
('e2000000-0000-4000-8000-000000000022', 'd2000000-0000-4000-8000-000000000002', 'en',
 'Protect yourself from fraud',
 'Good habits to follow.',
 'Never share your PIN. Coris will never ask for secrets over SMS.')
ON CONFLICT DO NOTHING;
