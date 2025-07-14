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
    private NaverNewsCollector naverNewsCollector;

    @Autowired
    private HankyungNewsCollector hankyungNewsCollector;

    @Test
    void Naver_API로_뉴스_데이터를_수집할_수_있다() {
        // given
        String keyword = "감자";
        log.info("테스트 시작: Naver API 뉴스 수집, 키워드: {}", keyword);

        // when
        var result = naverNewsCollector.collect(null, keyword);

        // then
        assertThat(result).isNotNull();
        assertThat(result).isNotEmpty();
        log.info("테스트 성공: Naver API 응답 길이 = {}", result.size());
        log.debug("Naver API 응답 내용: {}", result);
    }

    @Test
    void 한국경제_RSS로_뉴스_데이터를_수집할_수_있다() {
        log.info("테스트 시작: 한국경제 RSS 뉴스 수집");
        var result = hankyungNewsCollector.collect(null, null);
        assertThat(result).isNotNull();
        assertThat(result).isNotEmpty();
        log.info("테스트 성공: 한국경제 RSS 기사 개수 = {}", result.size());
        log.debug("첫 번째 기사: {}", result.get(0));
    }
}