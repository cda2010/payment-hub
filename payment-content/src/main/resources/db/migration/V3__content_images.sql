-- Images des contenus (URL publique ou chemin servi par /api/v1/content/media/*).

ALTER TABLE content_item
    ADD COLUMN image_url VARCHAR(1024),
    ADD COLUMN image_alt VARCHAR(255);
