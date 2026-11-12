package com.gateway.payment.schedule.service;

import com.gateway.payment.schedule.domain.ScheduledPayment;
import com.gateway.payment.schedule.domain.ScheduledPaymentFrequency;
import com.gateway.payment.schedule.domain.ScheduledPaymentStatus;
import com.gateway.payment.schedule.repository.ScheduledPaymentRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Locale;
import java.util.NoSuchElementException;
import java.util.UUID;

@Service
public class ScheduledPaymentService {

    private final ScheduledPaymentRepository repository;

    public ScheduledPaymentService(ScheduledPaymentRepository repository) {
        this.repository = repository;
    }

    @Transactional(readOnly = true)
    public List<ScheduledPayment> listActive() {
        return repository.findByActifTrueOrderByNextRunAtAscCreatedAtDesc();
    }

    @Transactional(readOnly = true)
    public ScheduledPayment get(UUID id) {
        return repository.findByIdAndActifTrue(id)
                .orElseThrow(() -> new NoSuchElementException("scheduled payment not found: " + id));
    }

    @Transactional
    public ScheduledPayment create(ScheduledPayment draft) {
        validate(draft);
        if (!StringUtils.hasText(draft.getOwnerKey())) {
            draft.setOwnerKey("demo");
        }
        if (!StringUtils.hasText(draft.getCurrency())) {
            draft.setCurrency("XOF");
        }
        draft.setStatus(ScheduledPaymentStatus.ACTIVE);
        draft.setActif(true);
        return repository.save(draft);
    }

    @Transactional
    public ScheduledPayment pause(UUID id) {
        ScheduledPayment sp = get(id);
        if (sp.getStatus() == ScheduledPaymentStatus.CANCELLED) {
            throw new IllegalArgumentException("cannot pause a cancelled schedule");
        }
        sp.setStatus(ScheduledPaymentStatus.PAUSED);
        return repository.save(sp);
    }

    @Transactional
    public ScheduledPayment resume(UUID id) {
        ScheduledPayment sp = get(id);
        if (sp.getStatus() == ScheduledPaymentStatus.CANCELLED) {
            throw new IllegalArgumentException("cannot resume a cancelled schedule");
        }
        if (sp.getNextRunAt() == null || sp.getNextRunAt().isBefore(LocalDate.now())) {
            sp.setNextRunAt(LocalDate.now().plusDays(1));
        }
        sp.setStatus(ScheduledPaymentStatus.ACTIVE);
        return repository.save(sp);
    }

    @Transactional
    public void delete(UUID id) {
        ScheduledPayment sp = get(id);
        sp.setStatus(ScheduledPaymentStatus.CANCELLED);
        sp.setActif(false);
        repository.save(sp);
    }

    private void validate(ScheduledPayment draft) {
        if (!StringUtils.hasText(draft.getLabel())) {
            throw new IllegalArgumentException("label is required");
        }
        if (!StringUtils.hasText(draft.getProductCode())) {
            throw new IllegalArgumentException("productCode is required");
        }
        if (draft.getAmount() == null || draft.getAmount().compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("amount must be positive");
        }
        if (draft.getFrequency() == null) {
            throw new IllegalArgumentException("frequency is required");
        }
        if (draft.getNextRunAt() == null) {
            draft.setNextRunAt(LocalDate.now().plusDays(1));
        }
        draft.setLabel(draft.getLabel().trim());
        draft.setProductCode(draft.getProductCode().trim().toUpperCase(Locale.ROOT));
    }

    public static ScheduledPaymentFrequency parseFrequency(String raw) {
        if (!StringUtils.hasText(raw)) {
            return ScheduledPaymentFrequency.MONTHLY;
        }
        try {
            return ScheduledPaymentFrequency.valueOf(raw.trim().toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException ex) {
            return ScheduledPaymentFrequency.MONTHLY;
        }
    }
}
