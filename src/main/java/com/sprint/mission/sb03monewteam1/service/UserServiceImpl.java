package com.sprint.mission.sb03monewteam1.service;

import at.favre.lib.crypto.bcrypt.BCrypt;
import com.sprint.mission.sb03monewteam1.dto.UserDto;
import com.sprint.mission.sb03monewteam1.dto.request.UserLoginRequest;
import com.sprint.mission.sb03monewteam1.dto.request.UserRegisterRequest;
import com.sprint.mission.sb03monewteam1.dto.request.UserUpdateRequest;
import com.sprint.mission.sb03monewteam1.entity.Comment;
import com.sprint.mission.sb03monewteam1.entity.CommentLike;
import com.sprint.mission.sb03monewteam1.entity.Subscription;
import com.sprint.mission.sb03monewteam1.entity.User;
import com.sprint.mission.sb03monewteam1.exception.user.EmailAlreadyExistsException;
import com.sprint.mission.sb03monewteam1.exception.user.ForbiddenAccessException;
import com.sprint.mission.sb03monewteam1.exception.user.InvalidEmailOrPasswordException;
import com.sprint.mission.sb03monewteam1.exception.user.UserNotFoundException;
import com.sprint.mission.sb03monewteam1.mapper.UserMapper;
import com.sprint.mission.sb03monewteam1.repository.jpa.comment.CommentRepository;
import com.sprint.mission.sb03monewteam1.repository.jpa.commentLike.CommentLikeRepository;
import com.sprint.mission.sb03monewteam1.repository.jpa.interest.InterestRepository;
import com.sprint.mission.sb03monewteam1.repository.jpa.subscription.SubscriptionRepository;
import com.sprint.mission.sb03monewteam1.repository.jpa.user.UserRepository;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@RequiredArgsConstructor
@Service
@Transactional
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final SubscriptionRepository subscriptionRepository;
    private final InterestRepository interestRepository;
    private final CommentLikeRepository commentLikeRepository;
    private final CommentRepository commentRepository;
    private final UserMapper userMapper;

    @Override
    public UserDto createUser(UserRegisterRequest userRegisterRequest) {

        log.info("사용자 생성 시작: email={}, nickname={}", userRegisterRequest.email(),
            userRegisterRequest.nickname());

        String email = userRegisterRequest.email();
        String nickname = userRegisterRequest.nickname();
        String rawPassword = userRegisterRequest.password();

        if (userRepository.existsByEmail(email)) {
            log.warn("중복된 이메일로 회원가입 시도: email={}", email);
            throw new EmailAlreadyExistsException(email);
        }

        String encodedPassword = BCrypt.withDefaults().hashToString(12, rawPassword.toCharArray());

        User user = User.builder()
            .email(email)
            .nickname(nickname)
            .password(encodedPassword)
            .build();

        User savedUser = userRepository.save(user);

        log.info("사용자 생성 완료: id={}, email={}, nickname={}",
            savedUser.getId(), savedUser.getEmail(), savedUser.getNickname());

        return userMapper.toDto(savedUser);
    }

    @Override
    public UserDto login(UserLoginRequest userLoginRequest) {

        String email = userLoginRequest.email();
        String rawPassword = userLoginRequest.password();

        log.info("로그인 인증 시작 - email={}", email);

        User user = userRepository.findByEmail(email)
            .orElseThrow(
                () -> {
                    log.warn("로그인 실패 - 존재하지 않는 이메일: {}", email);
                    return new InvalidEmailOrPasswordException(email);
                });

        BCrypt.Result result = BCrypt.verifyer()
            .verify(rawPassword.toCharArray(), user.getPassword());

        if ((!result.verified || user.isDeleted())) {
            log.warn("로그인 실패 - 잘못된 비밀번호: email={}", email);
            throw new InvalidEmailOrPasswordException(email);
        }

        log.info("로그인 성공 - email={}, nickname={}", user.getEmail(), user.getNickname());

        return userMapper.toDto(user);
    }

    @Override
    public UserDto updateUser(UUID requestHeaderUserId, UUID userId, UserUpdateRequest request) {

        log.info("사용자 정보 수정 시작 - userId={}, nickname={}", userId, request.nickname());
        validateOwnership(requestHeaderUserId, userId);

        User user = userRepository.findByIdAndIsDeletedFalse(userId)
            .orElseThrow(
                () -> {
                    log.warn("수정 실패 (존재하지 않는 사용자): userId={}", userId);
                    return new UserNotFoundException(userId);
                });

        user.update(request.nickname());

        log.info("사용자 정보 수정 완료 - id={}, nickname={}", user.getId(), user.getNickname());

        return userMapper.toDto(user);
    }

    @Override
    public void deleteUser(UUID requestHeaderUserId, UUID userId) {

        log.info("사용자 논리 삭제 시작: userId={}", userId);
        validateOwnership(requestHeaderUserId, userId);

        User user = userRepository.findByIdAndIsDeletedFalse(userId)
            .orElseThrow(() -> new UserNotFoundException(userId));

        logicallyDeleteUser(user, userId);

        log.info("사용자 논리 삭제 완료: userId={}", userId);

    }

    @Override
    public void deleteHardUser(UUID requestHeaderUserId, UUID userId) {

        log.info("사용자 물리 삭제 요청: userId={}", userId);
        validateOwnership(requestHeaderUserId, userId);

        User user = userRepository.findById(userId)
            .orElseThrow(() -> new UserNotFoundException(userId));

        if (!user.isDeleted()) {
            logicallyDeleteUser(user, userId);
        }

        List<Comment> comments = commentRepository.findByAuthorId(userId);
        comments.forEach(comment -> {
            commentLikeRepository.deleteByCommentId(comment.getId());
            commentRepository.delete(comment);
        });
        commentLikeRepository.deleteByUserId(userId);
        log.debug("댓글 좋아요 객체 삭제 완료");
        subscriptionRepository.deleteByUserId(userId);
        log.debug("구독 객체 삭제 완료");
        userRepository.deleteById(userId);
        log.debug("사용자 삭제 완료");

        log.info("사용자 물리 삭제 완료: userId={}", userId);

    }

    private void validateOwnership(UUID requestHeaderUserId, UUID userId) {
        if (!requestHeaderUserId.equals(userId)) {
            log.warn("처리 실패: 다른 사용자 접근 시도 - requestHeaderUserId={}, userId={}",
                requestHeaderUserId, userId);
            throw new ForbiddenAccessException("다른 사용자의 정보는 수정 및 삭제할 수 없습니다");
        }
    }

    private void logicallyDeleteUser(User user, UUID userId) {
        user.setDeleted();

        List<Subscription> subscriptions = subscriptionRepository.findAllByUserId(userId);
        log.debug("사용자 관심사 구독자 수 감소 시작: userId={}", userId);
        subscriptions.forEach(subscription ->
            interestRepository.decrementSubscriberCount(subscription.getInterest().getId()));
        log.debug("사용자 관심사 구독자 수 감소 완료: userId={}", userId);

        List<CommentLike> commentLikes = commentLikeRepository.findAllByUserId(userId);
        log.debug("댓글 좋아요 수 감소 시작: userId={}", userId);
        commentLikes.forEach(commentLike ->
            commentRepository.decreaseLikeCountById(commentLike.getComment().getId()));
        log.debug("댓글 좋아요 수 감소 완료: userId={}", userId);
    }
}
