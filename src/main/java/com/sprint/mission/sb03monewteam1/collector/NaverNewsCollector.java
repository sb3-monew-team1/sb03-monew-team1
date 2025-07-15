package com.sprint.mission.sb03monewteam1.collector;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sprint.mission.sb03monewteam1.dto.CollectedArticleDto;
import com.sprint.mission.sb03monewteam1.entity.Interest;
import com.sprint.mission.sb03monewteam1.exception.article.ArticleCollectException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

@Slf4j
@Component
@RequiredArgsConstructor
public class NaverNewsCollector {

    private final WebClient naverApiWebClient;
    private final ObjectMapper objectMapper;

    @Value("${news.api.naver.client-id}")
    private String naverClientId;

    @Value("${news.api.naver.client-secret}")
    private String naverClientSecret;

    @Value("${news.api.naver.source-name:NAVER}")
    private String sourceName;

    public List<CollectedArticleDto> collect(Interest interest, String keyword) {
        try {
            String query = URLEncoder.encode(keyword, StandardCharsets.UTF_8);
            String response = naverApiWebClient
                .get()
                .uri(uriBuilder -> uriBuilder
                    .path("/v1/search/news.json")
                    .queryParam("query", query)
                    .queryParam("display", 10)
                    .queryParam("start", 1)
                    .queryParam("sim", "sim")
                    .build())
                .header("X-Naver-Client-Id", naverClientId)
                .header("X-Naver-Client-Secret", naverClientSecret)
                .retrieve()
                .bodyToMono(String.class)
                .block();

            return parseArticles(response);
        } catch (Exception e) {
            log.error("Naver API 수집/파싱 실패: {}", e.getMessage(), e);
            throw new ArticleCollectException("Naver API 수집/파싱 실패: " + e.getMessage());
        }
    }

    private List<CollectedArticleDto> parseArticles(String json) throws Exception {
        List<CollectedArticleDto> result = new ArrayList<>();
        JsonNode root = objectMapper.readTree(json);
        JsonNode items = root.get("items");
        if (items == null || !items.isArray()) {
            return result;
        }
        for (JsonNode item : items) {
            String title = item.has("title") ? item.get("title").asText() : "";
            String link = item.has("link") ? item.get("link").asText() : "";
            String pubDateStr = item.has("pubDate") ? item.get("pubDate").asText() : null;
            Instant pubDate = null;

            if (pubDateStr != null) {
                try {
                    pubDate = Instant.from(DateTimeFormatter.RFC_1123_DATE_TIME.parse(pubDateStr));
                } catch (DateTimeParseException e) {
                    log.warn("날짜 파싱 실패: {}", pubDateStr);
                    pubDate = null;
                }
            }
            String summary = item.has("description") ? item.get("description").asText() : title;

            result.add(CollectedArticleDto.builder()
                .title(title)
                .sourceUrl(link)
                .source(sourceName)
                .publishDate(pubDate)
                .summary(summary)
                .rawContent(null)
                .imageUrl(null)
                .build());
        }
        return result;
    }
}
