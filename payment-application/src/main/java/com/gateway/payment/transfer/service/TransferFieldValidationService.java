package com.gateway.payment.transfer.service;

import com.gateway.payment.common.exception.BusinessException;
import com.gateway.payment.transfer.api.dto.TransferFieldDto;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Service
public class TransferFieldValidationService {

    public void validate(Map<String, Object> values, List<TransferFieldDto> fields, String partyLabel) {
        if (fields == null || fields.isEmpty()) {
            return;
        }
        for (TransferFieldDto field : fields) {
            Object raw = values != null ? values.get(field.name()) : null;
            String val = raw == null ? "" : String.valueOf(raw).trim();
            if (field.required() && val.isBlank()) {
                throw new BusinessException(
                        "FIELD_REQUIRED",
                        partyLabel + " : " + field.label() + " requis",
                        HttpStatus.BAD_REQUEST);
            }
            if (!val.isBlank() && field.pattern() != null && !field.pattern().isBlank()
                    && !val.matches(field.pattern())) {
                throw new BusinessException(
                        "FIELD_INVALID",
                        partyLabel + " : " + field.label() + " invalide",
                        HttpStatus.BAD_REQUEST);
            }
            if (!val.isBlank() && field.options() != null && !field.options().isEmpty()
                    && field.options().stream().noneMatch(opt -> opt.equals(val))) {
                throw new BusinessException(
                        "FIELD_INVALID",
                        partyLabel + " : valeur non autorisée pour " + field.label(),
                        HttpStatus.BAD_REQUEST);
            }
        }
    }
}
