-- Un propriétaire n'a qu'un alias actif marqué compte par défaut.

ALTER TABLE account_alias
    ADD COLUMN par_defaut BOOLEAN NOT NULL DEFAULT FALSE;

CREATE UNIQUE INDEX uq_account_alias_defaut
    ON account_alias (tenant_id, owner_kind, owner_id)
    WHERE par_defaut = TRUE AND active = TRUE;
