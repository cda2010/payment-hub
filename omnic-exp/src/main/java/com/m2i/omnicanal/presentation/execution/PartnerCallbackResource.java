package com.m2i.omnicanal.presentation.execution;

import com.m2i.omnicanal.outils.restclient.model.ResultMessage;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/**
 * Le partenaire asynchrone confirme ici que l'ordre est passé.
 */
@RestController
@RequestMapping("/api/partenaires")
public class PartnerCallbackResource {

    private final PartnerExecutionService execution;

    public PartnerCallbackResource(PartnerExecutionService execution) {
        this.execution = execution;
    }

    @PostMapping("/callback/{cle}")
    public ResultMessage callback(@PathVariable("cle") String cle, @RequestBody(required = false) Map<String, Object> body) {
        return execution.callback(cle, body);
    }

    @GetMapping("/ordres/{cle}")
    public ResultMessage statut(@PathVariable("cle") String cle) {
        return execution.statut(cle);
    }
}
