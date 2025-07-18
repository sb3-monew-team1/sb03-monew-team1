package com.sprint.mission.sb03monewteam1.seeder;

import com.sprint.mission.sb03monewteam1.entity.Article;
import com.sprint.mission.sb03monewteam1.entity.Comment;
import com.sprint.mission.sb03monewteam1.entity.User;
import com.sprint.mission.sb03monewteam1.repository.ArticleRepository;
import com.sprint.mission.sb03monewteam1.repository.CommentRepository;
import com.sprint.mission.sb03monewteam1.repository.UserRepository;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@Profile({"dev", "postgres"})
@RequiredArgsConstructor
public class CommentDataSeeder implements DataSeeder{

    private final CommentRepository commentRepository;
    private final UserRepository userRepository;
    private final ArticleRepository articleRepository;

    @Override
    public void seed() {

        if (commentRepository.count() > 0) {
            log.info("CommentDataSeeder: 댓글이 이미 존재하여 시드를 실행하지 않습니다.");
            return;
        }

        User savedUser = userRepository.findByEmail("user1@example.com")
            .orElseThrow(() -> new IllegalStateException("User not found in seeder"));

        List<Article> articles = articleRepository.findAll();

        for (Article article : articles) {
            for (int i = 1; i <= 10; i++) {
                Comment comment = createComment("[" + article.getTitle() + "] 댓글 " + i, savedUser, article);
                article.increaseCommentCount(); // 댓글 수 증가
                commentRepository.save(comment);
            }
        }

        articleRepository.saveAll(articles);
        log.info("샘플 댓글 10개 생성 완료");
    }

    private Comment createComment(String content, User user, Article article) {
        return Comment.builder()
            .content(content)
            .author(user)
            .article(article)
            .build();
    }
}
