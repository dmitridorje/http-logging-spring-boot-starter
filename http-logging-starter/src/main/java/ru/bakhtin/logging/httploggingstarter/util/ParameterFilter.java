package ru.bakhtin.logging.httploggingstarter.util;

import jakarta.annotation.PostConstruct;
import ru.bakhtin.logging.httploggingstarter.properties.HttpLoggingProperties;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class ParameterFilter<T extends Enum<T>> {
    private final HttpLoggingProperties properties;
    private final Class<T> enumClass;
    private final boolean isRequest;
    private List<T> includeParameters;
    private List<T> excludeParameters;


    public ParameterFilter(HttpLoggingProperties properties, Class<T> enumClass, boolean isRequest) {
        this.properties = properties;
        this.enumClass = enumClass;
        this.isRequest = isRequest;
        init();
    }

    @PostConstruct
    private void init() {
        List<String> includeParametersRaw = getIncludeParameters();
        List<String> excludeParametersRaw = getExcludeParameters();
        includeParameters = convertToLoggableAttribute(includeParametersRaw);
        excludeParameters = convertToLoggableAttribute(excludeParametersRaw);
    }

    private List<String> getIncludeParameters() {
        return Optional.ofNullable(isRequest ? properties.getIncludeRequestParameters()
                        : properties.getIncludeResponseParameters())
                .orElse(Collections.emptyList());
    }

    private List<String> getExcludeParameters() {
        return Optional.ofNullable(isRequest ? properties.getExcludeRequestParameters()
                        : properties.getExcludeResponseParameters())
                .orElse(Collections.emptyList());
    }

    private List<T> convertToLoggableAttribute(List<String> rawValues) {
        return rawValues.stream()
                .flatMap(value -> {
                    if ("*".equals(value)) {
                        return Stream.of(enumClass.getEnumConstants());
                    }
                    try {
                        return Stream.of(Enum.valueOf(enumClass, value.trim()));
                    } catch (IllegalArgumentException e) {
                        return Stream.empty();
                    }
                })
                .collect(Collectors.toList());
    }

    public List<T> getFinalParameters() {
        List<T> finalIncludeParameters = new ArrayList<>(includeParameters);
        finalIncludeParameters.removeAll(excludeParameters);
        return finalIncludeParameters;
    }
}
