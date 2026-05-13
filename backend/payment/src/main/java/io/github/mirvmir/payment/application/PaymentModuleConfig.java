package io.github.mirvmir.payment.application;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

@Configuration
@ComponentScan(basePackageClasses = PaymentModuleConfig.class)
public class PaymentModuleConfig {

    @Bean
    public RestTemplate restTemplate() {
        return new RestTemplate();
    }
}
