package com.sprint.mission.sb03monewteam1.seeder;

import com.sprint.mission.sb03monewteam1.entity.Comment;
import com.sprint.mission.sb03monewteam1.entity.CommentLike;
import com.sprint.mission.sb03monewteam1.entity.User;
import com.sprint.mission.sb03monewteam1.repository.jpa.CommentLikeRepository;
import com.sprint.mission.sb03monewteam1.repository.jpa.CommentRepository;
import com.sprint.mission.sb03monewteam1.repository.jpa.UserRepository;
import java.util.Collections;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@Profile({"dev", "postgres"})
@RequiredArgsConstructor
public class CommentLikeDataSeeder implements DataSeeder {

    private final CommentRepository commentRepository;
    private final CommentLikeRepository commentLikeRepository;
    private final UserRepository userRepository;

    @Override
    public void seed() {
        List<Comment> comments = commentRepository.findAll();
        List<User> users = userRepository.findAll();

        for (Comment comment : comments) {
            // 유저 목록 섞기
            Collections.shuffle(users);

            // 1~3명 유저 선택
            int likeCount = Math.min((int) (Math.random() * 3) + 1, users.size());
            List<User> selectedUsers = users.subList(0, likeCount);

            for (User user : selectedUsers) {
                if (commentLikeRepository.existsByCommentIdAndUserId(comment.getId(), user.getId())) {
                    continue;
                }
                CommentLike like = createCommentLike(user, comment);
                comment.increaseLikeCount(); // 좋아요 수 증가
                commentLikeRepository.save(like);
            }
        }

        commentRepository.saveAll(comments);
        log.info("댓글 좋아요 시드 데이터 생성 완료 - 총 댓글 수: {}", comments.size());
    }

    private CommentLike createCommentLike(User user, Comment comment) {
        return CommentLike.builder()
            .user(user)
            .comment(comment)
            .build();
    }
}
