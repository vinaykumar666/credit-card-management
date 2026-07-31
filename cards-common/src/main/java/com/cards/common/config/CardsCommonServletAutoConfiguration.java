package com.cards.common.config;

import com.cards.common.eventstore.AppEventFootfallFilter;
import com.cards.common.eventstore.AppEventStore;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;

/**
 * Servlet-only footfall filter. Skipped on WebFlux apps (API gateway) where
 * {@code jakarta.servlet.Filter} is not on the classpath.
 */
@AutoConfiguration(afterName = "com.cards.common.config.CardsCommonJdbcAutoConfiguration")
@ConditionalOnClass(name = {
        "jakarta.servlet.Filter",
        "org.springframework.boot.web.servlet.FilterRegistrationBean"
})
@ConditionalOnWebApplication(type = ConditionalOnWebApplication.Type.SERVLET)
@ConditionalOnProperty(name = "cards.app-events.footfall-filter", havingValue = "true", matchIfMissing = true)
public class CardsCommonServletAutoConfiguration {

    @Bean
    @ConditionalOnBean(AppEventStore.class)
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
