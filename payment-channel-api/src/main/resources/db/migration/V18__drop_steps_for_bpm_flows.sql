-- Les parcours BPMN n'utilisent plus payment_flow_step comme source d'orchestration.
-- Les formulaires sont rattachés via flowable:formKey → modules/*/forms/{key}.json

DELETE FROM payment_flow_step
WHERE flow_id IN (
    SELECT id FROM payment_flow WHERE bpm_process_key IS NOT NULL AND bpm_process_key <> ''
);
