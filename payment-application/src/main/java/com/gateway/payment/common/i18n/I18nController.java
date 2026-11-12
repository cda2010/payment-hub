package com.gateway.payment.common.i18n;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/i18n")
@Tag(name = "I18n")
public class I18nController {

    private final GatewayI18nService i18nService;

    public I18nController(GatewayI18nService i18nService) {
        this.i18nService = i18nService;
    }

    @GetMapping("/languages")
    @Operation(summary = "Langues supportées (déclarées dans gateway.i18n)")
    public LanguagesResponse languages() {
        return new LanguagesResponse(
                i18nService.properties().getDefaultLanguage(),
                i18nService.supportedLanguages().stream()
                        .map(l -> new LanguageDto(l.getCode(), l.getLabel()))
                        .toList()
        );
    }

    public record LanguagesResponse(String defaultLanguage, List<LanguageDto> languages) {}

    public record LanguageDto(String code, String label) {}
}
