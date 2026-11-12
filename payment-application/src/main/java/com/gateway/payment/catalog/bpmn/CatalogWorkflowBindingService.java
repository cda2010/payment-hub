package com.gateway.payment.catalog.bpmn;

import com.gateway.payment.common.exception.BusinessException;
import org.flowable.bpmn.model.BpmnModel;
import org.flowable.bpmn.model.Process;
import org.flowable.engine.RepositoryService;
import org.flowable.engine.repository.ProcessDefinition;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.List;

/**
 * Lie un produit catalogue à un process RapidBPMN / Flowable déjà déployé
 * (classpath modules/.../processes ou concepteur /bpmn-designer).
 * Ne génère pas de BPMN : le design est la responsabilité de RapidBPMN.
 */
@Service
public class CatalogWorkflowBindingService {

    private final ObjectProvider<RepositoryService> repositoryServiceProvider;

    public CatalogWorkflowBindingService(ObjectProvider<RepositoryService> repositoryServiceProvider) {
        this.repositoryServiceProvider = repositoryServiceProvider;
    }

    /**
     * Vérifie qu'une définition Flowable existe pour {@code workflowKey}.
     */
    public ProcessDefinition requireDeployedProcess(String workflowKey) {
        if (!StringUtils.hasText(workflowKey)) {
            throw new BusinessException(
                    "CATALOG_WORKFLOW_KEY_REQUIRED",
                    "workflowKey requis : process RapidBPMN (ex. accountTransfer)");
        }
        String key = workflowKey.trim();
        RepositoryService repositoryService = repositoryServiceProvider.getIfAvailable();
        if (repositoryService == null) {
            throw new BusinessException(
                    "BPM_ENGINE_UNAVAILABLE",
                    "Moteur BPM indisponible — impossible de valider " + key);
        }
        ProcessDefinition def = repositoryService.createProcessDefinitionQuery()
                .processDefinitionKey(key)
                .latestVersion()
                .singleResult();
        if (def == null) {
            throw new BusinessException(
                    "CATALOG_WORKFLOW_NOT_FOUND",
                    "Process RapidBPMN introuvable: " + key
                            + " — créez-le dans /bpmn-designer ou modules/.../processes/",
                    HttpStatus.NOT_FOUND);
        }
        return def;
    }

    /**
     * Process principal de la dernière version déployée de {@code workflowKey}.
     */
    public Process requireProcessModel(String workflowKey) {
        ProcessDefinition def = requireDeployedProcess(workflowKey);
        BpmnModel model = repositoryServiceProvider.getObject().getBpmnModel(def.getId());
        Process process = model != null ? model.getProcessById(def.getKey()) : null;
        if (process == null && model != null) {
            process = model.getMainProcess();
        }
        if (process == null) {
            throw new BusinessException(
                    "CATALOG_WORKFLOW_NOT_FOUND",
                    "Modèle BPMN illisible pour " + def.getKey(),
                    HttpStatus.NOT_FOUND);
        }
        return process;
    }

    public List<ProcessDefinition> listLatestProcesses() {
        RepositoryService repositoryService = repositoryServiceProvider.getIfAvailable();
        if (repositoryService == null) {
            return List.of();
        }
        return repositoryService.createProcessDefinitionQuery()
                .latestVersion()
                .orderByProcessDefinitionKey()
                .asc()
                .list();
    }
}
