-- Le regroupement entre dans l'arbre : il se rattache à une unité de n'importe quel niveau (pays, FSE, agence,
-- master, marchand, sous-distributeur), dont il prend le niveau + 1 et le chemin. Ses membres sont pris dans la
-- branche de ce parent (parent compris). L'ancrage, qui jouait ce rôle, disparaît ; plus de regroupement à la racine.

ALTER TABLE unite_organisation DROP CONSTRAINT ck_unite_parent;

-- 1. L'ancrage devient le parent.
UPDATE unite_organisation SET parent_id = ancrage_id
 WHERE type = 'REGROUPEMENT' AND ancrage_id IS NOT NULL;

-- 2. Sans ancrage : le parent est le plus proche ascendant commun des membres (un membre lui-même s'il est seul).
--    Sans ascendant commun (membres dans des pays différents) ou sans membre, le regroupement ne peut pas entrer
--    dans l'arbre : il est supprimé avec ses appartenances.
DO $$
DECLARE
    r       RECORD;
    m       RECORD;
    commun  TEXT[];
    segs    TEXT[];
    n       INT;
BEGIN
    FOR r IN SELECT id, code FROM unite_organisation WHERE type = 'REGROUPEMENT' AND parent_id IS NULL LOOP
        commun := NULL;
        FOR m IN
            SELECT u.chemin FROM regroupement_membre rm JOIN unite_organisation u ON u.id = rm.unite_id
             WHERE rm.regroupement_id = r.id
        LOOP
            segs := array_remove(string_to_array(m.chemin, '/'), '');
            IF commun IS NULL THEN
                commun := segs;
            ELSE
                n := 0;
                WHILE n < least(array_length(commun, 1), array_length(segs, 1)) AND commun[n + 1] = segs[n + 1] LOOP
                    n := n + 1;
                END LOOP;
                commun := commun[1:n];
            END IF;
        END LOOP;

        IF commun IS NULL OR coalesce(array_length(commun, 1), 0) = 0 THEN
            RAISE NOTICE 'Regroupement % supprimé : ses membres n''ont pas d''ascendant commun.', r.code;
            DELETE FROM regroupement_membre WHERE regroupement_id = r.id;
            DELETE FROM unite_organisation WHERE id = r.id;
        ELSE
            UPDATE unite_organisation SET parent_id = commun[array_length(commun, 1)]::uuid WHERE id = r.id;
        END IF;
    END LOOP;
END $$;

-- 3. Niveau et chemin sous le parent (un regroupement n'a pas d'enfant : rien d'autre à recalculer).
UPDATE unite_organisation r
   SET niveau = p.niveau + 1,
       chemin = p.chemin || r.id || '/'
  FROM unite_organisation p
 WHERE r.type = 'REGROUPEMENT' AND p.id = r.parent_id;

-- 4. Contraintes : seul le pays est sans parent ; l'ancrage disparaît.
ALTER TABLE unite_organisation DROP CONSTRAINT ck_unite_ancrage;
ALTER TABLE unite_organisation DROP COLUMN ancrage_id;
ALTER TABLE unite_organisation ADD CONSTRAINT ck_unite_parent CHECK ((type = 'PAYS') = (parent_id IS NULL));

-- 5. Démonstration : un regroupement au niveau d'un master (l'équipe de sous-distributeurs de Master Sahel).
INSERT INTO unite_organisation (id, code, libelle, type, zone, parent_id, statut, niveau, chemin, created_by, updated_by)
SELECT '10000000-0000-0000-0000-000000000043', 'RG-EQUIPE-SAHEL', 'Équipe Sahel', 'REGROUPEMENT', ms.zone, ms.id,
       'ACTIF', ms.niveau + 1, ms.chemin || '10000000-0000-0000-0000-000000000043/', 'flyway', 'flyway'
  FROM unite_organisation ms
 WHERE ms.id = '10000000-0000-0000-0000-000000000021';

INSERT INTO regroupement_membre (id, regroupement_id, unite_id, ajoute_par)
SELECT v.id::uuid, '10000000-0000-0000-0000-000000000043', v.unite::uuid, 'flyway'
  FROM (VALUES ('30000000-0000-0000-0000-000000000006', '10000000-0000-0000-0000-000000000031'),
               ('30000000-0000-0000-0000-000000000007', '10000000-0000-0000-0000-000000000032')) AS v(id, unite)
 WHERE EXISTS (SELECT 1 FROM unite_organisation WHERE id = '10000000-0000-0000-0000-000000000043');
