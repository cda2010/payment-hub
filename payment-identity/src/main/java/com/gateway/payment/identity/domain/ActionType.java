package com.gateway.payment.identity.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Catalogue d'une action « À faire » : libellé, icône et route.
 * {@code route} peut contenir {@code {aggregateId}}, {@code {paymentId}} ou {@code {partyId}}.
 */
@Entity
@Table(name = "action_type")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ActionType {

    @Id
    @Column(length = 64)
    private String code;

    @Column(nullable = false, length = 255)
    private String title;

    @Column(length = 512)
    private String subtitle;

    @Column(length = 64)
    private String icon;

    @Column(nullable = false, length = 255)
    private String route;

    @Column(name = "route_fallback", length = 255)
    private String routeFallback;

    @Column(nullable = false)
    @Builder.Default
    private int priority = 100;
}
