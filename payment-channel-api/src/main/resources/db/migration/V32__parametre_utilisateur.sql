-- ParametreUtilisateur (constitues) rattaché à InterfaceUtilisateur — parité Omnicanal

CREATE TABLE parametre_utilisateur (
    id                          UUID PRIMARY KEY,
    interface_utilisateur_id    UUID         NOT NULL REFERENCES interface_utilisateur(id) ON DELETE CASCADE,
    nom_parametre               VARCHAR(150),
    code_parametre              VARCHAR(100) NOT NULL,
    code_parametre_methode      VARCHAR(100),
    type_parametre              VARCHAR(50),
    valeur                      VARCHAR(500),
    parametre_api               VARCHAR(30),
    attribut                    VARCHAR(100),
    ordre                       INT          NOT NULL DEFAULT 0,
    created_at                  TIMESTAMPTZ  NOT NULL DEFAULT NOW(),
    CONSTRAINT uk_parametre_utilisateur_iface_code UNIQUE (interface_utilisateur_id, code_parametre)
);

CREATE INDEX idx_parametre_utilisateur_iface ON parametre_utilisateur(interface_utilisateur_id);
