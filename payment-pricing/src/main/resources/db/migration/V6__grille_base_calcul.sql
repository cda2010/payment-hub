-- Base de calcul (assiette) des frais sur la grille + période / scope pour les cumuls.

ALTER TABLE grille_tarifaire
    ADD COLUMN IF NOT EXISTS base_calcul VARCHAR(32) NOT NULL DEFAULT 'TRANSACTION_AMOUNT',
    ADD COLUMN IF NOT EXISTS cumul_period VARCHAR(32),
    ADD COLUMN IF NOT EXISTS cumul_scope VARCHAR(32);

COMMENT ON COLUMN grille_tarifaire.base_calcul IS
    'Assiette : TRANSACTION_AMOUNT | CONVERTED_AMOUNT | CUMUL_AMOUNT | CUMUL_COUNT | CUSTOM';
COMMENT ON COLUMN grille_tarifaire.cumul_period IS
    'Obligatoire si base_calcul = CUMUL_* : DAY | WEEK | MONTH | YEAR | ROLLING_24H | ROLLING_30D | CUSTOM';
COMMENT ON COLUMN grille_tarifaire.cumul_scope IS
    'Obligatoire si base_calcul = CUMUL_* : CUSTOMER | WALLET | ACCOUNT | AGENT | MERCHANT | TILL | UNITE';
