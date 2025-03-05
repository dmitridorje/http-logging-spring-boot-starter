package ru.bakhtin.logging.httploggingstarter.config;

import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.DependsOn;
import ru.bakhtin.logging.httploggingstarter.aspect.HttpLoggingAspect;
import ru.bakhtin.logging.httploggingstarter.aspect.enums.LoggableRequestAttribute;
import ru.bakhtin.logging.httploggingstarter.aspect.enums.LoggableResponseAttribute;
import ru.bakhtin.logging.httploggingstarter.properties.HttpLoggingProperties;
import ru.bakhtin.logging.httploggingstarter.util.ParameterFilter;

@AutoConfiguration
@EnableConfigurationProperties(HttpLoggingProperties.class)
@ConditionalOnProperty(prefix = "http-logging", name = "enabled", havingValue = "true", matchIfMissing = true)
public class LoggingAutoConfiguration {

    @Bean
    public ParameterFilter<LoggableRequestAttribute> requestParameterFilter(HttpLoggingProperties properties) {
        return new ParameterFilter<>(properties, LoggableRequestAttribute.class, true);
    }

    @Bean
    public ParameterFilter<LoggableResponseAttribute> responseParameterFilter(HttpLoggingProperties properties) {
        return new ParameterFilter<>(properties, LoggableResponseAttribute.class, false);
    }

    @Bean
    @DependsOn({"requestParameterFilter", "responseParameterFilter"})
    public HttpLoggingAspect httpLoggingAspect(ParameterFilter<LoggableRequestAttribute> requestParameterFilter,
                                               ParameterFilter<LoggableResponseAttribute> responseParameterFilter,
                                               HttpLoggingProperties properties) {
        return new HttpLoggingAspect(requestParameterFilter, responseParameterFilter, properties);
    }
}
