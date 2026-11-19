-- L'étiquette nomme le rôle du compte derrière l'alias (CPTEOPERATION, et les autres).
-- Un propriétaire n'a qu'un alias actif par étiquette. POINT_VENTE est le comptoir du réseau.

ALTER TABLE account_alias
    ADD COLUMN etiquette VARCHAR(40);

ALTER TABLE account_alias DROP CONSTRAINT ck_account_alias_owner_kind;

ALTER TABLE account_alias
    ADD CONSTRAINT ck_account_alias_owner_kind
        CHECK (owner_kind IN ('PARTY', 'ORG', 'UNITE', 'POINT_VENTE'));

CREATE UNIQUE INDEX uq_account_alias_etiquette
    ON account_alias (tenant_id, owner_kind, owner_id, etiquette)
    WHERE etiquette IS NOT NULL AND active = TRUE;
