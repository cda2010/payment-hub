-- ParametreUtilisateur : labels i18n + affiche (mapping IU RESPONSE)

ALTER TABLE parametre_utilisateur
    ADD COLUMN IF NOT EXISTS label_key VARCHAR(150),
    ADD COLUMN IF NOT EXISTS labels_json TEXT,
    ADD COLUMN IF NOT EXISTS affiche BOOLEAN NOT NULL DEFAULT TRUE;

COMMENT ON COLUMN parametre_utilisateur.label_key IS
    'Clé i18n (ex. bill.amount) — résolue via catalogue / Accept-Language';
COMMENT ON COLUMN parametre_utilisateur.labels_json IS
    'Libellés inline JSON {"fr":"...","en":"..."} — prioritaire sur label_key si présent';
COMMENT ON COLUMN parametre_utilisateur.affiche IS
    'Exposé UI / canal (aligné MParam.affiche)';

COMMENT ON COLUMN parametre_utilisateur.type_parametre IS
    'STRING | NUMBER | LIST | ENVELOPE — LIST = collection (toujours List, singleton inclus)';
