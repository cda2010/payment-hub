package com.gateway.payment.bpm;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.gateway.payment.approval.model.ApprovalDecideResult;
import com.gateway.payment.approval.service.ApprovalEngine;
import com.gateway.payment.common.domain.PaymentStatus;
import com.gateway.payment.common.exception.BusinessException;
import com.gateway.payment.configuration.domain.PaymentFlow;
import com.gateway.payment.configuration.domain.ProviderOtpConfig;
import com.gateway.payment.configuration.repository.PaymentProviderRepository;
import com.gateway.payment.configuration.repository.ProviderOtpConfigRepository;
import com.gateway.payment.payment.domain.PaymentEvent;
import com.gateway.payment.payment.domain.PaymentTransaction;
import com.gateway.payment.payment.repository.PaymentEventRepository;
import com.gateway.payment.payment.repository.PaymentTransactionRepository;
import org.flowable.engine.runtime.ProcessInstance;
import org.flowable.task.api.Task;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

/**
 * Orchestration checkout 100 % Flowable :
 * démarrage / submit form·OTP·approval / sync statut.
 * <p>
 * Détails métier délégués à :
 * {@link FlowableStartVariableFactory}, {@link FlowableRoleSelectionService},
 * {@link FlowableTaskStatusSynchronizer}.
 */
@Service
public class FlowablePaymentRuntime {

    private static final Logger log = LoggerFactory.getLogger(FlowablePaymentRuntime.class);

    private final PaymentProcessSupport processSupport;
    private final PaymentTransactionRepository transactionRepository;
    private final PaymentEventRepository eventRepository;
    private final PaymentProviderRepository providerRepository;
    private final ProviderOtpConfigRepository otpConfigRepository;
    private final BpmCheckoutNextActionService nextActionService;
    private final ApprovalEngine approvalEngine;
    private final FlowableStartVariableFactory startVariableFactory;
    private final FlowableRoleSelectionService roleSelectionService;
    private final FlowableTaskStatusSynchronizer taskStatusSynchronizer;
    private final ObjectMapper objectMapper;

    public FlowablePaymentRuntime(
            PaymentProcessSupport processSupport,
            PaymentTransactionRepository transactionRepository,
            PaymentEventRepository eventRepository,
            PaymentProviderRepository providerRepository,
            ProviderOtpConfigRepository otpConfigRepository,
            BpmCheckoutNextActionService nextActionService,
            ApprovalEngine approvalEngine,
            FlowableStartVariableFactory startVariableFactory,
            FlowableRoleSelectionService roleSelectionService,
            FlowableTaskStatusSynchronizer taskStatusSynchronizer,
            ObjectMapper objectMapper
    ) {
        this.processSupport = processSupport;
        this.transactionRepository = transactionRepository;
        this.eventRepository = eventRepository;
        this.providerRepository = providerRepository;
        this.otpConfigRepository = otpConfigRepository;
        this.nextActionService = nextActionService;
        this.approvalEngine = approvalEngine;
        this.startVariableFactory = startVariableFactory;
        this.roleSelectionService = roleSelectionService;
        this.taskStatusSynchronizer = taskStatusSynchronizer;
        this.objectMapper = objectMapper;
    }

    public static boolean isFlowableEligible(PaymentFlow flow) {
        return flow != null
                && flow.getBpmProcessKey() != null
                && !flow.getBpmProcessKey().isBlank();
    }

    @Transactional
    public void start(PaymentTransaction tx, PaymentFlow flow, Map<String, Object> context) {
        String key = FlowableStartVariableFactory.resolveProcessKey(flow, context);
        Map<String, Object> vars = startVariableFactory.build(tx, flow, key, context);

        ProcessInstance pi = processSupport.start(key, tx.getPaymentId(), vars);
        Map<String, Object> persisted = context != null ? new HashMap<>(context) : new HashMap<>();
        persisted.put(PaymentProcessSupport.VAR_PROCESS_INSTANCE_ID, pi.getId());
        persisted.put(PaymentProcessSupport.VAR_BPM_PROCESS_KEY, key);
        persisted.put(PaymentProcessSupport.VAR_FLOW_CODE, flow.getCode());
        persisted.put(PaymentProcessSupport.VAR_FLOW_NOM, flow.getNom());
        persisted.put("flowId", flow.getId().toString());
        tx.setContextJson(writeJson(persisted));
        taskStatusSynchronizer.syncAfterTransition(tx);
        addEvent(tx.getPaymentId(), "BPM_STARTED",
                "Processus Flowable " + key + " (« " + flow.getNom() + " ») démarré");
        log.info("Checkout Flowable paymentId={} flowCode={} flowNom={} key={}",
                tx.getPaymentId(), flow.getCode(), flow.getNom(), key);
    }

    @Transactional
    public void submitForm(PaymentTransaction tx, PaymentFlow flow, Map<String, Object> data) {
        Map<String, Object> vars = sanitizeVars(data);
        if (data != null && data.containsKey("phoneNumber")) {
            tx.setPhoneNumber(String.valueOf(data.get("phoneNumber")));
            vars.put("phoneNumber", tx.getPhoneNumber());
            vars.put("phone", tx.getPhoneNumber());
        }
        if (data != null && data.containsKey("amount")) {
            tx.setAmount(new BigDecimal(String.valueOf(data.get("amount"))));
            vars.put("amount", tx.getAmount());
        }
        Task active = processSupport.findActiveTask(tx.getPaymentId(), null);
        roleSelectionService.applyFormSelections(tx, active, data, vars);
        mergeContext(tx, vars);

        Map<String, String> userParams = FlowableStartVariableFactory.buildUserParams(data);
        if (!userParams.isEmpty()) {
            vars.put(GatewayCallFseBpmnErrors.VAR_USER_PARAMS, userParams);
        }
        providerRepository.findByCode(tx.getProviderCode()).ifPresent(p -> {
            if (StringUtils.hasText(p.getCodeFse())) {
                vars.put(GatewayCallFseBpmnErrors.VAR_SOURCE_CODE_FSE, p.getCodeFse());
                vars.put(GatewayCallFseBpmnErrors.VAR_DEST_CODE_FSE, p.getCodeFse());
                userParams.putIfAbsent("codeFse", p.getCodeFse());
            }
        });

        addEvent(tx.getPaymentId(), "FORM_SUBMITTED", "Formulaire soumis (Flowable)");
        assertFormTask(active);
        processSupport.completeActiveTask(
                tx.getPaymentId(),
                active != null ? active.getTaskDefinitionKey() : null,
                vars);
        taskStatusSynchronizer.syncAfterTransition(tx);
    }

    @Transactional
    public void submitApproval(PaymentTransaction tx, boolean approved, String comment, String approverId) {
        Task active = processSupport.findActiveTask(tx.getPaymentId(), null);
        if (active == null || !nextActionService.isApprovalTask(active.getFormKey(), active.getTaskDefinitionKey())) {
            throw new BusinessException("INVALID_ACTION", "Aucune tâche d'approbation active");
        }
        Map<String, Object> ctx = readContext(tx);
        ApprovalDecideResult result = approvalEngine.decide(tx, ctx, approved, comment, approverId);
        ctx.put("lastApprovalMessage", result.message());
        tx.setContextJson(writeJson(ctx));
        transactionRepository.save(tx);

        addEvent(tx.getPaymentId(),
                result.rejected() ? "APPROVAL_DENIED"
                        : (result.completeFlowableTask() ? "APPROVAL_GRANTED" : "APPROVAL_PARTIAL"),
                result.message() + " (" + active.getTaskDefinitionKey() + ")");

        if (!result.completeFlowableTask()) {
            tx.applyStatus(PaymentStatus.WAITING_APPROVAL);
            transactionRepository.save(tx);
            return;
        }

        Map<String, Object> vars = new HashMap<>();
        vars.put(com.gateway.payment.bpm.dmn.DmnTransferApprovalConfig.VAR_APPROVED, result.approved());
        if (comment != null && !comment.isBlank()) {
            vars.put(com.gateway.payment.bpm.dmn.DmnTransferApprovalConfig.VAR_APPROVAL_COMMENT, comment.trim());
        }
        if (approverId != null && !approverId.isBlank()) {
            vars.put(com.gateway.payment.bpm.dmn.DmnTransferApprovalConfig.VAR_APPROVER_ID, approverId.trim());
        }
        mergeContext(tx, vars);
        processSupport.completeActiveTask(tx.getPaymentId(), active.getTaskDefinitionKey(), vars);
        taskStatusSynchronizer.syncAfterTransition(tx);
    }

    @Transactional
    public void submitOtp(PaymentTransaction tx, PaymentFlow flow, Map<String, Object> data) {
        Object otp = data != null ? data.get("otp") : null;
        if (otp == null || String.valueOf(otp).isBlank()) {
            throw new BusinessException("OTP_INVALID", "OTP requis");
        }
        String otpValue = String.valueOf(otp).trim();
        Map<String, Object> ctx = readContext(tx);
        String otpProviderCode = firstNonBlank(stringVal(ctx.get("payProviderCode")), tx.getProviderCode());
        ProviderOtpConfig otpConfig = providerRepository.findByCode(otpProviderCode)
                .flatMap(p -> otpConfigRepository.findByProviderId(p.getId()))
                .orElse(null);
        if (otpConfig != null && otpConfig.getLength() != null && otpConfig.getLength() > 0) {
            if (!otpValue.matches("\\d{" + otpConfig.getLength() + "}")) {
                throw new BusinessException(
                        "OTP_INVALID",
                        "Le code OTP doit contenir " + otpConfig.getLength() + " chiffres");
            }
        }
        Map<String, Object> vars = new HashMap<>();
        vars.put("otp", otpValue);
        addEvent(tx.getPaymentId(), "OTP_SUBMITTED", "OTP utilisateur soumis (Flowable)");
        Task active = processSupport.findActiveTask(tx.getPaymentId(), null);
        boolean otpTask = active != null && nextActionService.isOtpTask(active.getFormKey(), active.getTaskDefinitionKey());
        boolean confirmHold = active != null && "CONFIRM".equals(active.getTaskDefinitionKey());
        if (!otpTask && !confirmHold) {
            throw new BusinessException("INVALID_ACTION", "Aucune tâche OTP active");
        }
        processSupport.completeActiveTask(tx.getPaymentId(), active.getTaskDefinitionKey(), vars);
        taskStatusSynchronizer.syncAfterTransition(tx);
    }

    @Transactional
    public void cancel(PaymentTransaction tx) {
        processSupport.deleteIfAlive(tx.getPaymentId(), "cancelled");
    }

    private void assertFormTask(Task active) {
        if (active != null && nextActionService.isOtpTask(active.getFormKey(), active.getTaskDefinitionKey())) {
            throw new BusinessException("INVALID_ACTION", "Étape OTP : utilisez SUBMIT_OTP");
        }
        if (active != null && nextActionService.isApprovalTask(active.getFormKey(), active.getTaskDefinitionKey())) {
            throw new BusinessException("INVALID_ACTION", "Étape approbation : utilisez APPROVE ou REJECT");
        }
    }

    private void mergeContext(PaymentTransaction tx, Map<String, Object> vars) {
        Map<String, Object> ctx = readContext(tx);
        vars.forEach((k, v) -> {
            if (!"otp".equalsIgnoreCase(k)) {
                ctx.put(k, v);
            }
        });
        tx.setContextJson(writeJson(ctx));
        transactionRepository.save(tx);
    }

    private Map<String, Object> sanitizeVars(Map<String, Object> data) {
        Map<String, Object> vars = new HashMap<>();
        if (data == null) {
            return vars;
        }
        data.forEach((k, v) -> {
            if (!"otp".equalsIgnoreCase(k)) {
                vars.put(k, v);
            }
        });
        return vars;
    }

    private Map<String, Object> readContext(PaymentTransaction tx) {
        if (tx.getContextJson() == null || tx.getContextJson().isBlank()) {
            return new HashMap<>();
        }
        try {
            return objectMapper.readValue(tx.getContextJson(), new TypeReference<>() {});
        } catch (Exception e) {
            return new HashMap<>();
        }
    }

    private String writeJson(Object value) {
        try {
            return objectMapper.writeValueAsString(value);
        } catch (Exception e) {
            return "{}";
        }
    }

    private void addEvent(String paymentId, String type, String message) {
        eventRepository.save(PaymentEvent.builder()
                .paymentId(paymentId)
                .eventType(type)
                .message(message)
                .createdAt(Instant.now())
                .build());
    }

    private static String stringVal(Object v) {
        return v != null ? String.valueOf(v) : null;
    }

    private static String firstNonBlank(String... values) {
        if (values == null) {
            return null;
        }
        for (String v : values) {
            if (StringUtils.hasText(v)) {
                return v.trim();
            }
        }
        return null;
    }
}
