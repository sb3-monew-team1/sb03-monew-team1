package com.sprint.mission.sb03monewteam1.collector;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@RequiredArgsConstructor
public class NewsCollector {

    private final WebClient naverApiWebClient;
    private final WebClient generalWebClient;

    @Value("${news.api.naver.client-id}")
    private String naverClientId;

    @Value("${news.api.naver.client-secret}")
    private String naverClientSecret;

    public String collectFromNaverApi(String keyword) {
        try {
            return naverApiWebClient
                    .get()
                    .uri(uriBuilder -> uriBuilder
                            .path("/v1/search/news.json")
                            .queryParam("query", keyword)
                            .queryParam("display", 10)
                            .queryParam("start", 1)
                            .build())
                    .header("X-Naver-Client-Id", naverClientId)
                    .header("X-Naver-Client-Secret", naverClientSecret)
                    .retrieve()
                    .bodyToMono(String.class)
                    .block();
        } catch (Exception e) {
            log.error("Naver API 호출 실패: {}", e.getMessage());
            return "";
        }
    }

    public String collectFromRss(String rssUrl) {
        try {
            return generalWebClient
                    .get()
                    .uri(rssUrl)
                    .retrieve()
                    .bodyToMono(String.class)
                    .block();
        } catch (Exception e) {
            log.error("RSS 수집 실패: {}", e.getMessage());
            return "";
        }
    }
}
