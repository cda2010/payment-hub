package com.gateway.payment.common.domain;

import java.util.Collections;
import java.util.EnumMap;
import java.util.EnumSet;
import java.util.Map;
import java.util.Set;

/**
 * Transitions autorisées pour {@link PaymentStatus}.
 * Les états terminaux n'acceptent plus de mutation (sauf identité).
 */
public final class PaymentStatusTransitionGuard {

    public static final Set<PaymentStatus> TERMINAL = EnumSet.of(
            PaymentStatus.SUCCESS,
            PaymentStatus.FAILED,
            PaymentStatus.CANCELLED,
            PaymentStatus.EXPIRED
    );

    private static final Map<PaymentStatus, Set<PaymentStatus>> ALLOWED = build();

    private PaymentStatusTransitionGuard() {
    }

    public static boolean isTerminal(PaymentStatus status) {
        return status != null && TERMINAL.contains(status);
    }

    public static boolean canTransition(PaymentStatus from, PaymentStatus to) {
        if (to == null) {
            return false;
        }
        if (from == null || from == to) {
            return true;
        }
        if (isTerminal(from)) {
            return false;
        }
        Set<PaymentStatus> next = ALLOWED.get(from);
        return next != null && next.contains(to);
    }

    /**
     * @return {@code to} si autorisé, sinon lève {@link IllegalStateException}
     */
    public static PaymentStatus require(PaymentStatus from, PaymentStatus to) {
        if (canTransition(from, to)) {
            return to;
        }
        throw new IllegalStateException(
                "Transition interdite: " + from + " → " + to);
    }

    /**
     * Applique si autorisé ; no-op si même statut ; ignore silencieusement
     * une tentative depuis un terminal (idempotence webhook).
     *
     * @return true si le statut a changé
     */
    public static boolean tryApply(PaymentStatus from, PaymentStatus to, StatusMutator mutator) {
        if (to == null || from == to) {
            return false;
        }
        if (isTerminal(from)) {
            return false;
        }
        if (!canTransition(from, to)) {
            throw new IllegalStateException("Transition interdite: " + from + " → " + to);
        }
        mutator.set(to);
        return true;
    }

    @FunctionalInterface
    public interface StatusMutator {
        void set(PaymentStatus status);
    }

    private static Map<PaymentStatus, Set<PaymentStatus>> build() {
        Map<PaymentStatus, Set<PaymentStatus>> map = new EnumMap<>(PaymentStatus.class);

        map.put(PaymentStatus.CREATED, EnumSet.of(
                PaymentStatus.INITIATED, PaymentStatus.FAILED, PaymentStatus.CANCELLED));

        map.put(PaymentStatus.INITIATED, EnumSet.of(
                PaymentStatus.AWAITING_USER_INPUT,
                PaymentStatus.WAITING_APPROVAL,
                PaymentStatus.OTP_GENERATION_PENDING,
                PaymentStatus.OTP_REQUIRED,
                PaymentStatus.PROCESSING,
                PaymentStatus.REDIRECT_REQUIRED,
                PaymentStatus.WAITING_CALLBACK,
                PaymentStatus.PENDING,
                PaymentStatus.UNKNOWN,
                PaymentStatus.SUCCESS,
                PaymentStatus.FAILED,
                PaymentStatus.CANCELLED,
                PaymentStatus.EXPIRED));

        map.put(PaymentStatus.AWAITING_USER_INPUT, EnumSet.of(
                PaymentStatus.WAITING_APPROVAL,
                PaymentStatus.OTP_GENERATION_PENDING,
                PaymentStatus.OTP_REQUIRED,
                PaymentStatus.PROCESSING,
                PaymentStatus.REDIRECT_REQUIRED,
                PaymentStatus.WAITING_CALLBACK,
                PaymentStatus.PENDING,
                PaymentStatus.UNKNOWN,
                PaymentStatus.SUCCESS,
                PaymentStatus.FAILED,
                PaymentStatus.CANCELLED,
                PaymentStatus.EXPIRED,
                PaymentStatus.RETURNED_FOR_CORRECTION));

        map.put(PaymentStatus.WAITING_APPROVAL, EnumSet.of(
                PaymentStatus.AWAITING_USER_INPUT,
                PaymentStatus.RETURNED_FOR_CORRECTION,
                PaymentStatus.PROCESSING,
                PaymentStatus.OTP_REQUIRED,
                PaymentStatus.PENDING,
                PaymentStatus.SUCCESS,
                PaymentStatus.FAILED,
                PaymentStatus.CANCELLED));

        map.put(PaymentStatus.RETURNED_FOR_CORRECTION, EnumSet.of(
                PaymentStatus.AWAITING_USER_INPUT,
                PaymentStatus.WAITING_APPROVAL,
                PaymentStatus.PROCESSING,
                PaymentStatus.FAILED,
                PaymentStatus.CANCELLED));

        map.put(PaymentStatus.OTP_GENERATION_PENDING, EnumSet.of(
                PaymentStatus.OTP_REQUIRED,
                PaymentStatus.PROCESSING,
                PaymentStatus.FAILED,
                PaymentStatus.CANCELLED,
                PaymentStatus.UNKNOWN));

        map.put(PaymentStatus.OTP_REQUIRED, EnumSet.of(
                PaymentStatus.OTP_GENERATION_PENDING,
                PaymentStatus.AWAITING_USER_INPUT,
                PaymentStatus.WAITING_APPROVAL,
                PaymentStatus.PROCESSING,
                PaymentStatus.REDIRECT_REQUIRED,
                PaymentStatus.WAITING_CALLBACK,
                PaymentStatus.PENDING,
                PaymentStatus.SUCCESS,
                PaymentStatus.FAILED,
                PaymentStatus.CANCELLED,
                PaymentStatus.UNKNOWN,
                PaymentStatus.EXPIRED));

        map.put(PaymentStatus.PROCESSING, EnumSet.of(
                PaymentStatus.AWAITING_USER_INPUT,
                PaymentStatus.OTP_GENERATION_PENDING,
                PaymentStatus.OTP_REQUIRED,
                PaymentStatus.REDIRECT_REQUIRED,
                PaymentStatus.WAITING_CALLBACK,
                PaymentStatus.PENDING,
                PaymentStatus.WAITING_APPROVAL,
                PaymentStatus.RETURNED_FOR_CORRECTION,
                PaymentStatus.SUCCESS,
                PaymentStatus.FAILED,
                PaymentStatus.CANCELLED,
                PaymentStatus.UNKNOWN,
                PaymentStatus.EXPIRED));

        map.put(PaymentStatus.REDIRECT_REQUIRED, EnumSet.of(
                PaymentStatus.WAITING_CALLBACK,
                PaymentStatus.PENDING,
                PaymentStatus.PROCESSING,
                PaymentStatus.AWAITING_USER_INPUT,
                PaymentStatus.OTP_REQUIRED,
                PaymentStatus.SUCCESS,
                PaymentStatus.FAILED,
                PaymentStatus.CANCELLED,
                PaymentStatus.EXPIRED,
                PaymentStatus.UNKNOWN));

        map.put(PaymentStatus.WAITING_CALLBACK, EnumSet.of(
                PaymentStatus.PENDING,
                PaymentStatus.PROCESSING,
                PaymentStatus.AWAITING_USER_INPUT,
                PaymentStatus.OTP_REQUIRED,
                PaymentStatus.WAITING_APPROVAL,
                PaymentStatus.REDIRECT_REQUIRED,
                PaymentStatus.SUCCESS,
                PaymentStatus.FAILED,
                PaymentStatus.CANCELLED,
                PaymentStatus.EXPIRED,
                PaymentStatus.UNKNOWN));

        map.put(PaymentStatus.PENDING, EnumSet.of(
                PaymentStatus.PROCESSING,
                PaymentStatus.WAITING_CALLBACK,
                PaymentStatus.OTP_REQUIRED,
                PaymentStatus.OTP_GENERATION_PENDING,
                PaymentStatus.AWAITING_USER_INPUT,
                PaymentStatus.WAITING_APPROVAL,
                PaymentStatus.RETURNED_FOR_CORRECTION,
                PaymentStatus.REDIRECT_REQUIRED,
                PaymentStatus.SUCCESS,
                PaymentStatus.FAILED,
                PaymentStatus.CANCELLED,
                PaymentStatus.EXPIRED,
                PaymentStatus.UNKNOWN));

        map.put(PaymentStatus.UNKNOWN, EnumSet.of(
                PaymentStatus.PROCESSING,
                PaymentStatus.PENDING,
                PaymentStatus.WAITING_CALLBACK,
                PaymentStatus.OTP_REQUIRED,
                PaymentStatus.SUCCESS,
                PaymentStatus.FAILED,
                PaymentStatus.CANCELLED,
                PaymentStatus.EXPIRED));

        // terminaux : aucun successeur
        for (PaymentStatus t : TERMINAL) {
            map.put(t, EnumSet.noneOf(PaymentStatus.class));
        }

        return Collections.unmodifiableMap(map);
    }
}
