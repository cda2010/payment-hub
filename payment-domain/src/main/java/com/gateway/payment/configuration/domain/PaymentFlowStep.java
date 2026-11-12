package com.gateway.payment.configuration.domain;

import com.gateway.payment.common.domain.StepType;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.UuidGenerator;

import java.util.UUID;

@Entity
@Table(name = "payment_flow_step")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PaymentFlowStep {

    @Id
    @UuidGenerator
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "flow_id", nullable = false)
    private PaymentFlow flow;

    @Column(nullable = false, length = 50)
    private String code;

    @Enumerated(EnumType.STRING)
    @Column(name = "step_type", nullable = false, length = 50)
    private StepType stepType;

    @Column(nullable = false)
    private Integer ordre;

    @Column(name = "api_operation_code", length = 50)
    private String apiOperationCode;

    @Column(name = "next_step_success", length = 50)
    private String nextStepSuccess;

    @Column(name = "next_step_failure", length = 50)
    private String nextStepFailure;

    @Column(name = "configuration_json", columnDefinition = "TEXT")
    private String configurationJson;

    @Column(nullable = false)
    private boolean actif;
}
