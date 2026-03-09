package org.example.testapi.config;

import org.flywaydb.core.Flyway;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.config.BeanFactoryPostProcessor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.sql.DataSource;
import java.util.ArrayList;
import java.util.List;

@Configuration
public class FlywayConfig {

    @Bean
    public Flyway flyway(DataSource dataSource) {
        Flyway flyway = Flyway.configure()
                .dataSource(dataSource)
                .locations("classpath:db/migration")
                .load();
        flyway.migrate();
        return flyway;
    }

    @Bean
    static BeanFactoryPostProcessor flywayDependencyPostProcessor() {
        return factory -> {
            if (factory.containsBeanDefinition("entityManagerFactory")) {
                BeanDefinition bd = factory.getBeanDefinition("entityManagerFactory");
                String[] existing = bd.getDependsOn();
                List<String> deps = existing != null ? new ArrayList<>(List.of(existing)) : new ArrayList<>();
                deps.add("flyway");
                bd.setDependsOn(deps.toArray(String[]::new));
            }
        };
    }
}
