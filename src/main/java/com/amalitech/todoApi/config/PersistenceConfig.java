package com.amalitech.todoApi.config;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import jakarta.persistence.EntityManagerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.context.support.PropertySourcesPlaceholderConfigurer;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.orm.jpa.vendor.HibernateJpaVendorAdapter;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.EnableTransactionManagement;

import javax.sql.DataSource;
import java.util.Properties;

@Configuration
@EnableTransactionManagement
@EnableJpaRepositories(basePackages = "com.amalitech.todoApi.repository")
@PropertySource(value = "classpath:application.yml", factory = YamlPropertySourceFactory.class)
public class PersistenceConfig {

    /* Must be static — instantiated before any @Value injection so
       it can register itself as the placeholder resolver for ${...}. */
    @Bean
    public static PropertySourcesPlaceholderConfigurer propertySourcesPlaceholderConfigurer() {
        return new PropertySourcesPlaceholderConfigurer();
    }

    // ── Datasource ───────────────────────────────────────────────────────
    // Values in application.yml embed env-var placeholders, e.g.
    // url: jdbc:postgresql://${DB_HOST:localhost}:${DB_PORT:5432}/${DB_NAME:todo}
    // Spring resolves those nested placeholders automatically.

    @Value("${spring.datasource.url}")
    private String datasourceUrl;

    @Value("${spring.datasource.username}")
    private String datasourceUsername;

    @Value("${spring.datasource.password}")
    private String datasourcePassword;

    @Value("${spring.datasource.driver-class-name}")
    private String driverClassName;

    // ── HikariCP pool ────────────────────────────────────────────────────

    @Value("${spring.datasource.hikari.maximum-pool-size:10}")
    private int maxPoolSize;

    @Value("${spring.datasource.hikari.minimum-idle:2}")
    private int minIdle;

    @Value("${spring.datasource.hikari.connection-timeout:30000}")
    private long connectionTimeout;

    @Value("${spring.datasource.hikari.idle-timeout:600000}")
    private long idleTimeout;

    @Value("${spring.datasource.hikari.max-lifetime:840000}")
    private long maxLifetime;

    @Value("${spring.datasource.hikari.keepalive-time:300000}")
    private long keepaliveTime;

    @Value("${spring.datasource.hikari.connection-test-query:SELECT 1}")
    private String connectionTestQuery;

    // ── JPA / Hibernate ──────────────────────────────────────────────────

    @Value("${spring.jpa.hibernate.ddl-auto:update}")
    private String ddlAuto;

    @Value("${spring.jpa.properties.hibernate.dialect:org.hibernate.dialect.PostgreSQLDialect}")
    private String dialect;

    @Value("${spring.jpa.properties.hibernate.jdbc.batch_size:20}")
    private int batchSize;

    @Value("${spring.jpa.show-sql:false}")
    private boolean showSql;

    // ── Beans ────────────────────────────────────────────────────────────

    @Bean
    public DataSource dataSource() {
        HikariConfig cfg = new HikariConfig();
        cfg.setJdbcUrl(datasourceUrl);
        cfg.setUsername(datasourceUsername);
        cfg.setPassword(datasourcePassword);
        cfg.setDriverClassName(driverClassName);
        cfg.setPoolName("TodoHikariPool");
        cfg.setMaximumPoolSize(maxPoolSize);
        cfg.setMinimumIdle(minIdle);
        cfg.setConnectionTimeout(connectionTimeout);
        cfg.setIdleTimeout(idleTimeout);
        cfg.setMaxLifetime(maxLifetime);
        cfg.setKeepaliveTime(keepaliveTime);
        cfg.setConnectionTestQuery(connectionTestQuery);
        cfg.addDataSourceProperty("reWriteBatchedInserts", "true");
        return new HikariDataSource(cfg);
    }

    @Bean
    public LocalContainerEntityManagerFactoryBean entityManagerFactory() {
        HibernateJpaVendorAdapter vendorAdapter = new HibernateJpaVendorAdapter();
        vendorAdapter.setGenerateDdl(true);
        vendorAdapter.setShowSql(showSql);

        LocalContainerEntityManagerFactoryBean factory = new LocalContainerEntityManagerFactoryBean();
        factory.setJpaVendorAdapter(vendorAdapter);
        factory.setPackagesToScan("com.amalitech.todoApi.models");
        factory.setDataSource(dataSource());
        factory.setJpaProperties(hibernateProperties());
        return factory;
    }

    @Bean
    public PlatformTransactionManager transactionManager(EntityManagerFactory entityManagerFactory) {
        JpaTransactionManager txManager = new JpaTransactionManager();
        txManager.setEntityManagerFactory(entityManagerFactory);
        return txManager;
    }

    private Properties hibernateProperties() {
        Properties props = new Properties();
        props.setProperty("hibernate.dialect",         dialect);
        props.setProperty("hibernate.hbm2ddl.auto",    ddlAuto);
        props.setProperty("hibernate.jdbc.batch_size", String.valueOf(batchSize));
        props.setProperty("hibernate.format_sql",      "false");
        return props;
    }
}
