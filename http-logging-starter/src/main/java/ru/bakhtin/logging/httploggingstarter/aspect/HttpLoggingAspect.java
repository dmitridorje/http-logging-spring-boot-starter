package ru.bakhtin.logging.httploggingstarter.aspect;

import jakarta.annotation.PostConstruct;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.AfterThrowing;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import ru.bakhtin.logging.httploggingstarter.aspect.enums.LoggableRequestAttribute;
import ru.bakhtin.logging.httploggingstarter.aspect.enums.LoggableResponseAttribute;
import ru.bakhtin.logging.httploggingstarter.exception.TaskDtoValidationException;
import ru.bakhtin.logging.httploggingstarter.properties.HttpLoggingProperties;
import ru.bakhtin.logging.httploggingstarter.util.ParameterFilter;

import java.lang.reflect.Method;
import java.util.List;

@Aspect
public class HttpLoggingAspect {
    private static final Logger log = LoggerFactory.getLogger(HttpLoggingAspect.class);

    private String selectedLogLevel;
    private final ParameterFilter<LoggableRequestAttribute> requestParameterFilter;
    private final ParameterFilter<LoggableResponseAttribute> responseParameterFilter;
    private final HttpLoggingProperties properties;

    public HttpLoggingAspect(ParameterFilter<LoggableRequestAttribute> requestParameterFilter,
                             ParameterFilter<LoggableResponseAttribute> responseParameterFilter,
                             HttpLoggingProperties properties) {
        this.requestParameterFilter = requestParameterFilter;
        this.responseParameterFilter = responseParameterFilter;
        this.properties = properties;
        init();
    }

    @PostConstruct
    private void init() {
        selectedLogLevel = properties.getLevel() != null ? properties.getLevel() : "INFO";
    }

    @Around("@annotation(ru.bakhtin.logging.httploggingstarter.aspect.annotation.LogHttpRequestAndResponse)")
    public Object logHttpRequestAndResponse(ProceedingJoinPoint joinPoint) throws Throwable {
        HttpServletRequest request = ((ServletRequestAttributes)
                RequestContextHolder.currentRequestAttributes()).getRequest();
        HttpServletResponse response = ((ServletRequestAttributes)
                RequestContextHolder.currentRequestAttributes()).getResponse();

        List<LoggableRequestAttribute> filteredRequestParams = requestParameterFilter.getFinalParameters();
        List<LoggableResponseAttribute> filteredResponseParams = responseParameterFilter.getFinalParameters();

        logAtCustomLevel("INFO", "Start logging request");
        logData(filteredRequestParams, request);

        Object result;
        try {
            result = joinPoint.proceed();
        } catch (Throwable ex) {
            logAtCustomLevel("ERROR", "Exception during request processing", ex);
            throw ex;
        }

        logAtCustomLevel("INFO", "Start logging response");
        logData(filteredResponseParams, response);

        return result;
    }

    @AfterThrowing(
            pointcut = "@annotation(ru.bakhtin.logging.httploggingstarter.aspect.annotation.LogNotFoundException)",
            throwing = "exception"
    )
    public void logNotFoundExceptions(JoinPoint joinPoint, Throwable exception) {
        if ("EntityNotFoundException".equals(exception.getClass().getSimpleName())) {
            logAtCustomLevel("ERROR", "EntityNotFoundException in {}: {}",
                    joinPoint.getSignature().toShortString(), exception.getMessage(), exception);
        }
    }

    @AfterReturning(
            pointcut = "@annotation(ru.bakhtin.logging.httploggingstarter.aspect.annotation.LogAfterReturning)",
            returning = "result")
    public void logAfterReturning(JoinPoint joinPoint, Object result) {
        if (result instanceof List<?> tasks) {
            logAtCustomLevel("INFO", "Method '{}' returned {} tasks",
                    joinPoint.getSignature().toShortString(), tasks.size());
        }
    }

    @Before("execution(* *..controller..*.create*(..)) && args(dto)")
    public void validateDtoBeforeCreation(Object dto) {

        try {
            Method getTitle = dto.getClass().getMethod("getTitle");
            Method getDescription = dto.getClass().getMethod("getDescription");
            Method getUserId = dto.getClass().getMethod("getUserId");

            String title = (String) getTitle.invoke(dto);
            String description = (String) getDescription.invoke(dto);
            Long userId = (Long) getUserId.invoke(dto);

            if (title == null || title.isEmpty()) {
                throw new TaskDtoValidationException("Task title cannot be empty");
            }
            if (description == null || description.isEmpty()) {
                throw new TaskDtoValidationException("Task description cannot be empty");
            }
            if (userId == null) {
                throw new TaskDtoValidationException("User ID cannot be null");
            }

            logAtCustomLevel("INFO", "TaskDto validated successfully before creating task, taskDto: title={}, description={}, userId={}",
                    title, description, userId);
        } catch (Exception e) {
            throw new RuntimeException("DTO validation failed", e);
        }
    }

    @Around("@annotation(ru.bakhtin.logging.httploggingstarter.aspect.annotation.MeasureExecutionTime)")
    public Object measureExecutionTime(ProceedingJoinPoint joinPoint) throws Throwable {
        long startTime = System.currentTimeMillis();

        Object result;
        try {
            result = joinPoint.proceed();
        } catch (Throwable ex) {
            logAtCustomLevel("ERROR", "Exception during request processing", ex);
            throw ex;
        }

        long endTime = System.currentTimeMillis();
        long executionTime = endTime - startTime;

        logAtCustomLevel("INFO", "Execution time of {}: {} ms",
                joinPoint.getSignature().toShortString(), executionTime);

        return result;
    }

    private <T extends Enum<T> & LoggableAttribute<U>, U> void logData(List<T> filteredParams, U requestOrResponse) {
        for (T attribute : filteredParams) {
            logAtCustomLevel("INFO", attribute.getMessageTemplate(), attribute.getValue(requestOrResponse));
        }
    }

    private void logAtCustomLevel(String level, String message, Object... args) {
        String configuredLevel = selectedLogLevel.toUpperCase();

        switch (level.toUpperCase()) {
            case "DEBUG" -> {
                if (log.isDebugEnabled() && shouldLogAtLevel(configuredLevel, "DEBUG")) {
                    log.debug(message, args);
                }
            }
            case "INFO" -> {
                if (log.isInfoEnabled() && shouldLogAtLevel(configuredLevel, "INFO")) {
                    log.info(message, args);
                }
            }
            case "WARN" -> {
                if (log.isWarnEnabled() && shouldLogAtLevel(configuredLevel, "WARN")) {
                    log.warn(message, args);
                }
            }
            case "ERROR" -> {
                if (log.isErrorEnabled() && shouldLogAtLevel(configuredLevel, "ERROR")) {
                    log.error(message, args);
                }
            }
            default -> log.info(message, args);
        }
    }

    private boolean shouldLogAtLevel(String configuredLevel, String messageLevel) {
        return switch (configuredLevel) {
            case "INFO" -> !messageLevel.equalsIgnoreCase("DEBUG");
            case "WARN" ->
                    messageLevel.equalsIgnoreCase("WARN") || messageLevel.equalsIgnoreCase("ERROR");
            case "ERROR" -> messageLevel.equalsIgnoreCase("ERROR");
            default -> true;
        };
    }
}
