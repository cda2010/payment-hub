-- BILL_PAYMENT : V79 a posé actorRole APPROVER / INITIATOR hors référentiel
-- CatalogProductRoleCode (PAYER, BILLER, …). La validation casting refuse ces codes.
-- APPROVAL_L* : formKey partagé suffit (comme ACCOUNT_TRANSFER) — pas d'actorRole.
-- correctBill : acteur = PAYER (rôle déclaré sur le produit).

UPDATE catalog_de_service_version v
SET configuration = jsonb_set(
        v.configuration,
        '{bindings}',
        (
            SELECT COALESCE(jsonb_agg(
                                   CASE
                                       WHEN b ->> 'step' IN ('APPROVAL_L1', 'APPROVAL_L2', 'APPROVAL_L3')
                                           THEN b - 'actorRole'
                                       WHEN b ->> 'step' = 'correctBill'
                                                AND b ->> 'actorRole' IN ('INITIATOR', 'APPROVER')
                                           THEN jsonb_set(b, '{actorRole}', '"PAYER"'::jsonb, true)
                                       ELSE b
                                       END
                               ORDER BY ordinality),
                           '[]'::jsonb)
            FROM jsonb_array_elements(COALESCE(v.configuration -> 'bindings', '[]'::jsonb))
                     WITH ORDINALITY AS t(b, ordinality)
        ),
        true
    ),
    updated_at = NOW()
FROM catalog_de_service s
WHERE v.catalog_de_service_id = s.id
  AND s.code = 'BILL_PAYMENT'
  AND v.status IN ('PUBLISHED', 'DRAFT', 'VALIDATED')
  AND EXISTS (
        SELECT 1
        FROM jsonb_array_elements(COALESCE(v.configuration -> 'bindings', '[]'::jsonb)) b
        WHERE b ->> 'actorRole' IN ('APPROVER', 'INITIATOR')
    );
