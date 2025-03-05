package ru.bakhtin.logging.httploggingstarter;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import ru.bakhtin.logging.httploggingstarter.properties.HttpLoggingProperties;

@SpringBootApplication
@EnableConfigurationProperties(HttpLoggingProperties.class)
public class HttpLoggingStarterApplication {

    public static void main(String[] args) {
        SpringApplication.run(HttpLoggingStarterApplication.class, args);
    }
}
