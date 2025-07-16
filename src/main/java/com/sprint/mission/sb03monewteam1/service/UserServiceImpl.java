package com.sprint.mission.sb03monewteam1.service;

import com.sprint.mission.sb03monewteam1.dto.UserDto;
import com.sprint.mission.sb03monewteam1.dto.request.UserLoginRequest;
import com.sprint.mission.sb03monewteam1.dto.request.UserRegisterRequest;
import com.sprint.mission.sb03monewteam1.dto.request.UserUpdateRequest;
import com.sprint.mission.sb03monewteam1.entity.CommentLike;
import com.sprint.mission.sb03monewteam1.entity.Subscription;
import com.sprint.mission.sb03monewteam1.entity.User;
import com.sprint.mission.sb03monewteam1.exception.user.EmailAlreadyExistsException;
import com.sprint.mission.sb03monewteam1.exception.user.ForbiddenAccessException;
import com.sprint.mission.sb03monewteam1.exception.user.InvalidEmailOrPasswordException;
import com.sprint.mission.sb03monewteam1.exception.user.UserNotFoundException;
import com.sprint.mission.sb03monewteam1.mapper.UserMapper;
import com.sprint.mission.sb03monewteam1.repository.CommentLikeRepository;
import com.sprint.mission.sb03monewteam1.repository.CommentRepository;
import com.sprint.mission.sb03monewteam1.repository.InterestRepository;
import com.sprint.mission.sb03monewteam1.repository.SubscriptionRepository;
import com.sprint.mission.sb03monewteam1.repository.UserRepository;
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
    public UserDto create(UserRegisterRequest userRegisterRequest) {

        log.info("사용자 생성 시작: email={}, nickname={}", userRegisterRequest.email(),
            userRegisterRequest.nickname());

        String email = userRegisterRequest.email();
        String nickname = userRegisterRequest.nickname();
        String password = userRegisterRequest.password();

        if (userRepository.existsByEmail(email)) {
            log.warn("중복된 이메일로 회원가입 시도: email={}", email);
            throw new EmailAlreadyExistsException(email);
        }

        User user = User.builder()
            .email(email)
            .nickname(nickname)
            .password(password)
            .build();

        User savedUser = userRepository.save(user);

        log.info("사용자 생성 완료: id={}, email={}, nickname={}",
            savedUser.getId(), savedUser.getEmail(), savedUser.getNickname());

        return userMapper.toDto(savedUser);
    }

    @Override
    public UserDto login(UserLoginRequest userLoginRequest) {

        String email = userLoginRequest.email();
        String password = userLoginRequest.password();

        log.info("로그인 인증 시작 - email={}", email);

        User user = userRepository.findByEmail(email)
            .orElseThrow(
                () -> {
                    log.warn("로그인 실패 - 존재하지 않는 이메일: {}", email);
                    return new InvalidEmailOrPasswordException(email);
                });

        if (!user.getPassword().equals(password) || user.isDeleted()) {
            log.warn("로그인 실패 - 존재하지 않는 비밀번호, 입력한 비밀번호: {}", password);
            throw new InvalidEmailOrPasswordException(email);
        }

        log.info("로그인 성공 - email={}, nickname={}", user.getEmail(), user.getNickname());

        return userMapper.toDto(user);
    }

    @Override
    public UserDto update(UUID requestHeaderUserId, UUID userId, UserUpdateRequest request) {

        log.info("사용자 정보 수정 시작 - userId={}, nickname={}", userId, request.nickname());

        if (!requestHeaderUserId.equals(userId)) {
            log.warn("수정 실패 (다른 사용자 정보 수정 요청): requestUserId={}, userId={}", requestHeaderUserId,
                userId);
            throw new ForbiddenAccessException("다른 사용자의 정보는 수정할 수 없습니다");
        }

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
    public void delete(UUID requestHeaderUserId, UUID userId) {

        log.info("사용자 논리 삭제 시작: userId={}", userId);

        if (!requestHeaderUserId.equals(userId)) {
            log.warn("논리 삭제 실패 (다른 사용자 논리 삭제 요청): requestUserId={}, userId={}", requestHeaderUserId,
                userId);
            throw new ForbiddenAccessException("다른 사용자는 삭제할 수 없습니다");
        }

        User user = userRepository.findByIdAndIsDeletedFalse(userId)
            .orElseThrow(() -> new UserNotFoundException(userId));

        user.setDeleted();

        List<Subscription> subscriptions = subscriptionRepository.findAllByUserId(userId);
        log.debug("사용자 관심사 구독자 수 감소 시작: userId={}", userId);
        subscriptions.forEach(subscription -> {
            interestRepository.decrementSubscriberCount(subscription.getInterest().getId());
        });
        log.debug("사용자 관심사 구독자 수 감소 완료: userId={}", userId);

        List<CommentLike> commentLikes = commentLikeRepository.findAllByUserId(userId);
        log.debug("댓글 좋아요 수 감소 및 댓글 논리 삭제 시작: userId={}", userId);
        commentLikes.forEach(commentLike -> {
            commentRepository.decreaseLikeCountAndDeleteById(commentLike.getComment().getId());
        });
        log.debug("댓글 좋아요 수 감소 및 댓글 논리 삭제 처리 완료: userId={}", userId);

        log.info("사용자 논리 삭제 완료: userId={}", userId);

    }

    @Override
    public void deleteHard(UUID requestHeaderUserId, UUID userId) {

        log.info("사용자 물리 삭제 요청: userId={}", userId);
        if (!requestHeaderUserId.equals(userId)) {
            log.warn("물리 삭제 실패 (다른 사용자 물리 삭제 요청): requestUserId={}, userId={}", requestHeaderUserId,
                userId);
            throw new ForbiddenAccessException("다른 사용자는 삭제 할 수 없습니다");
        }

        User user = userRepository.findByIdAndIsDeletedFalse(userId)
            .orElseThrow(() -> new UserNotFoundException(userId));

        user.setDeleted();

        List<Subscription> subscriptions = subscriptionRepository.findAllByUserId(userId);
        log.debug("사용자 관심사 구독자 수 감소 시작: userId={}", userId);
        subscriptions.forEach(subscription -> {
            interestRepository.decrementSubscriberCount(subscription.getInterest().getId());
        });
        log.debug("사용자 관심사 구독자 수 감소 완료: userId={}", userId);

        List<CommentLike> commentLikes = commentLikeRepository.findAllByUserId(userId);
        log.debug("댓글 좋아요 수 감소 및 댓글 논리 삭제 시작: userId={}", userId);
        commentLikes.forEach(commentLike -> {
            commentRepository.decreaseLikeCountAndDeleteById(commentLike.getComment().getId());
        });
        log.debug("댓글 좋아요 수 감소 및 댓글 논리 삭제 처리 완료: userId={}", userId);


        subscriptionRepository.deleteByUserId(userId);
        commentLikeRepository.deleteByUserId(userId);
        userRepository.deleteById(userId);

        log.info("사용자 물리 삭제 완료: userId={}", userId);

    }
}
