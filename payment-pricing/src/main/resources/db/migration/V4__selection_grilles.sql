-- Sélection des grilles en base selon la position de l'opération, et repérage des grilles « voisines ».
-- Le parent de l'unité visée est une copie prise à l'enregistrement (rafraîchie à la publication) : il sert à
-- signaler plusieurs grilles sur des unités sœurs, qu'un regroupement remplacerait. Les grilles existantes le
-- recevront à leur prochaine version.
ALTER TABLE grille_tarifaire
    ADD COLUMN source_unite_parent_id      UUID,
    ADD COLUMN destination_unite_parent_id UUID;

CREATE INDEX idx_grille_source_unite ON grille_tarifaire (produit, devise, statut, source_unite_id)
    WHERE source_unite_id IS NOT NULL;
CREATE INDEX idx_grille_destination_unite ON grille_tarifaire (produit, devise, statut, destination_unite_id)
    WHERE destination_unite_id IS NOT NULL;
CREATE INDEX idx_grille_source_parent ON grille_tarifaire (produit, devise, source_unite_parent_id)
    WHERE source_unite_parent_id IS NOT NULL;
CREATE INDEX idx_grille_destination_parent ON grille_tarifaire (produit, devise, destination_unite_parent_id)
    WHERE destination_unite_parent_id IS NOT NULL;
