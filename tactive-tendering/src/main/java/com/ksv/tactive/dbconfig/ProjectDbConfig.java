package com.ksv.tactive.dbconfig;

import java.util.HashMap;
import java.util.Map;

import javax.sql.DataSource;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.jdbc.DataSourceProperties;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.orm.jpa.EntityManagerFactoryBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.transaction.PlatformTransactionManager;

@Configuration
@EnableJpaRepositories(
    basePackages = "com.ksv.tactive.project.dao",
    entityManagerFactoryRef = "projectEntityManagerFactory",
    transactionManagerRef = "projectTransactionManager"
)
public class ProjectDbConfig {

	@Primary
	@Bean
	@ConfigurationProperties("spring.datasource.project")
	DataSourceProperties projectDataSourceProperties() {
	    return new DataSourceProperties();
	}

	@Primary
    @Bean(name = "projectDataSource")
    @ConfigurationProperties(prefix = "spring.datasource.project")
    DataSource projectDataSource() {
        return projectDataSourceProperties()
				.initializeDataSourceBuilder()
				.build();
    }

	@Primary
    @Bean(name = "projectEntityManagerFactory")
    LocalContainerEntityManagerFactoryBean projectEntityManagerFactory(
            EntityManagerFactoryBuilder builder,
            @Qualifier("projectDataSource") DataSource dataSource) {
        Map<String, Object> properties = new HashMap<>();
        properties.put("hibernate.dialect", "org.hibernate.dialect.PostgreSQLDialect");
        properties.put("hibernate.hbm2ddl.auto", "update");
        return builder
                .dataSource(dataSource)
                .packages("com.ksv.tactive.project.entity")
                .persistenceUnit("project")
                .properties(properties)
                .build();
    }

	@Primary
    @Bean(name = "projectTransactionManager")
    PlatformTransactionManager projectTransactionManager(
            @Qualifier("projectEntityManagerFactory") LocalContainerEntityManagerFactoryBean emf) {
        return new JpaTransactionManager(emf.getObject());
    }
}