package com.example.boot_activiti6.config;

import com.example.boot_activiti6.listener.ActivitiGlobalListener;
import org.activiti.engine.delegate.event.ActivitiEventListener;
import org.activiti.spring.SpringAsyncExecutor;
import org.activiti.spring.SpringProcessEngineConfiguration;
import org.activiti.spring.boot.AbstractProcessEngineAutoConfiguration;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.transaction.PlatformTransactionManager;

import javax.sql.DataSource;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * @author zxb 2023/1/10 11:18
 */
@Configuration
public class ActivitiConfig extends AbstractProcessEngineAutoConfiguration {
    @Bean
    @Primary
    @ConfigurationProperties(prefix = "spring.datasource")
    public DataSource activitiDataSource() {
        return DataSourceBuilder.create().build();
    }

    @Bean
    public SpringProcessEngineConfiguration processEngineConfiguration(DataSource dataSource, PlatformTransactionManager transactionManager) {
        SpringProcessEngineConfiguration configuration = new SpringProcessEngineConfiguration();
        configuration.setDataSource(dataSource);
        configuration.setTransactionManager(transactionManager);
        // 添加全局监听器
        List<ActivitiEventListener> eventListeners = new ArrayList<>();
        eventListeners.add(activitiGlobalListener());
        configuration.setEventListeners(eventListeners);
        return configuration;
    }


    @Bean
    public ActivitiGlobalListener activitiGlobalListener(){
        return new ActivitiGlobalListener();
    }
}
