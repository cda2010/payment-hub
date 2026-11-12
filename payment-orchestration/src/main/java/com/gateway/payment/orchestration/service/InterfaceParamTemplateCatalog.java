package com.gateway.payment.orchestration.service;

import com.gateway.payment.common.domain.ServiceTypeCode;
import com.gateway.payment.orchestration.domain.InterfaceUtilisateur;
import com.gateway.payment.orchestration.domain.ParametreUtilisateur;
import com.gateway.payment.orchestration.repository.ParametreUtilisateurRepository;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.Map;

/**
 * Catalogue Omnicanal-like des paramètres par défaut ({@link ParametreUtilisateur})
 * et inférence du {@link ServiceTypeCode} selon le type méthode.
 */
@Component
public class InterfaceParamTemplateCatalog {

    public record ParamTemplate(String nom, String code, String codeMethode, String attribut, int ordre) {}

    public record ParamTemplatePreview(
            String codeParametre,
            String codeParametreMethode,
            String attribut,
            String typeParametre,
            int ordre
    ) {}

    private static final Map<String, List<ParamTemplate>> DEFAULT_PARAMS = Map.ofEntries(
            Map.entry("TRANSFERT", List.of(
                    t("CODEFSESOURCE", "CODEFSESOURCE", null, null, 1),
                    t("CODEFSEDEST", "CODEFSEDEST", null, null, 2),
                    t("compteDebiteur", "compteDebiteur", "CPTSOURCE", "NUMERO_COMPTE", 3),
                    t("sourceAccount", "sourceAccount", "CPTSOURCE", null, 4),
                    t("accountNumber", "accountNumber", "CPTSOURCE", null, 5),
                    t("compteSource", "compteSource", "CPTSOURCE", null, 6),
                    t("fromAccount", "fromAccount", "CPTSOURCE", null, 7),
                    t("compteCredit", "compteCredit", "CPTDEST", "NUMERO_COMPTE", 8),
                    t("destinationAccount", "destinationAccount", "CPTDEST", null, 9),
                    t("destAccount", "destAccount", "CPTDEST", null, 10),
                    t("compteDest", "compteDest", "CPTDEST", null, 11),
                    t("toAccount", "toAccount", "CPTDEST", null, 12),
                    t("montantDebit", "montantDebit", "MONTANT", null, 13),
                    t("montant", "montant", "MONTANT", null, 14),
                    t("amount", "amount", "MONTANT", null, 15),
                    t("reference", "reference", "REFERENCE", null, 16),
                    t("referenceExterne", "referenceExterne", "REFERENCE", null, 17),
                    t("customerReference", "customerReference", "REFERENCE", null, 18),
                    t("merchantReference", "merchantReference", "REFERENCE", null, 19)
            )),
            Map.entry("DEBIT", List.of(
                    t("CODEFSESOURCE", "CODEFSESOURCE", null, null, 1),
                    t("compteDebiteur", "compteDebiteur", "CPTSOURCE", "NUMERO_COMPTE", 2),
                    t("sourceAccount", "sourceAccount", "CPTSOURCE", null, 3),
                    t("accountNumber", "accountNumber", "CPTSOURCE", null, 4),
                    t("montantDebit", "montantDebit", "MONTANT", null, 5),
                    t("montant", "montant", "MONTANT", null, 6),
                    t("amount", "amount", "MONTANT", null, 7)
            )),
            Map.entry("CREDIT", List.of(
                    t("CODEFSEDEST", "CODEFSEDEST", null, null, 1),
                    t("compteCredit", "compteCredit", "CPTDEST", "NUMERO_COMPTE", 2),
                    t("destinationAccount", "destinationAccount", "CPTDEST", null, 3),
                    t("destAccount", "destAccount", "CPTDEST", null, 4),
                    t("montantDebit", "montantDebit", "MONTANT", null, 5),
                    t("montant", "montant", "MONTANT", null, 6),
                    t("amount", "amount", "MONTANT", null, 7)
            )),
            Map.entry("CONSULTATIONSOLDE", List.of(
                    t("compteDebiteur", "compteDebiteur", "CPTSOURCE", "NUMERO_COMPTE", 1),
                    t("numeroCompte", "numeroCompte", "CPTSOURCE", null, 2),
                    t("sourceAccount", "sourceAccount", "CPTSOURCE", null, 3),
                    t("accountNumber", "accountNumber", "CPTSOURCE", null, 4),
                    t("codeFse", "codeFse", null, null, 5)
            )),
            Map.entry("GENERATE_OTP", List.of(
                    t("telephone", "telephone", "TELEPHONE", null, 1),
                    t("msisdn", "msisdn", "TELEPHONE", null, 2),
                    t("phone", "phone", "TELEPHONE", null, 3),
                    t("phoneNumber", "phoneNumber", "TELEPHONE", null, 4),
                    t("numeroCompte", "numeroCompte", "CPTSOURCE", "NUMERO_COMPTE", 5),
                    t("compteDebiteur", "compteDebiteur", "CPTSOURCE", "NUMERO_COMPTE", 6),
                    t("codeFse", "codeFse", null, null, 7),
                    t("amount", "amount", "MONTANT", null, 8),
                    t("currency", "currency", null, null, 9)
            )),
            Map.entry("CONFIRM_PAYMENT", List.of(
                    t("telephone", "telephone", "TELEPHONE", null, 1),
                    t("msisdn", "msisdn", "TELEPHONE", null, 2),
                    t("phone", "phone", "TELEPHONE", null, 3),
                    t("phoneNumber", "phoneNumber", "TELEPHONE", null, 4),
                    t("otp", "otp", null, null, 5),
                    t("amount", "amount", "MONTANT", null, 6),
                    t("transactionAmount", "transactionAmount", "MONTANT", null, 7),
                    t("currency", "currency", null, null, 8),
                    t("currencyCode", "currencyCode", null, null, 9),
                    t("codeFse", "codeFse", null, null, 10),
                    t("paymentId", "paymentId", null, null, 11)
            )),
            Map.entry("LOOKUP_BILL", List.of(
                    t("billerCode", "billerCode", null, null, 1),
                    t("billReference", "billReference", "BILL_REF", null, 2),
                    t("productCode", "productCode", null, null, 3),
                    t("subscriberReference", "subscriberReference", "BILL_REF", null, 4),
                    t("meterNumber", "meterNumber", "BILL_REF", null, 5),
                    t("meterReference", "meterReference", "BILL_REF", null, 6),
                    t("customerReference", "customerReference", "BILL_REF", null, 7),
                    t("reference", "reference", "BILL_REF", null, 8)
            )),
            Map.entry("BILL_LIST", List.of(
                    t("billerCode", "billerCode", null, null, 1),
                    t("productCode", "productCode", null, null, 2),
                    t("subscriberReference", "subscriberReference", "BILL_REF", null, 3),
                    t("meterNumber", "meterNumber", "BILL_REF", null, 4),
                    t("billStatus", "billStatus", null, null, 5)
            )),
            Map.entry("PAY_BILL", List.of(
                    t("billerCode", "billerCode", null, null, 1),
                    t("billReference", "billReference", "BILL_REF", null, 2),
                    t("subscriberReference", "subscriberReference", "BILL_REF", null, 3),
                    t("meterNumber", "meterNumber", "BILL_REF", null, 4),
                    t("productCode", "productCode", null, null, 5),
                    t("amount", "amount", "MONTANT", null, 6),
                    t("otp", "otp", null, null, 7),
                    t("currency", "currency", null, null, 8),
                    t("paymentId", "paymentId", null, null, 9),
                    t("idempotencyKey", "idempotencyKey", null, null, 10),
                    t("transactionReference", "transactionReference", null, null, 11)
            )),
            Map.entry("GET_STATUS", List.of(
                    t("providerReference", "providerReference", null, null, 1),
                    t("billerCode", "billerCode", null, null, 2),
                    t("productCode", "productCode", null, null, 3)
            )),
            Map.entry("DATASOURCE", List.of(
                    t("dataKey", "dataKey", "DATA_KEY", null, 1),
                    t("dataSource", "dataSource", "DATA_KEY", null, 2),
                    t("codeFse", "codeFse", null, null, 3),
                    t("q", "q", null, null, 4),
                    t("query", "query", null, null, 5),
                    t("category", "category", null, null, 6),
                    t("page", "page", null, null, 7),
                    t("size", "size", null, null, 8)
            ))
    );

    public ServiceTypeCode serviceTypeFor(String typeMethode) {
        return switch (normalize(typeMethode)) {
            case "CONSULTATIONSOLDE", "CONSULTATIONHIST" -> ServiceTypeCode.CONSULTATION_SOLDE;
            case "TRANSFERT", "DEBIT", "CREDIT" -> ServiceTypeCode.TRANSFERT;
            case "LOOKUP_BILL", "BILL_LIST", "PAY_BILL",
                 "CONSULTATIONFACTURE", "SETTLEMENT", "GETMONTANT", "VERIFREGLEFACTURIER", "GETREGLE" ->
                    ServiceTypeCode.PAIEMENT_FACTURE;
            case "GENERATE_OTP", "CONFIRM_PAYMENT", "INFOCLIENT", "GETINFOS", "GET_STATUS", "DATASOURCE" ->
                    ServiceTypeCode.PAYEMENT;
            default -> ServiceTypeCode.TRANSFERT;
        };
    }

    public List<ParamTemplatePreview> previewParametres(String typeMethode) {
        return templatesFor(typeMethode).stream()
                .map(t -> new ParamTemplatePreview(
                        t.code(),
                        t.codeMethode(),
                        t.attribut(),
                        "STRING",
                        t.ordre()
                ))
                .toList();
    }

    public List<ParamTemplate> templatesFor(String typeMethode) {
        return DEFAULT_PARAMS.getOrDefault(normalize(typeMethode), List.of());
    }

    public int seedDefaultParametres(InterfaceUtilisateur iface, ParametreUtilisateurRepository repository) {
        int added = 0;
        for (ParamTemplate tpl : templatesFor(iface.getTypeMethode())) {
            var existing = repository.findByInterfaceUtilisateurIdAndCodeParametre(iface.getId(), tpl.code());
            if (existing.isPresent()) {
                ParametreUtilisateur param = existing.get();
                boolean dirty = false;
                if (StringUtils.hasText(tpl.codeMethode())
                        && !StringUtils.hasText(param.getCodeParametreMethode())) {
                    param.setCodeParametreMethode(tpl.codeMethode());
                    dirty = true;
                }
                if (StringUtils.hasText(tpl.attribut())
                        && !StringUtils.hasText(param.getAttribut())) {
                    param.setAttribut(tpl.attribut());
                    dirty = true;
                }
                if (dirty) {
                    repository.save(param);
                }
                continue;
            }
            repository.save(ParametreUtilisateur.builder()
                    .interfaceUtilisateur(iface)
                    .nomParametre(tpl.nom())
                    .codeParametre(tpl.code())
                    .codeParametreMethode(tpl.codeMethode())
                    .typeParametre("STRING")
                    .attribut(tpl.attribut())
                    .valeur(defaultValue(iface, tpl))
                    .ordre(tpl.ordre())
                    .build());
            added++;
        }
        return added;
    }

    public static String normalize(String typeMethode) {
        if (!StringUtils.hasText(typeMethode)) {
            return "";
        }
        return typeMethode.trim().toUpperCase();
    }

    private static String defaultValue(InterfaceUtilisateur iface, ParamTemplate tpl) {
        if ("codeFse".equals(tpl.code()) && iface.getCodeFse() != null) {
            return iface.getCodeFse();
        }
        return null;
    }

    private static ParamTemplate t(String nom, String code, String codeMethode, String attribut, int ordre) {
        return new ParamTemplate(nom, code, codeMethode, attribut, ordre);
    }
}
