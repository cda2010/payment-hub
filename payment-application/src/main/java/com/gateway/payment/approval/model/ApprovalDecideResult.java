package com.gateway.payment.approval.model;

/**
 * Résultat d'une décision d'approbation pour le runtime BPMN.
 */
public record ApprovalDecideResult(
        boolean completeFlowableTask,
        boolean approved,
        boolean instanceFinished,
        boolean rejected,
        int currentLevel,
        int totalLevels,
        String message
) {
    public static ApprovalDecideResult partial(int level, int total, String message) {
        return new ApprovalDecideResult(false, true, false, false, level, total, message);
    }

    public static ApprovalDecideResult levelComplete(int level, int total, boolean instanceDone, String message) {
        return new ApprovalDecideResult(true, true, instanceDone, false, level, total, message);
    }

    public static ApprovalDecideResult rejected(int level, int total, String message) {
        return new ApprovalDecideResult(true, false, true, true, level, total, message);
    }
}
