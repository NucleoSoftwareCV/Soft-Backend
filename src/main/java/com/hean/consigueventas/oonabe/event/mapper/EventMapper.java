package com.hean.consigueventas.oonabe.event.mapper;

import com.hean.consigueventas.oonabe.event.dto.request.EventUpsertRequest;
import com.hean.consigueventas.oonabe.event.dto.response.EventResponse;
import com.hean.consigueventas.oonabe.event.entity.Event;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface EventMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "category", ignore = true)
    @Mapping(target = "approvedBy", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    Event toEntity(EventUpsertRequest request);


    @Mapping(target = "categoryId", source = "category.id")
    @Mapping(target = "categoryName", source = "category.name")
    EventResponse toResponse(Event event);

}
