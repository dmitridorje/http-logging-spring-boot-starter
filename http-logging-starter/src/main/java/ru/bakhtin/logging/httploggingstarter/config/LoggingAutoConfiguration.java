package ru.bakhtin.logging.httploggingstarter.config;

import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import ru.bakhtin.logging.httploggingstarter.aspect.HttpLoggingAspect;
import ru.bakhtin.logging.httploggingstarter.properties.HttpLoggingProperties;

@AutoConfiguration
@EnableConfigurationProperties(HttpLoggingProperties.class)
@ConditionalOnProperty(prefix = "http-logging", name = "enabled", havingValue = "true", matchIfMissing = true)
public class LoggingAutoConfiguration {

    @Bean
    public HttpLoggingAspect httpLoggingAspect(HttpLoggingProperties properties) {
        return new HttpLoggingAspect(properties);
    }
}
