package ru.bakhtin.logging.httploggingstarter.aspect.enums;

import jakarta.servlet.http.HttpServletRequest;
import ru.bakhtin.logging.httploggingstarter.aspect.LoggableAttribute;

import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

public enum LoggableRequestAttribute implements LoggableAttribute<HttpServletRequest> {
    METHOD("HTTP Method: {}", HttpServletRequest::getMethod),
    URI("Request URI: {}", HttpServletRequest::getRequestURI),
    QUERY_STRING("Query String: {}", HttpServletRequest::getQueryString),
    HEADERS("Headers: {}", request -> getHeaders(request).toString()),
    CLIENT_IP("Client IP: {}", HttpServletRequest::getRemoteAddr),
    USER_AGENT("User-Agent: {}", request -> request.getHeader("User-Agent"));

    private final String messageTemplate;
    private final Function<HttpServletRequest, String> valueExtractor;

    LoggableRequestAttribute(String messageTemplate, Function<HttpServletRequest, String> valueExtractor) {
        this.messageTemplate = messageTemplate;
        this.valueExtractor = valueExtractor;
    }

    @Override
    public String getMessageTemplate() {
        return messageTemplate;
    }

    @Override
    public String getValue(HttpServletRequest request) {
        return valueExtractor.apply(request);
    }

    private static Map<String, String> getHeaders(HttpServletRequest request) {
        Map<String, String> headers = new HashMap<>();
        Enumeration<String> headerNames = request.getHeaderNames();
        while (headerNames.hasMoreElements()) {
            String headerName = headerNames.nextElement();
            headers.put(headerName, request.getHeader(headerName));
        }
        return headers;
    }
}
