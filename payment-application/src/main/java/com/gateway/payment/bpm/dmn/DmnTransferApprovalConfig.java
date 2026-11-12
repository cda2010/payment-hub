package com.gateway.payment.bpm.dmn;

/**
 * DMN montant/contexte → {@code approvalProfileCode}.
 */
public final class DmnTransferApprovalConfig {

    public static final String DMN_RESOURCE = "rules/transfer-approval.dmn";
    public static final String DMN_MODULE_RESOURCE =
            "modules/payment-admin-module/decisions/transfer-approval.dmn";
    public static final String DMN_NAMESPACE = "https://payment-gateway/dmn/transfer-approval";
    public static final String DMN_MODEL_NAME = "transfer-approval";
    public static final String DECISION_NAME = "TransferApproval";
    public static final String DECISION_KEY = "decision_TransferApproval";

    public static final String OUT_PROFILE_CODE = "approvalProfileCode";
    public static final String VAR_PROFILE_CODE = "approvalProfileCode";
    public static final String VAR_LEVELS_REQUIRED = "approvalLevelsRequired";
    public static final String VAR_GROUP_L1 = "approvalCandidateGroupL1";
    public static final String VAR_GROUP_L2 = "approvalCandidateGroupL2";
    public static final String VAR_GROUP_L3 = "approvalCandidateGroupL3";
    public static final String VAR_APPROVED = "approved";
    public static final String VAR_APPROVAL_COMMENT = "approvalComment";
    public static final String VAR_APPROVER_ID = "approverId";
    public static final String VAR_APPROVAL_INSTANCE_ID = "approvalInstanceId";
    public static final String CTX_INITIATED_BY = "initiatedBy";

    public static final String PROFILE_NO_APPROVAL = "NO_APPROVAL";

    private DmnTransferApprovalConfig() {
    }
}
