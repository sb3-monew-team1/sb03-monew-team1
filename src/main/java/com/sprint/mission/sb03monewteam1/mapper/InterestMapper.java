package com.sprint.mission.sb03monewteam1.mapper;

import com.sprint.mission.sb03monewteam1.dto.InterestDto;
import com.sprint.mission.sb03monewteam1.entity.Interest;
import com.sprint.mission.sb03monewteam1.entity.InterestKeyword;
import org.mapstruct.Mapper;

import java.util.List;
import java.util.stream.Collectors;

@Mapper(componentModel = "spring")
public interface InterestMapper {

    InterestDto toDto(Interest interest, boolean subscribedByMe);

    default List<String> map(List<InterestKeyword> keywords) {
        if (keywords == null) {
            return null;
        }
        return keywords.stream()
            .map(InterestKeyword::getKeyword)
            .collect(Collectors.toList());
    }
}
