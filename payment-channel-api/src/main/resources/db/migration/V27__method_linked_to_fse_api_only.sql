-- Les méthodes Omnicanal sont liées à l'Api (fse_api_id), pas directement à l'environnement.
UPDATE provider_api
SET environment_id = NULL
WHERE fse_api_id IS NOT NULL;
