-- Supprime les IU typeEnvoi=RESPONSE (mapping déclaratif abandonné au profit du contrat BillResponse fixe).
-- parametre_utilisateur est en ON DELETE CASCADE.

DELETE FROM interface_utilisateur WHERE type_envoi = 'RESPONSE';
