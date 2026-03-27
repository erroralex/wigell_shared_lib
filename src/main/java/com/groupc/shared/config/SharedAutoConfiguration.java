package com.groupc.shared.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import com.groupc.shared.client.CurrencyClient;
import com.groupc.shared.exception.GlobalExceptionHandler;

@Configuration
public class SharedAutoConfiguration {

    @Bean
    public GlobalExceptionHandler globalExceptionHandler() {
        return new GlobalExceptionHandler();
    }

    @Bean
    public CurrencyClient currencyClient(@Value("${wigell.currency.url:http://localhost:8580}") String url) {
        return new CurrencyClient(url);
    }
}
