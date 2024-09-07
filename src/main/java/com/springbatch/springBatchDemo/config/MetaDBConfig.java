package com.springbatch.springBatchDemo.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.transaction.PlatformTransactionManager;

import javax.sql.DataSource;

@Configuration
public class MetaDBConfig {

    @Bean
    @Primary//db충돌을 맊기위해
    @ConfigurationProperties(prefix = "spring.datasource-meta")
    public DataSource metaDBSource(){
        return DataSourceBuilder.create().build();
    }

    @Bean
    @Primary//충돌방지를 위해
    public PlatformTransactionManager metaTransactionManger(){
        return new DataSourceTransactionManager(metaDBSource());
    }


}
