package com.sprint.mission.sb03monewteam1.seeder;

import com.sprint.mission.sb03monewteam1.entity.Interest;
import com.sprint.mission.sb03monewteam1.entity.InterestKeyword;
import com.sprint.mission.sb03monewteam1.repository.InterestRepository;
import com.sprint.mission.sb03monewteam1.repository.InterestKeywordRepository;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Component
@Profile("dev")
@RequiredArgsConstructor
public class InterestDataSeeder implements DataSeeder {

    private final InterestRepository interestRepository;
    private final InterestKeywordRepository interestKeywordRepository;

    @Override
    @Transactional
    public void seed() {
        if (interestRepository.count() > 0) {
            log.info("InterestDataSeeder: 관심사가 이미 존재하여 시드를 실행하지 않습니다.");
            return;
        }

        List<Interest> interests = List.of(
            createInterest("Football", 150),
            createInterest("Soccer", 100),
            createInterest("Basketball", 80),
            createInterest("Baseball", 120),
            createInterest("Tennis", 50)
        );

        interestRepository.saveAll(interests);

        for (Interest interest : interests) {
            createInterestKeywords(interest);
        }

        log.info("InterestDataSeeder: 총 {}개의 관심사와 관련된 키워드 시드 데이터가 추가되었습니다.", interests.size());
    }

    private Interest createInterest(String name, long subscriberCount) {
        return Interest.builder()
            .name(name)
            .subscriberCount(subscriberCount)
            .build();
    }

    private void createInterestKeywords(Interest interest) {
        List<String> keywords = getKeywordsForInterest(interest.getName());
        for (String keyword : keywords) {
            InterestKeyword interestKeyword = InterestKeyword.builder()
                .keyword(keyword)
                .interest(interest)
                .build();
            interestKeywordRepository.save(interestKeyword);
        }
    }

    private List<String> getKeywordsForInterest(String interestName) {
        switch (interestName) {
            case "Football":
                return List.of("club", "sport", "ball");
            case "Soccer":
                return List.of("ball", "outdoor");
            case "Basketball":
                return List.of("court", "game");
            case "Baseball":
                return List.of("bat", "outdoor");
            case "Tennis":
                return List.of("court", "racket");
            default:
                return List.of("sports");
        }
    }
}
