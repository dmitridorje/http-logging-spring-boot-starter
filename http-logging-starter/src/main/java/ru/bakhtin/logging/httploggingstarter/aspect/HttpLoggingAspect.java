package ru.bakhtin.logging.httploggingstarter.aspect;

import jakarta.annotation.PostConstruct;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.AfterThrowing;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.bakhtin.logging.httploggingstarter.exception.TaskDtoValidationException;
import ru.bakhtin.logging.httploggingstarter.properties.HttpLoggingProperties;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.List;

@Aspect
public class HttpLoggingAspect {
    private static final Logger log = LoggerFactory.getLogger(HttpLoggingAspect.class);

    private String selectedLogLevel;
    private final HttpLoggingProperties properties;

    public HttpLoggingAspect(HttpLoggingProperties properties) {
        this.properties = properties;
        init();
    }

    @PostConstruct
    private void init() {
        selectedLogLevel = properties.getLevel() != null ? properties.getLevel() : "INFO";
    }

    @Around("@annotation(ru.bakhtin.logging.httploggingstarter.aspect.annotation.LogControllerMethodCall)")
    public Object logMethodCall(ProceedingJoinPoint joinPoint) throws Throwable {
        String className = joinPoint.getTarget().getClass().getSimpleName();
        String methodName = joinPoint.getSignature().getName();
        String args = Arrays.toString(joinPoint.getArgs());

        logAtCustomLevel("Class: {} | Method: {} | Args: {}", className, methodName, args);

        try {
            Object result = joinPoint.proceed();
            logAtCustomLevel("Class: {} | Method: {} | Status: SUCCESS", className, methodName);
            return result;
        } catch (Exception e) {
            log.error("Class: {} | Method: {} | Status: FAILED | Exception: {}",
                    className, methodName, e.getMessage());
            throw e;
        }
    }

    @AfterThrowing(
            pointcut = "@annotation(ru.bakhtin.logging.httploggingstarter.aspect.annotation.LogNotFoundException)",
            throwing = "exception"
    )
    public void logNotFoundExceptions(JoinPoint joinPoint, Throwable exception) {
        if ("EntityNotFoundException".equals(exception.getClass().getSimpleName())) {
            log.error("EntityNotFoundException in {}: {}", joinPoint.getSignature().toShortString(), exception.getMessage(), exception);
        }
    }

    @AfterReturning(
            pointcut = "@annotation(ru.bakhtin.logging.httploggingstarter.aspect.annotation.LogAfterReturning)",
            returning = "result")
    public void logAfterReturning(JoinPoint joinPoint, Object result) {
        if (result instanceof List<?> tasks) {
            logAtCustomLevel("Method '{}' returned {} tasks", joinPoint.getSignature().toShortString(), tasks.size());
        }
    }

    @Before("@annotation(ru.bakhtin.logging.httploggingstarter.aspect.annotation.ValidateDtoBeforeCreation) && args(dto)")
    public void validateDtoBeforeCreation(Object dto) {
        try {
            Class<?> dtoClass = dto.getClass();

            Method getTitle = getMethodIfExists(dtoClass, "getTitle");
            Method getDescription = getMethodIfExists(dtoClass, "getDescription");
            Method getUserId = getMethodIfExists(dtoClass, "getUserId");

            if (getTitle == null || getDescription == null || getUserId == null) {
                log.warn("DTO validation skipped: missing required methods in class {}", dtoClass.getName());
                return;
            }

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

            logAtCustomLevel("TaskDto validated successfully before creating task, taskDto: title={}, description={}, userId={}",
                    title, description, userId);
        } catch (IllegalAccessException | InvocationTargetException e) {
            throw new RuntimeException("DTO validation failed due to method invocation error", e);
        }
    }

    @Around("@annotation(ru.bakhtin.logging.httploggingstarter.aspect.annotation.MeasureExecutionTime)")
    public Object measureExecutionTime(ProceedingJoinPoint joinPoint) throws Throwable {
        long startTime = System.currentTimeMillis();

        Object result;
        try {
            result = joinPoint.proceed();
        } catch (Throwable ex) {
            log.error("Exception during request processing", ex);
            throw ex;
        }

        long endTime = System.currentTimeMillis();
        long executionTime = endTime - startTime;

        logAtCustomLevel("Execution time of {}: {} ms", joinPoint.getSignature().toShortString(), executionTime);

        return result;
    }

    private void logAtCustomLevel(String message, Object... args) {
        switch (selectedLogLevel.toUpperCase()) {
            case "DEBUG" -> log.debug(message, args);
            case "WARN" -> log.warn(message, args);
            case "ERROR" -> log.error(message, args);
            default -> log.info(message, args);
        }
    }

    private Method getMethodIfExists(Class<?> clazz, String methodName) {
        try {
            return clazz.getMethod(methodName);
        } catch (NoSuchMethodException e) {
            return null;
        }
    }
}
