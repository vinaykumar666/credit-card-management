package com.cards.common.config;

import com.cards.common.error.ErrorCodeProperties;
import com.cards.common.eventstore.AppEventFootfallFilter;
import com.cards.common.eventstore.AppEventStore;
import com.cards.common.eventstore.JdbcAppEventStore;
import com.cards.common.logging.MethodLifecycleAspect;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.jdbc.core.JdbcTemplate;

import javax.sql.DataSource;

/**
 * Spring Boot configuration for the cards-common library.
 * Loads error codes and registers lifecycle logging + optional app_event footfall persistence.
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

    @Bean
    @ConditionalOnClass(JdbcTemplate.class)
    @ConditionalOnBean(DataSource.class)
    @ConditionalOnProperty(name = "cards.app-events.enabled", havingValue = "true", matchIfMissing = true)
    public AppEventStore appEventStore(JdbcTemplate jdbcTemplate) {
        return new JdbcAppEventStore(jdbcTemplate);
    }

    @Bean
    @ConditionalOnWebApplication(type = ConditionalOnWebApplication.Type.SERVLET)
    @ConditionalOnBean(AppEventStore.class)
    @ConditionalOnProperty(name = "cards.app-events.footfall-filter", havingValue = "true", matchIfMissing = true)
    public FilterRegistrationBean<AppEventFootfallFilter> appEventFootfallFilter(
            AppEventStore appEventStore,
            @Value("${spring.application.name:unknown-service}") String serviceName
    ) {
        FilterRegistrationBean<AppEventFootfallFilter> bean = new FilterRegistrationBean<>();
        bean.setFilter(new AppEventFootfallFilter(appEventStore, serviceName));
        bean.setOrder(20);
        bean.addUrlPatterns("/*");
        return bean;
    }
}
