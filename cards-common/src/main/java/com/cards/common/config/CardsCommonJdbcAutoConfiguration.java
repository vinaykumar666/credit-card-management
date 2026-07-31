package com.cards.common.config;

import com.cards.common.eventstore.AppEventStore;
import com.cards.common.eventstore.JdbcAppEventStore;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.jdbc.core.JdbcTemplate;

import javax.sql.DataSource;

/**
 * JDBC-only auto-configuration. Skipped entirely when {@link JdbcTemplate} is not on the classpath
 * (gateway, BFF, enterprise adapter), which avoids {@code NoClassDefFoundError} and cascading
 * Resilience4j condition failures.
 */
@AutoConfiguration(afterName = "org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration")
@ConditionalOnClass(JdbcTemplate.class)
@ConditionalOnProperty(name = "cards.app-events.enabled", havingValue = "true", matchIfMissing = true)
public class CardsCommonJdbcAutoConfiguration {

    @Bean
    @ConditionalOnBean(DataSource.class)
    public AppEventStore appEventStore(JdbcTemplate jdbcTemplate) {
        return new JdbcAppEventStore(jdbcTemplate);
    }
}
