package com.m2i.omnicanal.presentation.service;

import java.util.Set;

/**
 * Motifs de schéma, alignés sur la décision de payment-2.
 * Le code FSE précis est porté par le motif ({@code DEST_FSE_0006}, {@code SOURCE_FSE_0007}).
 */
public final class SchemaMotif {

    private static final String DEST = "DEST_FSE_";
    private static final String SOURCE = "SOURCE_FSE_";

    private static final Set<String> MOTIFS = Set.of(
            "SAME_FSE_ANY",
            "SAME_FSE_SIEGE_1",
            "SAME_FSE_SIEGE_0",
            "SAME_FSE_SIEGE_TRUTHY",
            "DIFF_FSE_ANY",
            "DIFF_FSE_SIEGE_1",
            "DIFF_FSE_SIEGE_0",
            "INTERNATIONAL",
            "BANQUE_BANQUE",
            "NON_BANQUE_SAME_PAYS"
    );

    private SchemaMotif() {
    }

    public static boolean connu(String patternCode) {
        String code = normalise(patternCode);
        if (code == null) {
            return false;
        }
        if (code.startsWith(DEST) && code.length() > DEST.length()) {
            return true;
        }
        if (code.startsWith(SOURCE) && code.length() > SOURCE.length()) {
            return true;
        }
        return MOTIFS.contains(code);
    }

    public static boolean correspond(
            String patternCode,
            boolean sameFse,
            boolean samePays,
            int siege,
            boolean bothBanque,
            String codeFseSource,
            String codeFseDest
    ) {
        String code = normalise(patternCode);
        if (code == null) {
            return false;
        }
        if (code.startsWith(DEST)) {
            return code.substring(DEST.length()).equals(codeFseDest);
        }
        if (code.startsWith(SOURCE)) {
            return code.substring(SOURCE.length()).equals(codeFseSource);
        }
        return switch (code) {
            case "SAME_FSE_ANY" -> sameFse;
            case "SAME_FSE_SIEGE_1" -> sameFse && siege == 1;
            case "SAME_FSE_SIEGE_0" -> sameFse && siege == 0;
            case "SAME_FSE_SIEGE_TRUTHY" -> sameFse && siege != 0;
            case "DIFF_FSE_ANY" -> !sameFse;
            case "DIFF_FSE_SIEGE_1" -> !sameFse && siege == 1;
            case "DIFF_FSE_SIEGE_0" -> !sameFse && siege == 0;
            case "INTERNATIONAL" -> !sameFse && !samePays;
            case "BANQUE_BANQUE" -> !sameFse && bothBanque;
            case "NON_BANQUE_SAME_PAYS" -> !sameFse && samePays && !bothBanque;
            default -> false;
        };
    }

    private static String normalise(String patternCode) {
        if (patternCode == null || patternCode.isBlank()) {
            return null;
        }
        return patternCode.trim().toUpperCase();
    }
}
