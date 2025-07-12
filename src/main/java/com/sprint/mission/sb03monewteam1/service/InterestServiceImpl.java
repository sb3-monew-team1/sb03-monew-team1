package com.sprint.mission.sb03monewteam1.service;

import com.sprint.mission.sb03monewteam1.dto.request.InterestRegisterRequest;
import com.sprint.mission.sb03monewteam1.dto.response.InterestResponse;
import com.sprint.mission.sb03monewteam1.entity.Interest;
import com.sprint.mission.sb03monewteam1.exception.interest.InterestDuplicateException;
import com.sprint.mission.sb03monewteam1.mapper.InterestMapper;
import com.sprint.mission.sb03monewteam1.repository.InterestRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class InterestServiceImpl implements InterestService {

    private final InterestRepository interestRepository;
    private final InterestMapper interestMapper;

    @Override
    public InterestResponse create(InterestRegisterRequest request) {
        if (interestRepository.existsByName(request.name())) {
            throw new InterestDuplicateException(request.name());
        }

        Interest interest = Interest.builder()
            .name(request.name())
            .build();

        Interest saved = interestRepository.save(interest);

        return interestMapper.toDto(saved, false);
    }
}
