package com.hean.consigueventas.oonabe.rewards.Mapper;

import com.hean.consigueventas.oonabe.rewards.Dto.Response.PointsTransactionsResponse;
import com.hean.consigueventas.oonabe.rewards.Entity.PointsTransactions;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper(componentModel = "spring")
public interface PointsTransactionsMapper {

    @Mapping(
            source = "userPoints.user.email",
            target = "userEmail"
    )
    PointsTransactionsResponse toResponse(PointsTransactions transaction);
    List<PointsTransactionsResponse> toResponse(List<PointsTransactions> transactions);
}
