package com.example.demo.config.datasource;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.core.io.ClassPathResource;
import org.springframework.jdbc.datasource.init.ResourceDatabasePopulator;

import javax.sql.DataSource;
import java.util.HashMap;
import java.util.Map;

@Configuration
@Slf4j
public class DataSourceConfig {

    @Value("${spring.datasource.url}")
    private String mysqlUrl;

    @Value("${spring.datasource.username}")
    private String mysqlUser;

    @Value("${spring.datasource.password}")
    private String mysqlPassword;

    @Value("${spring.datasource.driver-class-name:com.mysql.cj.jdbc.Driver}")
    private String mysqlDriver;

    @Bean(name = "mysqlDataSource")
    public DataSource mysqlDataSource() {
        HikariConfig config = new HikariConfig();
        config.setJdbcUrl(mysqlUrl);
        config.setUsername(mysqlUser);
        config.setPassword(mysqlPassword);
        config.setDriverClassName(mysqlDriver);
        config.setPoolName("MySQL-Prod-Pool");
        config.setMaximumPoolSize(10);
        config.setMinimumIdle(2);
        config.setIdleTimeout(60000);
        config.setMaxLifetime(180000);
        config.setConnectionTimeout(30000);
        // Permite arrancar la app aunque MySQL no esté disponible en local al probar Demo
        config.setInitializationFailTimeout(-1);
        return new HikariDataSource(config);
    }

    @Bean(name = "h2DataSource")
    public DataSource h2DataSource() {
        HikariConfig config = new HikariConfig();
        config.setJdbcUrl("jdbc:h2:mem:gestiona_hospedaje_demo;DB_CLOSE_DELAY=-1;DB_CLOSE_ON_EXIT=FALSE;MODE=MySQL;DATABASE_TO_LOWER=TRUE");
        config.setUsername("sa");
        config.setPassword("");
        config.setDriverClassName("org.h2.Driver");
        config.setPoolName("H2-Demo-Pool");
        config.setMaximumPoolSize(10);
        config.setMinimumIdle(2);

        HikariDataSource ds = new HikariDataSource(config);
        inicializarEsquemaH2(ds);
        return ds;
    }

    private void inicializarEsquemaH2(DataSource ds) {
        try {
            log.info("Inicializando esquema DDL en H2 para Modo Demo...");
            ResourceDatabasePopulator populator = new ResourceDatabasePopulator();
            populator.addScript(new ClassPathResource("schema-h2.sql"));
            populator.setContinueOnError(true);
            populator.execute(ds);
            log.info("Esquema DDL H2 inicializado correctamente.");
        } catch (Exception e) {
            log.error("Error al inicializar el esquema H2: {}", e.getMessage(), e);
        }
    }

    @Bean
    @Primary
    public DataSource dataSource() {
        RoutingDataSource routingDataSource = new RoutingDataSource();

        Map<Object, Object> targetDataSources = new HashMap<>();
        targetDataSources.put(DatabaseType.MYSQL, mysqlDataSource());
        targetDataSources.put(DatabaseType.H2, h2DataSource());

        routingDataSource.setTargetDataSources(targetDataSources);
        routingDataSource.setDefaultTargetDataSource(mysqlDataSource());
        routingDataSource.afterPropertiesSet();

        return routingDataSource;
    }
}
