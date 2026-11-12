package com.gateway.payment.payment.api.dto;

import com.gateway.payment.common.domain.NextActionType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class NextActionDto {
    private NextActionType type;
    /**
     * Clé formulaire catalogue / BPMN (ex. {@code account-transfer-source}).
     * Utile pour un BFF / UI externe qui mappe des écrans natifs sans parser tout le schéma.
     */
    private String formKey;
    /** Legacy mini-schema ; toujours rempli (dérivé de formio si besoin). */
    private List<FormFieldDto> fields;
    /**
     * Schéma Form.io natif depuis configurationJson.formio.
     * Absent si le step n’a que {"fields":[...]} (le front construit alors le schéma).
     */
    private Map<String, Object> formSchema;
    private String url;
    private Integer expiresIn;
    /** Longueur attendue du code OTP (ex. 6 pour Coris/Orange). */
    private Integer otpLength;
    private String message;
    private Integer pollAfterSeconds;
    /**
     * Données écran complémentaires (ex. {@code bills} après BILL_LIST) — fusionnées
     * dans {@code IntegrationScreen.data} pour front / mobile.
     */
    private Map<String, Object> data;
}
