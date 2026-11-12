-- Seed des promos home (migration depuis mobile promo.data.ts).

INSERT INTO content_item (id, code, slot, enabled, priority, plugin_id, route, icon, tone, created_at, updated_at) VALUES
    ('a1000000-0000-4000-8000-000000000001', 'promo-loyalty',   'home.promo', TRUE, 50, 'fidelite',          '/fidelite',                'ribbon-outline',     'blue', NOW(), NOW()),
    ('a1000000-0000-4000-8000-000000000002', 'promo-parrainage','home.promo', TRUE, 40, 'parrainage',        '/parrainage',              'gift-outline',       'sand', NOW(), NOW()),
    ('a1000000-0000-4000-8000-000000000003', 'promo-challenge', 'home.promo', TRUE, 30, 'epargne-challenge', '/epargne-challenge',       'flag-outline',       'mint', NOW(), NOW()),
    ('a1000000-0000-4000-8000-000000000004', 'promo-credit',    'home.promo', TRUE, 20, 'credit-perso',      '/credit-perso-simulate',   'calculator-outline', 'navy', NOW(), NOW()),
    ('a1000000-0000-4000-8000-000000000005', 'promo-micro',     'home.promo', TRUE, 10, 'micro-credit',      '/micro-credit',            'pulse-outline',      'blue', NOW(), NOW());

INSERT INTO content_item_locale (id, item_id, locale, eyebrow, title, body, cta) VALUES
    ('b1000000-0000-4000-8000-000000000011', 'a1000000-0000-4000-8000-000000000001', 'fr',
     'Offre', 'Gagnez des points à chaque paiement',
     'Programme de fidélité Coris — cashback et récompenses.', 'Découvrir'),
    ('b1000000-0000-4000-8000-000000000012', 'a1000000-0000-4000-8000-000000000001', 'en',
     'Offer', 'Earn points with every payment',
     'Coris loyalty program — cashback and rewards.', 'Discover'),

    ('b1000000-0000-4000-8000-000000000021', 'a1000000-0000-4000-8000-000000000002', 'fr',
     'Partage', 'Invitez un proche, gagnez 1 000 pts',
     'Votre code AMINATA25 — vous et votre filleul êtes récompensés.', 'Parrainer'),
    ('b1000000-0000-4000-8000-000000000022', 'a1000000-0000-4000-8000-000000000002', 'en',
     'Share', 'Invite a friend, earn 1,000 pts',
     'Your code AMINATA25 — you and your invitee both get rewarded.', 'Refer'),

    ('b1000000-0000-4000-8000-000000000031', 'a1000000-0000-4000-8000-000000000003', 'fr',
     'Nouveau', 'Relevez un défi épargne',
     'Fixez un objectif et avancez jour après jour.', 'Commencer'),
    ('b1000000-0000-4000-8000-000000000032', 'a1000000-0000-4000-8000-000000000003', 'en',
     'New', 'Take on a savings challenge',
     'Set a goal and progress day by day.', 'Get started'),

    ('b1000000-0000-4000-8000-000000000041', 'a1000000-0000-4000-8000-000000000004', 'fr',
     'Crédit', 'Simulez votre prêt en 1 minute',
     'Mensualité claire, sans engagement.', 'Simuler'),
    ('b1000000-0000-4000-8000-000000000042', 'a1000000-0000-4000-8000-000000000004', 'en',
     'Credit', 'Simulate your loan in 1 minute',
     'Clear monthly payment, no commitment.', 'Simulate'),

    ('b1000000-0000-4000-8000-000000000051', 'a1000000-0000-4000-8000-000000000005', 'fr',
     'Pour vous', 'Une offre micro-crédit adaptée',
     'Basée sur votre activité Coris Money.', 'Voir mon score'),
    ('b1000000-0000-4000-8000-000000000052', 'a1000000-0000-4000-8000-000000000005', 'en',
     'For you', 'A tailored microcredit offer',
     'Based on your Coris Money activity.', 'See my score');
