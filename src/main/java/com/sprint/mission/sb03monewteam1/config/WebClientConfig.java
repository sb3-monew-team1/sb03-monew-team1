package com.sprint.mission.sb03monewteam1.config;

import java.time.Duration;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.netty.http.client.HttpClient;
import reactor.netty.resources.ConnectionProvider;

@Configuration
public class WebClientConfig {

    @Value("${news.api.naver.base-url:https://openapi.naver.com}")
    private String naverApiBaseUrl;

    @Bean
    public WebClient naverApiWebClient() {
        ConnectionProvider connectionProvider = ConnectionProvider.builder("naver-api")
            .maxConnections(100)
            .maxIdleTime(Duration.ofSeconds(20))
            .maxLifeTime(Duration.ofSeconds(60))
            .pendingAcquireTimeout(Duration.ofSeconds(60))
            .evictInBackground(Duration.ofSeconds(120))
            .build();

        HttpClient httpClient = HttpClient.create(connectionProvider)
            .responseTimeout(Duration.ofSeconds(30));

        return WebClient.builder()
            .baseUrl(naverApiBaseUrl)
            .clientConnector(new ReactorClientHttpConnector(httpClient))
            .build();
    }

    @Bean
    public WebClient generalWebClient() {
        ConnectionProvider connectionProvider = ConnectionProvider.builder("general")
            .maxConnections(50)
            .maxIdleTime(Duration.ofSeconds(20))
            .maxLifeTime(Duration.ofSeconds(60))
            .pendingAcquireTimeout(Duration.ofSeconds(60))
            .evictInBackground(Duration.ofSeconds(120))
            .build();

        HttpClient httpClient = HttpClient.create(connectionProvider)
            .responseTimeout(Duration.ofSeconds(30));

        return WebClient.builder()
            .clientConnector(new ReactorClientHttpConnector(httpClient))
            .build();
    }
}
