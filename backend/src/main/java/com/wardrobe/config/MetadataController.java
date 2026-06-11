package com.wardrobe.config;

import java.time.Instant;
import java.util.Locale;
import org.springframework.context.MessageSource;
import org.springframework.core.env.Environment;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/meta")
public class MetadataController {
    private final AppSettingsProperties settings;
    private final Environment environment;
    private final MessageSource messageSource;

    public MetadataController(
            AppSettingsProperties settings,
            Environment environment,
            MessageSource messageSource) {
        this.settings = settings;
        this.environment = environment;
        this.messageSource = messageSource;
    }

    @GetMapping
    public MetadataResponse metadata(Locale locale) {
        String message = messageSource.getMessage("api.meta.welcome", new Object[] {settings.getTitle()}, locale);
        return new MetadataResponse(
                settings.getTitle(),
                message,
                settings.getDefaultPageLimit(),
                settings.getSupportEmail(),
                settings.getExternalStyleServiceUrl(),
                settings.isAiSuggestionsEnabled(),
                environment.getActiveProfiles(),
                Instant.now());
    }

    public record MetadataResponse(
            String title,
            String message,
            int defaultPageLimit,
            String supportEmail,
            String externalStyleServiceUrl,
            boolean aiSuggestionsEnabled,
            String[] activeProfiles,
            Instant generatedAt) {
    }
}
