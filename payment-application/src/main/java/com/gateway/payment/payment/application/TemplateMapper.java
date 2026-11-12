package com.gateway.payment.payment.application;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Component
public class TemplateMapper {

    private static final Pattern PLACEHOLDER = Pattern.compile("\\$\\{([^}]+)}|\\{\\{([^}]+)}}");

    private final ObjectMapper objectMapper;

    public TemplateMapper(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    public String apply(String template, Map<String, Object> context) {
        if (template == null || template.isBlank()) {
            return template;
        }
        Matcher matcher = PLACEHOLDER.matcher(template);
        StringBuffer sb = new StringBuffer();
        while (matcher.find()) {
            String key = matcher.group(1) != null ? matcher.group(1) : matcher.group(2);
            Object value = resolve(context, key);
            matcher.appendReplacement(sb, Matcher.quoteReplacement(value == null ? "" : String.valueOf(value)));
        }
        matcher.appendTail(sb);
        return sb.toString();
    }

    public Map<String, Object> applyJsonTemplate(String templateJson, Map<String, Object> context) {
        if (templateJson == null || templateJson.isBlank()) {
            return new HashMap<>(context);
        }
        try {
            String rendered = apply(templateJson, context);
            return objectMapper.readValue(rendered, new TypeReference<>() {});
        } catch (Exception e) {
            return new HashMap<>(context);
        }
    }

    private Object resolve(Map<String, Object> context, String key) {
        if (context.containsKey(key)) {
            return context.get(key);
        }
        // aliases
        return switch (key) {
            case "phone", "msisdn" -> context.getOrDefault("phoneNumber", context.get("phone"));
            case "transactionAmount" -> context.getOrDefault("amount", context.get("transactionAmount"));
            case "currencyCode" -> context.getOrDefault("currency", context.get("currencyCode"));
            default -> null;
        };
    }
}
