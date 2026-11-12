package com.gateway.payment.divers.api;

import com.gateway.payment.divers.service.InsightsService;
import com.gateway.payment.divers.service.InsightsService.InsightsView;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/divers/insights")
public class InsightsController {

    private final InsightsService insights;

    public InsightsController(InsightsService insights) {
        this.insights = insights;
    }

    @GetMapping
    public InsightsView month(@RequestParam(required = false) String month) {
        return insights.month(partyId(), month);
    }

    private static UUID partyId() {
        UUID partyId = CurrentUser.partyId();
        if (partyId == null) {
            throw new IllegalArgumentException("partyId requis");
        }
        return partyId;
    }
}
