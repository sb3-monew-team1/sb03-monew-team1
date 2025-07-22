package com.sprint.mission.sb03monewteam1.event.listener;

import com.mongodb.client.result.UpdateResult;
import com.sprint.mission.sb03monewteam1.document.ArticleViewActivity;
import com.sprint.mission.sb03monewteam1.dto.ArticleViewActivityDto;
import com.sprint.mission.sb03monewteam1.event.ArticleViewActivityCreateEvent;
import com.sprint.mission.sb03monewteam1.event.ArticleViewActivityBulkDeleteEvent;
import com.sprint.mission.sb03monewteam1.event.ArticleViewCountUpdateEvent;
import com.sprint.mission.sb03monewteam1.repository.mongodb.ArticleViewActivityRepository;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
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

    private final MongoTemplate mongoTemplate;

    private final ArticleViewActivityRepository repository;

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
        log.debug("기사 뷰 활동 ArticleViewActivityCreateEvent 리스너 실행: {}", event);
        saveUserActivity(event.userId(), event.articleViewActivityDto());
    }

    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleBulkDeleteEvent(ArticleViewActivityBulkDeleteEvent event) {
        UUID articleId = event.articleId();
        log.debug("ArticleViewActivityBulkDeleteEvent 리스너 실행: articleId={}", articleId);

        List<ArticleViewActivity> allDocs = repository.findAllByArticleViews_articleId(articleId);

        for (ArticleViewActivity doc : allDocs) {
            deleteUserActivity(doc.getUserId(), articleId);
        }
    }

    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleViewCountChanged(ArticleViewCountUpdateEvent event) {
        UUID articleId = event.articleId();
        long newViewCount = event.newViewCount();

        log.debug("ArticleViewCountChangedEvent 리스너 실행: articleId={}, newViewCount={}", articleId, newViewCount);

        Query query = new Query(Criteria.where("articleViews.articleId").is(articleId));
        Update update = new Update().set("articleViews.$.articleViewCount", newViewCount);

        UpdateResult result = mongoTemplate.updateMulti(query, update, ArticleViewActivity.class);

        log.info("기사 뷰 수 동기화 완료: articleId={}, 반영된 문서 수={}", articleId, result.getModifiedCount());
    }
}
