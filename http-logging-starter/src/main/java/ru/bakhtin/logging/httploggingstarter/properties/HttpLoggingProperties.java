package ru.bakhtin.logging.httploggingstarter.properties;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.ArrayList;
import java.util.List;

@ConfigurationProperties(prefix = "http-logging")
public class HttpLoggingProperties {
    private boolean enabled = true;
    private String level = "INFO";
    private List<String> includeRequestParameters = new ArrayList<>();
    private List<String> excludeRequestParameters = new ArrayList<>();
    private List<String> includeResponseParameters = new ArrayList<>();
    private List<String> excludeResponseParameters = new ArrayList<>();

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public String getLevel() {
        return level;
    }

    public void setLevel(String level) {
        this.level = level;
    }

    public List<String> getIncludeRequestParameters() {
        return includeRequestParameters;
    }

    public void setIncludeRequestParameters(List<String> includeRequestParameters) {
        this.includeRequestParameters = includeRequestParameters;
    }

    public List<String> getExcludeRequestParameters() {
        return excludeRequestParameters;
    }

    public void setExcludeRequestParameters(List<String> excludeRequestParameters) {
        this.excludeRequestParameters = excludeRequestParameters;
    }

    public List<String> getIncludeResponseParameters() {
        return includeResponseParameters;
    }

    public void setIncludeResponseParameters(List<String> includeResponseParameters) {
        this.includeResponseParameters = includeResponseParameters;
    }

    public List<String> getExcludeResponseParameters() {
        return excludeResponseParameters;
    }

    public void setExcludeResponseParameters(List<String> excludeResponseParameters) {
        this.excludeResponseParameters = excludeResponseParameters;
    }
}
