package com.sprint.mission.sb03monewteam1.seeder;

import com.sprint.mission.sb03monewteam1.entity.Interest;
import com.sprint.mission.sb03monewteam1.entity.Subscription;
import com.sprint.mission.sb03monewteam1.entity.User;
import com.sprint.mission.sb03monewteam1.repository.jpa.SubscriptionRepository;
import com.sprint.mission.sb03monewteam1.repository.jpa.UserRepository;
import com.sprint.mission.sb03monewteam1.repository.jpa.InterestRepository;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Random;

@Slf4j
@Component
@Profile({"dev", "postgres"})
@RequiredArgsConstructor
public class SubscriptionDataSeeder implements DataSeeder {

    private final SubscriptionRepository subscriptionRepository;
    private final UserRepository userRepository;
    private final InterestRepository interestRepository;

    @Override
    @Transactional
    public void seed() {
        if (subscriptionRepository.count() > 0) {
            log.info("SubscriptionDataSeeder: 구독 데이터가 이미 존재하여 시드를 실행하지 않습니다.");
            return;
        }

        List<User> users = userRepository.findAll();
        List<Interest> interests = interestRepository.findAll();

        if (users.isEmpty() || interests.isEmpty()) {
            log.warn("SubscriptionDataSeeder: 사용자가 없거나 관심사가 없습니다.");
            return;
        }

        List<Subscription> subscriptions = createSubscriptions(users, interests);

        subscriptionRepository.saveAll(subscriptions);

        log.info("SubscriptionDataSeeder: 총 {}개의 구독 데이터가 추가되었습니다.", subscriptions.size());
    }

    private List<Subscription> createSubscriptions(List<User> users, List<Interest> interests) {
        Random random = new Random();
        List<Subscription> subscriptions = new ArrayList<>();
        Set<String> createdPairs = new HashSet<>();

        for (User user : users) {
            int subscriptionCount = 40;
            for (int i = 0; i < subscriptionCount; i++) {
                Interest randomInterest = interests.get(random.nextInt(interests.size()));
                String key = user.getId() + ":" + randomInterest.getId();
                if (!createdPairs.add(key)) {
                    continue;
                }
                Subscription subscription = new Subscription(randomInterest, user);
                subscriptions.add(subscription);
            }
        }

        return subscriptions;
    }
}
