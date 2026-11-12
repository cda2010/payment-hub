-- Cibles réseau d'une grille : source et destination de l'opération. Chacune vaut « toutes » (colonnes vides),
-- une zone (zone seule) ou une unité d'organisation de payment-network, de n'importe quel type (zone de l'unité
-- recopiée, code, libellé et type gardés pour l'affichage). Pas de clé étrangère : les unités sont dans une autre base.

ALTER TABLE grille_tarifaire
    ADD COLUMN source_zone               VARCHAR(10),
    ADD COLUMN source_unite_id           UUID,
    ADD COLUMN source_unite_code         VARCHAR(40),
    ADD COLUMN source_unite_libelle      VARCHAR(150),
    ADD COLUMN source_unite_type         VARCHAR(20),
    ADD COLUMN destination_zone          VARCHAR(10),
    ADD COLUMN destination_unite_id      UUID,
    ADD COLUMN destination_unite_code    VARCHAR(40),
    ADD COLUMN destination_unite_libelle VARCHAR(150),
    ADD COLUMN destination_unite_type    VARCHAR(20),
    ADD CONSTRAINT ck_grille_source CHECK (source_unite_id IS NULL
        OR (source_zone IS NOT NULL AND source_unite_code IS NOT NULL AND source_unite_type IS NOT NULL)),
    ADD CONSTRAINT ck_grille_destination CHECK (destination_unite_id IS NULL
        OR (destination_zone IS NOT NULL AND destination_unite_code IS NOT NULL AND destination_unite_type IS NOT NULL));
