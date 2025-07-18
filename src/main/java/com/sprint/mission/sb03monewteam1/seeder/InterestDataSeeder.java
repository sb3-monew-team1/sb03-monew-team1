package com.sprint.mission.sb03monewteam1.seeder;

import com.sprint.mission.sb03monewteam1.entity.Interest;
import com.sprint.mission.sb03monewteam1.entity.InterestKeyword;
import com.sprint.mission.sb03monewteam1.repository.jpa.InterestRepository;
import com.sprint.mission.sb03monewteam1.repository.jpa.InterestKeywordRepository;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Component
@Profile({"dev", "postgres"})
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

        List<Interest> interests = createInterests();
        List<InterestKeyword> interestKeywords = createInterestKeywords(interests);

        interestRepository.saveAll(interests);
        interestKeywordRepository.saveAll(interestKeywords);

        log.info("InterestDataSeeder: 총 {}개의 관심사와 관련된 키워드 시드 데이터가 추가되었습니다.", interests.size());
    }

    private List<Interest> createInterests() {
        List<String> names = List.of("Football", "Soccer", "Basketball", "Baseball", "Tennis", "Hockey", "Golf", "Cricket", "Rugby", "Badminton");
        List<Interest> interests = new ArrayList<>();

        for (int i = 0; i <= 70; i++) {
            String name = names.get(i % names.size()) + " " + i;
            int subscriberCount = 1 + (i % 10);
            interests.add(createInterest(name, subscriberCount));
        }
        return interests;
    }


    private Interest createInterest(String name, long subscriberCount) {
        return Interest.builder()
            .name(name)
            .subscriberCount(subscriberCount)
            .build();
    }

    private List<InterestKeyword> createInterestKeywords(List<Interest> interests) {
        List<InterestKeyword> interestKeywords = new ArrayList<>();
        for (Interest interest : interests) {
            List<String> keywords = generateRandomKeywords(interest.getName(), 3);
            for (String keyword : keywords) {
                interestKeywords.add(InterestKeyword.builder()
                    .interest(interest)
                    .keyword(keyword)
                    .build());
            }
        }
        return interestKeywords;
    }

    private List<String> generateRandomKeywords(String interestName, int count) {
        List<String> adjectives = List.of("popular", "exciting", "challenging", "relaxing", "creative", "social", "active", "peaceful");
        List<String> descriptors = List.of("community", "beginner", "advanced", "fun", "serious", "casual", "professional");

        List<String> keywords = new ArrayList<>();
        keywords.add(interestName.toLowerCase());

        Random random = new Random();
        while (keywords.size() < count) {
            if (random.nextBoolean()) {
                keywords.add(adjectives.get(random.nextInt(adjectives.size())));
            } else {
                keywords.add(descriptors.get(random.nextInt(descriptors.size())));
            }
        }

        return keywords;
    }
}
