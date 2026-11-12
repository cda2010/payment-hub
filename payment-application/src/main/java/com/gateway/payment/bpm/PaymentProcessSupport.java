package com.gateway.payment.bpm;

import com.gateway.payment.common.exception.BusinessException;
import org.flowable.engine.HistoryService;
import org.flowable.engine.RuntimeService;
import org.flowable.engine.TaskService;
import org.flowable.engine.history.HistoricProcessInstance;
import org.flowable.engine.runtime.ProcessInstance;
import org.flowable.task.api.Task;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
public class PaymentProcessSupport {

    private static final Logger log = LoggerFactory.getLogger(PaymentProcessSupport.class);

    public static final String VAR_PAYMENT_ID = "paymentId";
    public static final String VAR_FLOW_ID = "flowId";
    public static final String VAR_FLOW_CODE = "flowCode";
    public static final String VAR_FLOW_NOM = "flowNom";
    public static final String VAR_BPM_PROCESS_KEY = "bpmProcessKey";
    public static final String VAR_PROVIDER_CODE = "providerCode";
    public static final String VAR_PROCESS_INSTANCE_ID = "processInstanceId";

    private final RuntimeService runtimeService;
    private final HistoryService historyService;
    private final TaskService taskService;

    public PaymentProcessSupport(
            RuntimeService runtimeService,
            HistoryService historyService,
            TaskService taskService
    ) {
        this.runtimeService = runtimeService;
        this.historyService = historyService;
        this.taskService = taskService;
    }

    public ProcessInstance start(String processKey, String paymentId, Map<String, Object> variables) {
        if (!StringUtils.hasText(processKey)) {
            throw new BusinessException("BPM_PROCESS_KEY_MISSING", "Clé processus Flowable manquante sur le flow");
        }
        Map<String, Object> vars = new HashMap<>();
        if (variables != null) {
            vars.putAll(variables);
        }
        vars.put(VAR_PAYMENT_ID, paymentId);
        vars.put(VAR_BPM_PROCESS_KEY, processKey);
        ProcessInstance pi = runtimeService.startProcessInstanceByKey(processKey.trim(), paymentId, vars);
        log.info("Flowable start key={} paymentId={} pi={} flowNom={}",
                processKey, paymentId, pi.getId(), vars.get(VAR_FLOW_NOM));
        return pi;
    }

    public Task findActiveTask(String paymentId, String preferredTaskKey) {
        // Inclut les userTasks du callActivity (sous-processus) via businessKey et/ou variable paymentId
        List<Task> tasks = taskService.createTaskQuery()
                .processInstanceBusinessKey(paymentId)
                .list();
        if (tasks.isEmpty()) {
            tasks = taskService.createTaskQuery()
                    .processVariableValueEquals(VAR_PAYMENT_ID, paymentId)
                    .list();
        }
        if (tasks.isEmpty()) {
            return null;
        }
        if (StringUtils.hasText(preferredTaskKey)) {
            for (Task t : tasks) {
                if (preferredTaskKey.equals(t.getTaskDefinitionKey())) {
                    return t;
                }
            }
        }
        return tasks.get(0);
    }

    public void completeActiveTask(String paymentId, String preferredTaskKey, Map<String, Object> variables) {
        Task task = findActiveTask(paymentId, preferredTaskKey);
        if (task == null) {
            throw new BusinessException("NO_ACTIVE_TASK", "Aucune tâche utilisateur active pour ce paiement");
        }
        taskService.complete(task.getId(), variables != null ? variables : Map.of());
        log.info("Flowable complete task={} key={} paymentId={}",
                task.getId(), task.getTaskDefinitionKey(), paymentId);
    }

    public boolean isAlive(String paymentId) {
        return findInstance(paymentId) != null;
    }

    /**
     * Instance racine du paiement (ignore les sous-processus callActivity partageant le businessKey).
     */
    public ProcessInstance findInstance(String paymentId) {
        List<ProcessInstance> byKey = runtimeService.createProcessInstanceQuery()
                .processInstanceBusinessKey(paymentId)
                .list();
        ProcessInstance root = pickRoot(byKey);
        if (root != null) {
            return root;
        }
        List<ProcessInstance> byVar = runtimeService.createProcessInstanceQuery()
                .variableValueEquals(VAR_PAYMENT_ID, paymentId)
                .list();
        return pickRoot(byVar);
    }

    /**
     * Id de l'instance racine (runtime ou historique) pour le viewer BPMN.
     */
    public String findRootProcessInstanceId(String paymentId) {
        if (!StringUtils.hasText(paymentId)) {
            return null;
        }
        ProcessInstance live = findInstance(paymentId.trim());
        if (live != null) {
            return live.getId();
        }
        List<HistoricProcessInstance> byKey = historyService.createHistoricProcessInstanceQuery()
                .processInstanceBusinessKey(paymentId.trim())
                .orderByProcessInstanceStartTime()
                .asc()
                .list();
        HistoricProcessInstance historicRoot = pickHistoricRoot(byKey);
        if (historicRoot != null) {
            return historicRoot.getId();
        }
        List<HistoricProcessInstance> byVar = historyService.createHistoricProcessInstanceQuery()
                .variableValueEquals(VAR_PAYMENT_ID, paymentId.trim())
                .orderByProcessInstanceStartTime()
                .asc()
                .list();
        HistoricProcessInstance byVarRoot = pickHistoricRoot(byVar);
        return byVarRoot != null ? byVarRoot.getId() : null;
    }

    private static ProcessInstance pickRoot(List<ProcessInstance> instances) {
        if (instances == null || instances.isEmpty()) {
            return null;
        }
        for (ProcessInstance pi : instances) {
            if (pi.getSuperExecutionId() == null) {
                return pi;
            }
        }
        return instances.get(0);
    }

    private static HistoricProcessInstance pickHistoricRoot(List<HistoricProcessInstance> instances) {
        if (instances == null || instances.isEmpty()) {
            return null;
        }
        for (HistoricProcessInstance pi : instances) {
            if (pi.getSuperProcessInstanceId() == null) {
                return pi;
            }
        }
        return instances.get(0);
    }

    public void deleteIfAlive(String paymentId, String reason) {
        ProcessInstance pi = findInstance(paymentId);
        if (pi != null) {
            runtimeService.deleteProcessInstance(pi.getId(), reason != null ? reason : "cancelled");
        }
    }

    public Map<String, Object> variables(String paymentId) {
        ProcessInstance pi = findInstance(paymentId);
        if (pi == null) {
            return Map.of();
        }
        return runtimeService.getVariables(pi.getId());
    }
}
