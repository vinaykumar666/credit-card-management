package com.cards.common.config;

import com.cards.common.error.ErrorCodeProperties;
import com.cards.common.eventstore.AppEventStore;
import com.cards.common.logging.MethodLifecycleAspect;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;

/**
 * Core cards-common auto-configuration (safe for WebFlux / non-JDBC services such as the gateway).
 * Servlet footfall filter: {@link CardsCommonServletAutoConfiguration}.
 * JDBC app_event store: {@link CardsCommonJdbcAutoConfiguration}.
 */
@Configuration
@EnableConfigurationProperties(ErrorCodeProperties.class)
@PropertySource(value = "classpath:error-codes.yml", factory = YamlPropertySourceFactory.class)
public class CardsCommonAutoConfiguration {

    @Bean
    public MethodLifecycleAspect methodLifecycleAspect(
            org.springframework.beans.factory.ObjectProvider<AppEventStore> appEventStore,
            @Value("${spring.application.name:unknown-service}") String serviceName
    ) {
        return new MethodLifecycleAspect(appEventStore, serviceName);
    }
}
