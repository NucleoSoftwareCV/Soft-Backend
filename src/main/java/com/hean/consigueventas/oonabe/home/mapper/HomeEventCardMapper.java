package com.hean.consigueventas.oonabe.home.mapper;

import com.hean.consigueventas.oonabe.event.dto.response.EventCardResponse;
import com.hean.consigueventas.oonabe.home.dto.response.HomeEventCardResponse;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface HomeEventCardMapper {

    @Mapping(target = "recurrenceLabel", expression = "java(recurrenceLabel(source.isRecurring()))")
    HomeEventCardResponse toResponse(EventCardResponse source);

    default String recurrenceLabel(Boolean recurring) {
        return Boolean.TRUE.equals(recurring) ? "RECURRENTE" : null;
    }
}
