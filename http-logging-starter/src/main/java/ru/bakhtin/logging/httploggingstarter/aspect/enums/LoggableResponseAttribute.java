package ru.bakhtin.logging.httploggingstarter.aspect.enums;

import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpStatus;
import ru.bakhtin.logging.httploggingstarter.aspect.LoggableAttribute;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

public enum LoggableResponseAttribute implements LoggableAttribute<HttpServletResponse> {
    STATUS("Response Status: {}", response -> HttpStatus.valueOf(response.getStatus()).toString()),
    HEADERS("Headers: {}", response -> getHeaders(response).toString()),
    CONTENT_TYPE("Content Type: {}", ServletResponse::getContentType),
    CHARACTER_ENCODING("Character Encoding: {}", ServletResponse::getCharacterEncoding),
    REDIRECT_URL("Redirect URL: {}", response -> response.getHeader("Location"));

    private final String messageTemplate;
    private final Function<HttpServletResponse, String> valueExtractor;

    LoggableResponseAttribute(String messageTemplate, Function<HttpServletResponse, String> valueExtractor) {
        this.messageTemplate = messageTemplate;
        this.valueExtractor = valueExtractor;
    }

    @Override
    public String getMessageTemplate() {
        return messageTemplate;
    }

    @Override
    public String getValue(HttpServletResponse response) {
        return valueExtractor.apply(response);
    }

    private static Map<String, String> getHeaders(HttpServletResponse response) {
        Map<String, String> headers = new HashMap<>();
        response.getHeaderNames().forEach(name -> headers.put(name, response.getHeader(name)));
        return headers;
    }
}
