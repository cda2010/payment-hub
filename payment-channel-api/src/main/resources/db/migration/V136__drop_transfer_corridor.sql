-- Couples émetteur/destinataire locaux retirés. Les rails viennent de l'omnicanal.
ALTER TABLE transfer_order DROP CONSTRAINT IF EXISTS transfer_order_corridor_id_fkey;
ALTER TABLE transfer_order DROP COLUMN IF EXISTS corridor_id;
DROP TABLE IF EXISTS transfer_corridor;
