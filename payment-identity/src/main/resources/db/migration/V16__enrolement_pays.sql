-- Le pays choisit le formulaire publié par le produit catalogue ENROLEMENT.

ALTER TABLE enrolement ADD COLUMN pays VARCHAR(8);
ALTER TABLE enrolement ADD COLUMN complements JSONB;
