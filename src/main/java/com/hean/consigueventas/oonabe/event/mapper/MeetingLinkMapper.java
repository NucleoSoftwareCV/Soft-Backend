package com.hean.consigueventas.oonabe.event.mapper;

import com.hean.consigueventas.oonabe.event.dto.request.MeetingLinkUpsertRequest;
import com.hean.consigueventas.oonabe.event.dto.response.MeetingLinkResponse;
import com.hean.consigueventas.oonabe.event.entity.MeetingLink;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface MeetingLinkMapper {
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "eventOccurrence", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    MeetingLink toEntity(
            MeetingLinkUpsertRequest request
    );

    MeetingLinkResponse toResponse(MeetingLink meetingLink);
}
