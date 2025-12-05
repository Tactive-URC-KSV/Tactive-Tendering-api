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
    basePackages = "com.ksv.tactive.company.dao",
    entityManagerFactoryRef = "companyEntityManagerFactory",
    transactionManagerRef = "companyTransactionManager"
)
public class CompanyDbConfig {
	
	@Bean
	@ConfigurationProperties("spring.datasource.company")
	DataSourceProperties companyDataSourceProperties() {
	    return new DataSourceProperties();
	}
    @Bean(name = "companyDataSource")
	@ConfigurationProperties(prefix = "spring.datasource.company")
    DataSource companyDataSource() {
        return companyDataSourceProperties()
        		.initializeDataSourceBuilder()
        		.build();
    }
    @Bean(name = "companyEntityManagerFactory")
    LocalContainerEntityManagerFactoryBean companyEntityManagerFactory(
            EntityManagerFactoryBuilder builder,
            @Qualifier("companyDataSource") DataSource dataSource) {
        Map<String, Object> properties = new HashMap<>();
        properties.put("hibernate.dialect", "org.hibernate.dialect.PostgreSQLDialect");
        properties.put("hibernate.hbm2ddl.auto", "update");
        return builder
                .dataSource(dataSource)
                .packages("com.ksv.tactive.company.entity")
                .persistenceUnit("company")
                .properties(properties)
                .build();
    }
    @Bean(name = "companyTransactionManager")
    PlatformTransactionManager companyTransactionManager(
            @Qualifier("companyEntityManagerFactory") LocalContainerEntityManagerFactoryBean emf) {
        return new JpaTransactionManager(emf.getObject());
    }
}