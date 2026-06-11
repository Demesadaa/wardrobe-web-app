package com.wardrobe.config;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

@Validated
@ConfigurationProperties(prefix = "app.settings")
public class AppSettingsProperties {
    @NotBlank
    private String title;

    @Min(1)
    @Max(100)
    private int defaultPageLimit;

    @NotBlank
    @Email
    private String supportEmail;

    @NotBlank
    private String externalStyleServiceUrl;

    private boolean aiSuggestionsEnabled;

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public int getDefaultPageLimit() {
        return defaultPageLimit;
    }

    public void setDefaultPageLimit(int defaultPageLimit) {
        this.defaultPageLimit = defaultPageLimit;
    }

    public String getSupportEmail() {
        return supportEmail;
    }

    public void setSupportEmail(String supportEmail) {
        this.supportEmail = supportEmail;
    }

    public String getExternalStyleServiceUrl() {
        return externalStyleServiceUrl;
    }

    public void setExternalStyleServiceUrl(String externalStyleServiceUrl) {
        this.externalStyleServiceUrl = externalStyleServiceUrl;
    }

    public boolean isAiSuggestionsEnabled() {
        return aiSuggestionsEnabled;
    }

    public void setAiSuggestionsEnabled(boolean aiSuggestionsEnabled) {
        this.aiSuggestionsEnabled = aiSuggestionsEnabled;
    }
}
