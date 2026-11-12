-- Les rails de transfert viennent de l'omnicanal, plus de ce référentiel local.
ALTER TABLE transfer_corridor DROP CONSTRAINT IF EXISTS transfer_corridor_emitter_participant_id_fkey;
ALTER TABLE transfer_corridor DROP CONSTRAINT IF EXISTS transfer_corridor_beneficiary_participant_id_fkey;
ALTER TABLE transfer_corridor DROP COLUMN IF EXISTS emitter_participant_id;
ALTER TABLE transfer_corridor DROP COLUMN IF EXISTS beneficiary_participant_id;
DROP TABLE IF EXISTS transfer_participant;
