package com.gateway.payment.payment.application;

import com.gateway.payment.payment.domain.PaymentEvent;
import com.gateway.payment.payment.repository.PaymentEventRepository;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Service
public class PaymentEventAuditWriter {

    private final PaymentEventRepository eventRepository;

    public PaymentEventAuditWriter(PaymentEventRepository eventRepository) {
        this.eventRepository = eventRepository;
    }

    @Async("paymentAuditExecutor")
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void persistAsync(PaymentEvent event) {
        eventRepository.save(event);
    }
}
