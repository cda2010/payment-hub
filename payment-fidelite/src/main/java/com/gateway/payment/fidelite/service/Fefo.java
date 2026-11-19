package com.gateway.payment.fidelite.service;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;

/** Consomme d'abord le lot qui expire le plus tôt. Une échéance nulle passe en dernier. */
public final class Fefo {

    public record Lot(UUID id, long remaining, Instant expiresAt, Instant earnedAt) {
    }

    public record Prise(UUID id, long points) {
    }

    private Fefo() {
    }

    public static List<Prise> prelever(List<Lot> lots, long demande) {
        if (lots == null || lots.isEmpty() || demande <= 0) {
            return List.of();
        }
        List<Lot> ordered = lots.stream()
                .filter(lot -> lot != null && lot.remaining() > 0)
                .sorted(Comparator
                        .comparing((Lot lot) -> lot.expiresAt() == null)
                        .thenComparing(Lot::expiresAt, Comparator.nullsLast(Comparator.naturalOrder()))
                        .thenComparing(Lot::earnedAt, Comparator.nullsLast(Comparator.naturalOrder())))
                .toList();
        List<Prise> prises = new ArrayList<>();
        long reste = demande;
        for (Lot lot : ordered) {
            if (reste <= 0) {
                break;
            }
            long pris = Math.min(lot.remaining(), reste);
            prises.add(new Prise(lot.id(), pris));
            reste -= pris;
        }
        return prises;
    }

    /** Échéance la plus proche parmi les lots touchés. Nulle si aucun n'expire. */
    public static Instant echeance(List<Lot> lots, List<Prise> prises) {
        Instant plusTot = null;
        for (Prise prise : prises) {
            for (Lot lot : lots) {
                if (lot.id().equals(prise.id()) && lot.expiresAt() != null
                        && (plusTot == null || lot.expiresAt().isBefore(plusTot))) {
                    plusTot = lot.expiresAt();
                }
            }
        }
        return plusTot;
    }
}
