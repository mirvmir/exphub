package io.github.mirvmir.common.properties;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

import java.time.Clock;

@ComponentScan(basePackageClasses = CommonModuleConfig.class)
@Configuration
public class CommonModuleConfig {

    @Bean
    public Clock clock() {
        return Clock.systemUTC();
    }
}