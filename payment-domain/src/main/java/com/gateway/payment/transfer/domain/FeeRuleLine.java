package com.gateway.payment.transfer.domain;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.UuidGenerator;

import java.math.BigDecimal;
import java.util.UUID;

@Entity
@Table(name = "fee_rule_line")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FeeRuleLine {

    @Id
    @UuidGenerator
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "fee_rule_id", nullable = false)
    private FeeRule feeRule;

    @Enumerated(EnumType.STRING)
    @Column(name = "fee_type", nullable = false, length = 20)
    private FeeType feeType;

    @Column(name = "min_amount", precision = 18, scale = 2)
    private BigDecimal minAmount;

    @Column(name = "max_amount", precision = 18, scale = 2)
    private BigDecimal maxAmount;

    @Column(nullable = false, precision = 18, scale = 4)
    private BigDecimal value;

    @Column(name = "fixed_amount", precision = 18, scale = 2)
    private BigDecimal fixedAmount;

    @Column(name = "percent_value", precision = 18, scale = 4)
    private BigDecimal percentValue;

    @Column(name = "min_fee", precision = 18, scale = 2)
    private BigDecimal minFee;

    @Column(name = "max_fee", precision = 18, scale = 2)
    private BigDecimal maxFee;

    @Column(nullable = false)
    private Integer ordre;
}

