package com.cards.common.config;

import com.cards.common.error.ErrorCodeProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;

/**
 * Spring Boot configuration for the cards-common library.
 * Loads {@code error-codes.yml} and registers {@link ErrorCodeProperties} so services share one error catalog.
 */
@Configuration
@EnableConfigurationProperties(ErrorCodeProperties.class)
@PropertySource(value = "classpath:error-codes.yml", factory = YamlPropertySourceFactory.class)
public class CardsCommonAutoConfiguration {
}
