-- Aligner les profils TARIFF CASH_OUT sur la grille FEE-CASH-OUT.

UPDATE identity_profile_grid
SET grille_code = 'FEE-CASH-OUT'
WHERE produit_code = 'CASH_OUT'
  AND grille_code = 'FEE-CASH-AGENT';
