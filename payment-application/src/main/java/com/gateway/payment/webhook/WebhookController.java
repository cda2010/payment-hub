package com.gateway.payment.webhook;

import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/webhooks")
@Tag(name = "Webhooks")
public class WebhookController {

    private final WebhookService webhookService;

    public WebhookController(WebhookService webhookService) {
        this.webhookService = webhookService;
    }

    @PostMapping(value = "/{provider}", consumes = MediaType.APPLICATION_JSON_VALUE)
    public Map<String, Object> receive(
            @PathVariable String provider,
            @RequestBody String body,
            HttpServletRequest request
    ) {
        return webhookService.handle(provider, body, request);
    }
}
