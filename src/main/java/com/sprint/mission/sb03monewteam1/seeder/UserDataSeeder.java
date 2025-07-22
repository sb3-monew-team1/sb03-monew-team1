package com.sprint.mission.sb03monewteam1.seeder;

import com.sprint.mission.sb03monewteam1.entity.User;
import com.sprint.mission.sb03monewteam1.repository.jpa.user.UserRepository;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Profile;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Component
@Profile({"dev", "postgres"})
@RequiredArgsConstructor
public class UserDataSeeder implements DataSeeder {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    private static final String DEFAULT_PASSWORD = "!qwe1234";

    @Override
    @Transactional
    public void seed() {
        if (userRepository.count() > 0) {
            log.info("UserDataSeeder: 사용자가 이미 존재하여 시드를 실행하지 않습니다.");
            return;
        }

        String encodedPassword = passwordEncoder.encode(DEFAULT_PASSWORD);

        List<User> users = List.of(
            createUser("user1@example.com", "유저1", encodedPassword),
            createUser("user2@example.com", "유저2", encodedPassword),
            createUser("user3@example.com", "유저3", encodedPassword),
            createUser("user4@example.com", "유저4", encodedPassword),
            createUser("user5@example.com", "유저5", encodedPassword)
        );

        userRepository.saveAll(users);
        log.info("UserDataSeeder: 총 {}명의 사용자 시드 데이터가 추가되었습니다.", users.size());
    }


    private User createUser(String email, String nickname, String password) {
        return User.builder()
            .email(email)
            .nickname(nickname)
            .password(password)
            .build();
    }
}
