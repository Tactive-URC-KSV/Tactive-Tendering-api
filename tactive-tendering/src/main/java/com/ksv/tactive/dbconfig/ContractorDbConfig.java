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
    basePackages = "com.ksv.tactive.contractor.dao",
    entityManagerFactoryRef = "contractorEntityManagerFactory",
    transactionManagerRef = "contractorTransactionManager"
)
public class ContractorDbConfig {

	@Bean
	@ConfigurationProperties("spring.datasource.contractor")
	DataSourceProperties contractorDataSourceProperties() {
	    return new DataSourceProperties();
	}
	
    @Bean(name = "contractorDataSource")
    @ConfigurationProperties(prefix = "spring.datasource.contractor")
    DataSource contractorDataSource() {
        return contractorDataSourceProperties()
				.initializeDataSourceBuilder()
				.build();
    }
	
    @Bean(name = "contractorEntityManagerFactory")
    LocalContainerEntityManagerFactoryBean contractorEntityManagerFactory(
            EntityManagerFactoryBuilder builder,
            @Qualifier("contractorDataSource") DataSource dataSource) {
        Map<String, Object> properties = new HashMap<>();
        properties.put("hibernate.dialect", "org.hibernate.dialect.PostgreSQLDialect");
        properties.put("hibernate.hbm2ddl.auto", "update");
        return builder
                .dataSource(dataSource)
                .packages("com.ksv.tactive.contractor.entity")
                .persistenceUnit("contractor")
                .properties(properties)
                .build();
    }
	
    @Bean(name = "contractorTransactionManager")
    PlatformTransactionManager contractorTransactionManager(
            @Qualifier("contractorEntityManagerFactory") LocalContainerEntityManagerFactoryBean emf) {
        return new JpaTransactionManager(emf.getObject());
    }

}
