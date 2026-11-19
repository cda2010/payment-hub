-- Le programme appartient à un FSE du réseau (unite_organisation de type FSE).
-- Les programmes déjà créés restent sans FSE jusqu'à une nouvelle création.

ALTER TABLE programme ADD COLUMN fse_id UUID;
ALTER TABLE programme ADD COLUMN fse_nom VARCHAR(160);

CREATE INDEX programme_fse_idx ON programme (fse_id);
