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
    basePackages = "com.ksv.tactive.currency.dao",
    entityManagerFactoryRef = "currencyEntityManagerFactory",
    transactionManagerRef = "currencyTransactionManager"
)
public class CurrencyMasterDbConfig {
	@Bean
	@ConfigurationProperties("spring.datasource.currency")
	DataSourceProperties currencyDataSourceProperties() {
	    return new DataSourceProperties();
	}
	
    @Bean(name = "currencyDataSource")
    @ConfigurationProperties(prefix = "spring.datasource.currency")
    DataSource currencyDataSource() {
        return currencyDataSourceProperties()
				.initializeDataSourceBuilder()
				.build();
    }
	
    @Bean(name = "currencyEntityManagerFactory")
    LocalContainerEntityManagerFactoryBean currencyEntityManagerFactory(
            EntityManagerFactoryBuilder builder,
            @Qualifier("currencyDataSource") DataSource dataSource) {
        Map<String, Object> properties = new HashMap<>();
        properties.put("hibernate.dialect", "org.hibernate.dialect.PostgreSQLDialect");
        properties.put("hibernate.hbm2ddl.auto", "update");
        return builder
                .dataSource(dataSource)
                .packages("com.ksv.tactive.currency.entity")
                .persistenceUnit("currency")
                .properties(properties)
                .build();
    }
	
    @Bean(name = "currencyTransactionManager")
    PlatformTransactionManager currencyTransactionManager(
            @Qualifier("currencyEntityManagerFactory") LocalContainerEntityManagerFactoryBean emf) {
        return new JpaTransactionManager(emf.getObject());
    }

}
