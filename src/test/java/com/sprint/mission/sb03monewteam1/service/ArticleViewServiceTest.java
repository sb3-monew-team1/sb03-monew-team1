package com.sprint.mission.sb03monewteam1.service;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.sprint.mission.sb03monewteam1.exception.SomeException;
import com.sprint.mission.sb03monewteam1.fixture.ArticleFixture;
import com.sprint.mission.sb03monewteam1.repository.ArticleRepository;
import com.sprint.mission.sb03monewteam1.repository.ArticleViewRepository;

@ExtendWith(MockitoExtension.class)
class ArticleViewServiceTest {

    @Mock
    private ArticleViewRepository articleViewRepository;

    @Mock
    private ArticleRepository articleRepository;

    @InjectMocks
    private ArticleViewServiceImpl articleViewService; // 아직 존재하지 않음 - 실패할 것

    @Test
    void 기사_뷰_등록_성공() {
        // given
        UUID userId = UUID.randomUUID();
        UUID articleId = UUID.randomUUID();
        Article article = ArticleFixture.createArticleWithId(articleId);

        when(articleRepository.findByIdAndIsDeletedFalse(articleId))
                .thenReturn(Optional.of(article));
        when(articleViewRepository.existsByUserIdAndArticleId(userId, articleId))
                .thenReturn(false);

        // when & then
        assertThatThrownBy(() -> articleViewService.createArticleView(userId, articleId))
                .isInstanceOf(SomeException.class); // 서비스가 없으므로 컴파일 에러 발생
    }
}
