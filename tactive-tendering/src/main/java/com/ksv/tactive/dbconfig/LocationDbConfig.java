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
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.transaction.PlatformTransactionManager;

@Configuration
@EnableJpaRepositories(
    basePackages = "com.ksv.tactive.location.dao",
    entityManagerFactoryRef = "locationEntityManagerFactory",
    transactionManagerRef = "locationTransactionManager"
)
public class LocationDbConfig {
	@Bean
	@ConfigurationProperties("spring.datasource.location")
	DataSourceProperties locationDataSourceProperties() {
	    return new DataSourceProperties();
	}
	
    @Bean(name = "locationDataSource")
    @ConfigurationProperties(prefix = "spring.datasource.location")
    DataSource locationDataSource() {
        return locationDataSourceProperties()
				.initializeDataSourceBuilder()
				.build();
    }
	
    @Bean(name = "locationEntityManagerFactory")
    LocalContainerEntityManagerFactoryBean locationEntityManagerFactory(
            EntityManagerFactoryBuilder builder,
            @Qualifier("locationDataSource") DataSource dataSource) {
        Map<String, Object> properties = new HashMap<>();
        properties.put("hibernate.dialect", "org.hibernate.dialect.PostgreSQLDialect");
        properties.put("hibernate.hbm2ddl.auto", "update");
        return builder
                .dataSource(dataSource)
                .packages("com.ksv.tactive.location.entity")
                .persistenceUnit("location")
                .properties(properties)
                .build();
    }
	
    @Bean(name = "locationTransactionManager")
    PlatformTransactionManager locationTransactionManager(
            @Qualifier("locationEntityManagerFactory") LocalContainerEntityManagerFactoryBean emf) {
        return new JpaTransactionManager(emf.getObject());
    }

}
