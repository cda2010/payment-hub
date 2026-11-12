package com.m2i.omnicanal.presentation.execution;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.m2i.omnicanal.model.Api;
import com.m2i.omnicanal.outils.restclient.model.ResultMessage;
import com.m2i.omnicanal.presentation.http.PartnerHttpClient;
import com.m2i.omnicanal.presentation.repository.OrdrePartenaireRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.support.TransactionTemplate;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

/**
 * Applique le mode d'exécution du fournisseur et l'unicité de l'ordre.
 */
@Service
public class PartnerExecutionService {

    private static final Logger log = LoggerFactory.getLogger(PartnerExecutionService.class);

    private final OrdrePartenaireRepository ordres;
    private final JdbcTemplate jdbc;
    private final ObjectMapper json;
    private final TransactionTemplate writes;
    private final String publicBaseUrl;

    public PartnerExecutionService(OrdrePartenaireRepository ordres, JdbcTemplate jdbc, ObjectMapper json,
            PlatformTransactionManager transactions,
            @Value("${omnicanal.execution.public-base-url:http://localhost:5981}") String publicBaseUrl) {
        this.ordres = ordres;
        this.jdbc = jdbc;
        this.json = json;
        this.writes = new TransactionTemplate(transactions);
        this.writes.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRES_NEW);
        this.publicBaseUrl = publicBaseUrl == null ? "http://localhost:5981" : publicBaseUrl.replaceAll("/+$", "");
    }

    public record Outcome(ResultMessage result, boolean partnerCalled) {
    }

    public Outcome execute(Api api, String codeMethode, String httpMethod,
            Map<String, String> raw, Map<String, Object> headers, Supplier<ResultMessage> partnerCall) {
        if (ExecutionGate.readOnly(httpMethod, codeMethode)) {
            return new Outcome(partnerCall.get(), true);
        }
        FseMode fse = fseOf(api);
        String mode = ExecutionGate.normalizeMode(fse.mode());
        boolean hasOtp = ExecutionGate.hasOtp(raw);
        boolean dialogue = ExecutionGate.otpDialogue(codeMethode);
        String explicit = ExecutionGate.explicitKey(raw);
        String resolved = explicit != null ? explicit : ExecutionGate.fingerprint(fse.codeFse(), codeMethode, raw);
        String key = resolved.length() > 200 ? resolved.substring(0, 200) : resolved;
        OrdrePartenaire existing = ordres.findByCleIdempotence(key).orElse(null);
        ExecutionGate.Decision decision = ExecutionGate.decide(mode, dialogue, hasOtp, existing == null ? null : existing.getStatut());
        if (decision.action() == ExecutionGate.Action.REPLAY) {
            return new Outcome(replay(existing), false);
        }
        if (decision.action() == ExecutionGate.Action.HOLD_OTP) {
            if (existing == null) {
                try {
                    writes.execute(status -> remember(key, fse, api, codeMethode, mode, "EN_ATTENTE_OTP", ExecutionGate.OTP_REQUIS,
                            "Code OTP requis avant l'envoi au partenaire", null, null));
                } catch (DataIntegrityViolationException ex) {
                    log.debug("Ordre OTP déjà retenu {}", key);
                }
            }
            return new Outcome(hold(key, mode), false);
        }
        OrdrePartenaire row = existing != null ? existing : insertInProgress(key, fse, api, codeMethode, mode);
        if (row == null) {
            OrdrePartenaire raced = ordres.findByCleIdempotence(key).orElse(null);
            if (raced != null && !"EN_COURS".equals(raced.getStatut()) && !"EN_ATTENTE_OTP".equals(raced.getStatut())) {
                return new Outcome(replay(raced), false);
            }
            return new Outcome(pending(key, "Ordre déjà en cours de traitement", null), false);
        }
        String callback = null;
        if ("ASYNC".equals(mode)) {
            callback = callbackUrl(key);
            row.setCallbackUrl(callback);
            headers.put("X-Callback-Url", callback);
        }
        headers.put("Idempotency-Key", key);
        ResultMessage result;
        try {
            result = partnerCall.get();
        } catch (RuntimeException ex) {
            store(row.getId(), "ECHEC", -400, ex.getMessage(), null, callback);
            throw ex;
        }
        if ("ASYNC".equals(mode) && result != null && !ExecutionGate.infrastructure(result.getErrCode())) {
            store(row.getId(), "EN_ATTENTE_CALLBACK", ExecutionGate.ACCEPTE, "Ordre accepté, en attente de confirmation du partenaire", result.getResponseEntity(), callback);
            return new Outcome(pending(key, "Ordre accepté, en attente de confirmation du partenaire", callback), true);
        }
        int err = result == null ? -400 : result.getErrCode();
        String statut = err == 0 ? "TERMINE" : "ECHEC";
        store(row.getId(), statut, err, result == null ? "Erreur inconnue" : result.getMsg(), result == null ? null : result.getResponseEntity(), callback);
        return new Outcome(result, true);
    }

    public ResultMessage callback(String cle, Map<String, Object> body) {
        OrdrePartenaire row = ordres.findByCleIdempotence(cle).orElse(null);
        if (row == null) {
            return PartnerHttpClient.result(-1, "Ordre inconnu", "1", null, "JSON");
        }
        if (!"EN_ATTENTE_CALLBACK".equals(row.getStatut()) && !"EN_COURS".equals(row.getStatut())) {
            return replay(row);
        }
        int err = number(body == null ? null : body.get("errCode"));
        String message = body != null && body.get("msg") != null ? String.valueOf(body.get("msg")) : (err == 0 ? "Confirmé par le partenaire" : "Refus du partenaire");
        Object payload = body == null ? null : body.getOrDefault("payload", body);
        store(row.getId(), err == 0 ? "TERMINE" : "ECHEC", err, message, payload, row.getCallbackUrl());
        log.info("Callback partenaire cle={} err={}", cle, err);
        return replay(ordres.findByCleIdempotence(cle).orElse(row));
    }

    public ResultMessage statut(String cle) {
        OrdrePartenaire row = ordres.findByCleIdempotence(cle).orElse(null);
        if (row == null) {
            return PartnerHttpClient.result(-1, "Ordre inconnu", "1", null, "JSON");
        }
        return replay(row);
    }

    private OrdrePartenaire insertInProgress(String key, FseMode fse, Api api, String codeMethode, String mode) {
        try {
            return writes.execute(status -> remember(key, fse, api, codeMethode, mode, "EN_COURS", null, null, null, null));
        } catch (DataIntegrityViolationException ex) {
            return null;
        }
    }

    private OrdrePartenaire remember(String key, FseMode fse, Api api, String codeMethode, String mode, String statut,
            Integer err, String message, Object body, String callback) {
        Instant now = Instant.now();
        OrdrePartenaire row = new OrdrePartenaire();
        row.setCleIdempotence(key);
        row.setCodeFse(fse.codeFse());
        row.setCodeApi(api == null ? null : api.getCodeApi());
        row.setCodeMethode(codeMethode);
        row.setModeExecution(mode);
        row.setStatut(statut);
        row.setErrCode(err);
        row.setMessage(cut(message));
        row.setReponse(write(body));
        row.setCallbackUrl(callback);
        row.setCreeLe(now);
        row.setMisAJourLe(now);
        return ordres.saveAndFlush(row);
    }

    private void store(Long id, String statut, int err, String message, Object body, String callback) {
        writes.executeWithoutResult(status -> {
            OrdrePartenaire row = ordres.findById(id).orElseThrow();
            row.setStatut(statut);
            row.setErrCode(err);
            row.setMessage(cut(message));
            row.setReponse(write(body));
            if (callback != null) {
                row.setCallbackUrl(callback);
            }
            row.setMisAJourLe(Instant.now());
        });
    }

    private ResultMessage replay(OrdrePartenaire row) {
        if (row == null) {
            return PartnerHttpClient.result(-1, "Ordre inconnu", "1", null, "JSON");
        }
        if ("EN_ATTENTE_OTP".equals(row.getStatut())) {
            return hold(row.getCleIdempotence(), row.getModeExecution());
        }
        if ("EN_ATTENTE_CALLBACK".equals(row.getStatut()) || "EN_COURS".equals(row.getStatut())) {
            String message = "EN_COURS".equals(row.getStatut())
                    ? "Ordre déjà en cours de traitement"
                    : "Ordre accepté, en attente de confirmation du partenaire";
            return pending(row.getCleIdempotence(), message, row.getCallbackUrl());
        }
        return PartnerHttpClient.result(row.getErrCode() == null ? -1 : row.getErrCode(), row.getMessage(), "1", read(row.getReponse()), "JSON");
    }

    private ResultMessage hold(String key, String mode) {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("statut", "EN_ATTENTE_OTP");
        body.put("modeExecution", mode);
        body.put("cle", key);
        return PartnerHttpClient.result(ExecutionGate.OTP_REQUIS, "Code OTP requis avant l'envoi au partenaire", "1", body, "JSON");
    }

    private ResultMessage pending(String key, String message, String callback) {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("statut", "EN_ATTENTE_CALLBACK");
        body.put("cle", key);
        if (callback != null) {
            body.put("callbackUrl", callback);
        }
        return PartnerHttpClient.result(ExecutionGate.ACCEPTE, message, "1", body, "JSON");
    }

    private String callbackUrl(String key) {
        return publicBaseUrl + "/api/partenaires/callback/" + URLEncoder.encode(key, StandardCharsets.UTF_8);
    }

    private FseMode fseOf(Api api) {
        if (api == null || api.getCodeApi() == null) {
            return new FseMode(null, "DIRECT");
        }
        List<Map<String, Object>> rows = jdbc.queryForList(
                "select f.code_fse, f.mode_execution from fournisseur_service f join api a on a.fournisseur_service_id = f.id where a.code_api = ?",
                api.getCodeApi());
        if (rows.isEmpty()) {
            return new FseMode(null, "DIRECT");
        }
        Map<String, Object> row = rows.get(0);
        String code = row.get("code_fse") == null ? null : String.valueOf(row.get("code_fse"));
        String mode = row.get("mode_execution") == null ? null : String.valueOf(row.get("mode_execution"));
        return new FseMode(code, mode);
    }

    private String write(Object body) {
        if (body == null) {
            return null;
        }
        try {
            return json.writeValueAsString(body);
        } catch (Exception ex) {
            return null;
        }
    }

    private Object read(String body) {
        if (body == null || body.isBlank()) {
            return null;
        }
        try {
            return json.readValue(body, Object.class);
        } catch (Exception ex) {
            return body;
        }
    }

    private static int number(Object value) {
        if (value instanceof Number number) {
            return number.intValue();
        }
        if (value == null) {
            return -1;
        }
        try {
            return Integer.parseInt(String.valueOf(value).trim());
        } catch (NumberFormatException ex) {
            return -1;
        }
    }

    private static String cut(String message) {
        if (message == null) {
            return null;
        }
        return message.length() <= 500 ? message : message.substring(0, 500);
    }

    private record FseMode(String codeFse, String mode) {
    }
}
