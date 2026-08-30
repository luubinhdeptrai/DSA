package com.example.springpractice.config;

import org.springframework.context.annotation.Configuration;

import java.time.Clock;

import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.PropertySource;
import org.springframework.context.annotation.Scope;
import org.springframework.context.support.PropertySourcesPlaceholderConfigurer;

import org.springframework.beans.factory.config.ConfigurableBeanFactory;


@Configuration
@PropertySource("classpath:application.properties")
@ComponentScan("com.example.springpractice")
public class AppConfig {

    @Bean
    public Clock applicationClock()
    {
        return Clock.systemUTC();
    }

    @Bean
    public static PropertySourcesPlaceholderConfigurer
            propertySourcesPlaceholderConfigurer() {
        // TODO
        return new PropertySourcesPlaceholderConfigurer();
    }

    @Bean
    @Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
    public StringBuilder notificationDraft() {
        return new StringBuilder();
    }
    
}
