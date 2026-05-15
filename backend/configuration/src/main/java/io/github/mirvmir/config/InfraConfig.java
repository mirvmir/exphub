package io.github.mirvmir.config;

import io.github.mirvmir.common.properties.AppProperties;
import io.github.mirvmir.properties.DbProperties;
import io.github.mirvmir.properties.HibernateProperties;
import liquibase.integration.spring.SpringLiquibase;
import lombok.AllArgsConstructor;
import org.hibernate.SessionFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.DependsOn;
import org.springframework.context.annotation.PropertySource;
import org.springframework.jdbc.datasource.DriverManagerDataSource;
import org.springframework.orm.hibernate5.HibernateTransactionManager;
import org.springframework.orm.hibernate5.LocalSessionFactoryBean;
import org.springframework.transaction.PlatformTransactionManager;

import javax.sql.DataSource;
import java.util.Properties;

@AllArgsConstructor
@Configuration
public class InfraConfig {

    private final AppProperties appProperties;

    @Bean
    public DataSource dataSource(
            DbProperties dbProperties,
            HibernateProperties hibernateProperties
    ) {
        DriverManagerDataSource ds = new DriverManagerDataSource();
        ds.setDriverClassName(hibernateProperties.getDriver());
        ds.setUrl(dbProperties.getUrl());
        ds.setUsername(dbProperties.getUser());
        ds.setPassword(dbProperties.getPassword());
        return ds;
    }

    @Bean
    public SpringLiquibase liquibase(
            DataSource dataSource,
            @Value("${liquibase.change_log}") String changeLog,
            @Value("${liquibase.enabled:true}") boolean enabled
    ) {
        SpringLiquibase liquibase = new SpringLiquibase();
        liquibase.setDataSource(dataSource);
        liquibase.setChangeLog(changeLog);
        liquibase.setShouldRun(enabled);
        return liquibase;
    }

    @Bean
    @DependsOn("liquibase")
    public LocalSessionFactoryBean sessionFactory(
            DataSource dataSource,
            HibernateProperties hibernateProperties
    ) {
        LocalSessionFactoryBean factory = new LocalSessionFactoryBean();
        factory.setDataSource(dataSource);

        factory.setPackagesToScan(
                "io.github.mirvmir.activity.application.persistence.entity",
                "io.github.mirvmir.payment.application.persistence.entity",
                "io.github.mirvmir.catalog.application.persistence.entity",
                "io.github.mirvmir.course.application.persistence.entity",
                "io.github.mirvmir.enrollment.application.persistence.entity",
                "io.github.mirvmir.identity.application.persistence.entity",
                "io.github.mirvmir.identity.domain",
                "io.github.mirvmir.media.application.persistence.entity",
                "io.github.mirvmir.practice.application.persistence.entity",
                "io.github.mirvmir.profile.application.persistence.entity",
                "io.github.mirvmir.review.application.persistence.entity",
                "io.github.mirvmir.taxonomy.application.persistence.entity",
                "io.github.mirvmir.wallet.application.persistence.entity");

        Properties props = new Properties();
        props.put("hibernate.dialect", hibernateProperties.getDialect());
        props.put("hibernate.hbm2ddl.auto", hibernateProperties.getHbm2ddlAuto());

        factory.setHibernateProperties(props);

        return factory;
    }

    @Bean
    public PlatformTransactionManager transactionManager(SessionFactory sessionFactory) {
        return new HibernateTransactionManager(sessionFactory);
    }

    @Bean
    public HibernateProperties hibernateProperties(
            @Value("${hibernate.connection.driver_class}")
            String driver,
            @Value("${hibernate.dialect}")
            String dialect,
            @Value("${hibernate.hbm2ddl.auto}")
            String hbm2ddlAuto

    ) {
        return new HibernateProperties(
                driver,
                dialect,
                hbm2ddlAuto
        );
    }

    @Bean
    public DbProperties dbProperties(
            AppProperties appProperties,
            @Value("${db.web.url}")
            String dbWebUrl,
            @Value("${db.docker.url}")
            String dbDockerUrl,
            @Value("${db.user}")
            String dbUser,
            @Value("${db.password}")
            String dbPassword

    ) {
        return new DbProperties(
                appProperties,
                dbWebUrl,
                dbDockerUrl,
                dbUser,
                dbPassword
        );
    }
}