package com.gateway.payment.omnicanal.canal;

import com.gateway.payment.common.domain.ServiceTypeCode;
import com.gateway.payment.omnicanal.idempotency.CanalIdempotencyService;
import com.gateway.payment.omnicanal.response.CanalTransferResponse;
import com.gateway.payment.omnicanal.util.CanalParamParser;
import com.gateway.payment.omnicanal.validation.ChannelValidationException;
import com.gateway.payment.orchestration.domain.ProprieteApi;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * API canal Omnicanal-compatible (façade additive).
 * <pre>
 * POST /api/v1/boundaryByTypeMethode
 * POST /api/v1/omnicanalPresentation
 * POST /api/v1/canal/compile-leg
 * </pre>
 */
@RestController
@Tag(name = "Canal Omnicanal")
public class CanalPresentationController {

    private final CanalPresentationService canalService;

    public CanalPresentationController(CanalPresentationService canalService) {
        this.canalService = canalService;
    }

    @PostMapping({
            "/api/v1/boundaryByTypeMethode",
            "/api/boundaryByTypeMethode",
            "/api/v1/canal/boundaryByTypeMethode"
    })
    @Operation(summary = "Appel FSE form-urlencoded (codeFSE, codeMethode, otrParametre)")
    public ResponseEntity<CanalTransferResponse> boundaryByTypeMethode(
            @RequestParam("codeFSE") String codeFse,
            @RequestParam("codeMethode") String codeMethode,
            @RequestParam("otrParametre") String otrParametre
    ) {
        return ResponseEntity.ok(canalService.boundaryByTypeMethode(codeFse, codeMethode, otrParametre));
    }

    @PostMapping({
            "/api/v1/omnicanalPresentation",
            "/api/omnicanalPresentation",
            "/api/v1/canal/omnicanalPresentation"
    })
    @Operation(summary = "Présentation / orchestration schéma (transfert, consultation…)")
    public ResponseEntity<CanalTransferResponse> omnicanalPresentation(
            @RequestBody CanalPresentationRequest body,
            @RequestHeader(value = CanalIdempotencyService.HEADER_IDEMPOTENCY, required = false) String idempotencyKey,
            HttpServletRequest request
    ) {
        if (body == null) {
            throw new ChannelValidationException("400", "Corps JSON obligatoire");
        }
        Map<String, String> userParams = new HashMap<>();
        if (body.param() != null) {
            for (CanalParam p : body.param()) {
                if (p != null && p.codeParametre() != null) {
                    userParams.put(p.codeParametre(), p.valeur() != null ? p.valeur() : "");
                }
            }
        }
        if (body.otrParametre() != null) {
            userParams.putAll(CanalParamParser.parseOtrParametre(body.otrParametre()));
        }

        String source = firstNonBlank(body.sourceCodeFse(), body.fse(), body.codeFse());
        String dest = firstNonBlank(body.destCodeFse(), body.fse(), body.codeFse(), source);
        ServiceTypeCode serviceType = resolveServiceType(body);

        String key = firstNonBlank(idempotencyKey, body.referenceExterne(), body.idempotencyKey());
        String correlationId = firstNonBlank(
                request.getHeader("X-Correlation-Id"),
                body.correlationId(),
                UUID.randomUUID().toString()
        );

        return ResponseEntity.ok(canalService.omnicanalPresentation(
                serviceType, source, dest, body.siege(), userParams, key, correlationId
        ));
    }

    @PostMapping("/api/v1/canal/compile-leg")
    @Operation(summary = "Preview LegCompiler (canal)")
    public ResponseEntity<?> compileLeg(@RequestBody CanalCompileRequest body) {
        return ResponseEntity.ok(canalService.compilePreview(
                body.sourceCodeFse(),
                body.destCodeFse() != null ? body.destCodeFse() : body.sourceCodeFse(),
                body.typeMethode(),
                body.apiAUtiliser() != null ? body.apiAUtiliser() : ProprieteApi.FSESOURCE,
                body.userParams()
        ));
    }

    @ExceptionHandler(ChannelValidationException.class)
    public ResponseEntity<CanalTransferResponse> onValidation(ChannelValidationException ex) {
        return ResponseEntity.badRequest().body(CanalTransferResponse.fail(ex.getCode(), ex.getMessage()));
    }

    private static ServiceTypeCode resolveServiceType(CanalPresentationRequest body) {
        if (body.serviceType() != null) {
            return body.serviceType();
        }
        if (body.codeService() != null) {
            ServiceTypeCode mapped = ServiceTypeCode.fromString(body.codeService());
            if (mapped != null) {
                return mapped;
            }
        }
        // Heuristique Omnicanal : présence d'un typeMethode CONSULTATIONSOLDE dans params
        if (body.typeMethode() != null) {
            ServiceTypeCode fromMethode = ServiceTypeCode.fromString(body.typeMethode());
            if (fromMethode != null) {
                return fromMethode;
            }
        }
        return ServiceTypeCode.TRANSFERT;
    }

    private static String firstNonBlank(String... values) {
        if (values == null) {
            return null;
        }
        for (String v : values) {
            if (v != null && !v.isBlank()) {
                return v.trim();
            }
        }
        return null;
    }

    public record CanalParam(String codeParametre, String valeur) {}

    public record CanalPresentationRequest(
            ServiceTypeCode serviceType,
            String codeService,
            String typeMethode,
            String fse,
            String codeFse,
            String sourceCodeFse,
            String destCodeFse,
            Integer siege,
            String otrParametre,
            String referenceExterne,
            String idempotencyKey,
            String correlationId,
            String codeUniteOrganisation,
            List<CanalParam> param
    ) {}

    public record CanalCompileRequest(
            String sourceCodeFse,
            String destCodeFse,
            String typeMethode,
            ProprieteApi apiAUtiliser,
            Map<String, String> userParams
    ) {}
}
