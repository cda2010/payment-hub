package com.gateway.payment.payment.api.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FormFieldDto {
    private String name;
    private String label;
    private String type;
    private Boolean required;
    private String placeholder;
    private String pattern;
    /** Options SELECT (label/value) — enrichies côté UI si absentes. */
    private java.util.List<java.util.Map<String, String>> options;
}
