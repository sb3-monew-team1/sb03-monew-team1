package com.sprint.mission.sb03monewteam1.scheduler;

import com.sprint.mission.sb03monewteam1.collector.HankyungNewsCollector;
import com.sprint.mission.sb03monewteam1.collector.NaverNewsCollector;
import com.sprint.mission.sb03monewteam1.entity.Interest;
import com.sprint.mission.sb03monewteam1.entity.InterestKeyword;
import com.sprint.mission.sb03monewteam1.repository.InterestRepository;
import com.sprint.mission.sb03monewteam1.service.ArticleService;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ArticleBatchScheduler {

    private final InterestRepository interestRepository;
    private final NaverNewsCollector naverNewsCollector;
    private final HankyungNewsCollector hankyungNewsCollector;
    private final ArticleService articleService;

    @Scheduled(cron = "0 0 * * * *")
    public void collectNaverNews() {
        List<Interest> interests = interestRepository.findAllWithKeywords();
        for (Interest interest : interests) {
            for (InterestKeyword interestKeyword : interest.getKeywords()) {
                String keyword = interestKeyword.getKeyword();
                articleService.collectAndSaveNaverArticles(interest, keyword);
            }
        }
    }

    @Scheduled(cron = "0 0 2 * * *")
    public void collectHankyungNews() {
        List<Interest> interests = interestRepository.findAllWithKeywords();
        for (Interest interest : interests) {
            for (InterestKeyword interestKeyword : interest.getKeywords()) {
                String keyword = interestKeyword.getKeyword();
                articleService.collectAndSaveHankyungArticles(interest, keyword);
            }
        }
    }

    public void collectNaverNews(Interest interest, String keyword) {
        naverNewsCollector.collect(interest, keyword);
    }

    public void collectHankyungNews(Interest interest, String keyword) {
        hankyungNewsCollector.collect(interest, keyword);
    }
}
