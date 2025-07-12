package com.sprint.mission.sb03monewteam1.collector;

import static org.assertj.core.api.Assertions.assertThat;

import com.sprint.mission.sb03monewteam1.config.LoadTestEnv;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@Slf4j
@LoadTestEnv
@SpringBootTest
class NewsCollectorTest {

    @Autowired
    private NewsCollector newsCollector;

    @Test
    void Naver_API로_뉴스_데이터를_수집할_수_있다() {
        // given
        String keyword = "감자";
        log.info("테스트 시작: Naver API 뉴스 수집, 키워드: {}", keyword);

        // when
        String result = newsCollector.collectFromNaverApi(keyword);

        // then
        assertThat(result).isNotNull();
        assertThat(result).isNotEmpty();
        log.info("테스트 성공: Naver API 응답 길이 = {}", result.length());
        log.debug("Naver API 응답 내용: {}", result);
    }

    @Test
    void 한국경제_RSS로_뉴스_데이터를_수집할_수_있다() {
        // given
        String rssUrl = System.getProperty("NEWS_RSS_HANKYUNG_URL");
        log.info("테스트 시작: 한국경제 RSS 뉴스 수집, URL: {}", rssUrl);

        // when
        String result = newsCollector.collectFromRss(rssUrl);

        // then
        assertThat(result).isNotNull();
        assertThat(result).isNotEmpty();
        log.info("테스트 성공: 한국경제 RSS 응답 길이 = {}", result.length());
        log.debug("RSS 응답 내용 (첫 200자): {}", result.substring(0, Math.min(200, result.length())));
    }

    @Test
    void 조선일보_RSS로_뉴스_데이터를_수집할_수_있다() {
        // given
        String rssUrl = System.getProperty("NEWS_RSS_CHOSUN_URL");
        log.info("테스트 시작: 조선일보 RSS 뉴스 수집, URL: {}", rssUrl);

        // when
        String result = newsCollector.collectFromRss(rssUrl);

        // then
        assertThat(result).isNotNull();
        assertThat(result).isNotEmpty();
        log.info("테스트 성공: 조선일보 RSS 응답 길이 = {}", result.length());
        log.debug("RSS 응답 내용 (첫 200자): {}", result.substring(0, Math.min(200, result.length())));
    }

    @Test
    void 연합뉴스_RSS로_뉴스_데이터를_수집할_수_있다() {
        // given
        String rssUrl = System.getProperty("NEWS_RSS_YONHAP_URL");
        log.info("테스트 시작: 연합뉴스 RSS 뉴스 수집, URL: {}", rssUrl);

        // when
        String result = newsCollector.collectFromRss(rssUrl);

        // then
        assertThat(result).isNotNull();
        assertThat(result).isNotEmpty();
        log.info("테스트 성공: 연합뉴스 RSS 응답 길이 = {}", result.length());
        log.debug("RSS 응답 내용 (첫 200자): {}", result.substring(0, Math.min(200, result.length())));
    }
}