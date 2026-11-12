-- Catalogue partagé par Insights et Budget. Le code est le lien.

CREATE TABLE categorie_depense (
    code      VARCHAR(40) PRIMARY KEY,
    label     VARCHAR(80) NOT NULL,
    icon      VARCHAR(64) NOT NULL,
    priority  INT         NOT NULL
);

INSERT INTO categorie_depense (code, label, icon, priority) VALUES
    ('alimentation', 'Alimentation', 'restaurant-outline', 10),
    ('transport', 'Transport', 'car-outline', 20),
    ('loisirs', 'Loisirs', 'game-controller-outline', 30),
    ('factures', 'Factures', 'receipt-outline', 40),
    ('sante', 'Santé', 'medkit-outline', 50),
    ('autre', 'Autre', 'ellipsis-horizontal-outline', 90);
