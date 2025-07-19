package com.sprint.mission.sb03monewteam1.event.listener;

import com.sprint.mission.sb03monewteam1.document.ArticleViewActivity;
import com.sprint.mission.sb03monewteam1.dto.ArticleViewActivityDto;
import com.sprint.mission.sb03monewteam1.event.ArticleViewActivityCreateEvent;
import com.sprint.mission.sb03monewteam1.repository.mongodb.ArticleViewActivityRepository;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Slf4j
@Component
@RequiredArgsConstructor
public class ArticleViewActivityEventListener extends AbstractActivityEventListener<
    ArticleViewActivityDto,
    ArticleViewActivity,
    ArticleViewActivityRepository> {

    private final ArticleViewActivityRepository repository;

    @Override
    protected UUID getUserId(ArticleViewActivityDto dto) {
        return dto.viewedBy();
    }

    @Override
    protected UUID getActivityId(ArticleViewActivityDto dto) {
        return dto.articleId();
    }

    @Override
    protected ArticleViewActivityRepository getRepository() {
        return repository;
    }

    @Override
    protected List<ArticleViewActivityDto> getActivityList(ArticleViewActivity document) {
        return document.getArticleViews();
    }

    @Override
    protected ArticleViewActivity createNewDocument(UUID userId) {
        ArticleViewActivity activity = new ArticleViewActivity();
        activity.setUserId(userId);
        activity.setArticleViews(new ArrayList<>());
        return activity;
    }

    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleCreateEvent(ArticleViewActivityCreateEvent event) {
        log.debug("ArticleViewActivityCreateEvent 리스너 실행: {}", event);
        saveUserActivity(event.articleViewActivityDto());
    }
}
