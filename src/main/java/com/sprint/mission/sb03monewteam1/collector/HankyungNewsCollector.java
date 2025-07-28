package com.sprint.mission.sb03monewteam1.collector;

import com.rometools.rome.feed.synd.SyndFeed;
import com.rometools.rome.io.SyndFeedInput;
import com.rometools.rome.io.XmlReader;
import com.sprint.mission.sb03monewteam1.dto.CollectedArticleDto;
import com.sprint.mission.sb03monewteam1.exception.article.ArticleCollectException;
import dev.failsafe.Failsafe;
import dev.failsafe.RateLimiter;
import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

@Slf4j
@Component
@RequiredArgsConstructor
public class HankyungNewsCollector {

    private final WebClient generalWebClient;

    private final RateLimiter<Object> rateLimiter =
        RateLimiter.smoothBuilder(5, Duration.ofSeconds(1))
            .withMaxWaitTime(Duration.ofSeconds(1)).build();

    @Value("${news.api.hankyung.url}")
    private String hankyungRssUrl;

    @Value("${news.api.hankyung.source-name:HANKYUNG}")
    private String sourceName;

    public List<CollectedArticleDto> collect(String keyword) {
        return Failsafe.with(rateLimiter).get(() -> {
            try {
                String rawXml = generalWebClient.get()
                    .uri(hankyungRssUrl)
                    .retrieve()
                    .bodyToMono(String.class)
                    .timeout(Duration.ofSeconds(30))
                    .block();

                String cleanedXml = rawXml.replaceAll("<!DOCTYPE[^>]*>", "");

                SyndFeedInput input = new SyndFeedInput();
                input.setPreserveWireFeed(true);

                try (ByteArrayInputStream inputStream = new ByteArrayInputStream(
                    cleanedXml.getBytes(StandardCharsets.UTF_8))) {
                    SyndFeed feed = input.build(new XmlReader(inputStream));
                    return feed.getEntries().stream().map(entry -> {
                        String title = entry.getTitle();
                        String link = entry.getLink();
                        Instant pubDate = entry.getPublishedDate() != null
                            ? entry.getPublishedDate().toInstant()
                            : null;
                        String summary = title;

                        return CollectedArticleDto.builder()
                            .title(title)
                            .sourceUrl(link)
                            .source(sourceName)
                            .publishDate(pubDate)
                            .summary(summary)
                            .rawContent(null)
                            .imageUrl(null)
                            .build();
                    }).collect(Collectors.toList());
                }
            } catch (Exception e) {
                log.error("한국경제 RSS 수집/파싱 실패: {}", e.getMessage(), e);
                throw new ArticleCollectException("한국경제 RSS 수집/파싱 실패: " + e.getMessage());
            }
        });
    }
}
