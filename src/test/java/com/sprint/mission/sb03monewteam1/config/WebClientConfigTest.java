package com.sprint.mission.sb03monewteam1.config;

import static org.assertj.core.api.Assertions.*;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;
import org.springframework.web.reactive.function.client.WebClient;

@SpringBootTest
@TestPropertySource(properties = { "news.api.naver.base-url=https://openapi.naver.com" })
class WebClientConfigTest {

    @Autowired
    private WebClient naverApiWebClient;

    @Autowired
    private WebClient generalWebClient;

    @Test
    void naverApiWebClient_빈이_정상적으로_생성되어야_한다() {
        // given & when & then
        assertThat(naverApiWebClient).isNotNull();
        assertThat(naverApiWebClient.toString()).contains("https://openapi.naver.com");
    }

    @Test
    void generalWebClient_빈이_정상적으로_생성되어야_한다() {
        // given & when & then
        assertThat(generalWebClient).isNotNull();
    }

    @Test
    void 각_WebClient는_서로_다른_인스턴스여야_한다() {
        // given & when & then
        assertThat(naverApiWebClient).isNotSameAs(generalWebClient);
    }
}
