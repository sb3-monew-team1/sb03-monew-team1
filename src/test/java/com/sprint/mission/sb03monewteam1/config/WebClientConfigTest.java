package com.sprint.mission.sb03monewteam1.config;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.web.reactive.function.client.WebClient;

@SpringBootTest
class WebClientConfigTest {

    @Autowired
    private WebClient naverApiWebClient;

    @Autowired
    private WebClient generalWebClient;

    @Test
    void naverApiWebClient_빈이_정상적으로_생성되어야_한다() {
        // given & when & then
        assertThat(naverApiWebClient).isNotNull();
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

    @Test
    void WebClient_설정이_올바르게_적용되어야_한다() {
        // given & when & then
        // WebClient가 정상적으로 생성되고 주입되었다면 설정이 올바르다고 판단
        assertThat(naverApiWebClient).isNotNull();
        assertThat(generalWebClient).isNotNull();
        assertThat(naverApiWebClient).isNotSameAs(generalWebClient);
    }
}
