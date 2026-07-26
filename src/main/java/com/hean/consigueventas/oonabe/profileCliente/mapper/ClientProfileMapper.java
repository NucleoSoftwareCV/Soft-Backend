package com.hean.consigueventas.oonabe.profileCliente.mapper;

import com.hean.consigueventas.oonabe.category.entity.Category;
import com.hean.consigueventas.oonabe.profileCliente.dto.response.ClientInterestCategoryResponse;
import com.hean.consigueventas.oonabe.profileCliente.dto.response.ClientProfilePreferencesResponse;
import com.hean.consigueventas.oonabe.profileCliente.entity.ClientInterestCategory;
import com.hean.consigueventas.oonabe.profileCliente.entity.ClientProfile;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class ClientProfileMapper {

    public ClientProfilePreferencesResponse toPreferencesResponse(
            ClientProfile clientProfile,
            List<ClientInterestCategory> interestCategories
    ) {
        return new ClientProfilePreferencesResponse(
                clientProfile.getId(),
                clientProfile.getUser().getId(),
                clientProfile.getFirstName(),
                clientProfile.getLastName(),
                clientProfile.getCity() != null
                        ? clientProfile.getCity().getId()
                        : null,
                clientProfile.getCity() != null
                        ? clientProfile.getCity().getName()
                        : null,
                clientProfile.getCommunicationEmail(),
                clientProfile.getWhatsappPhone(),
                clientProfile.getReceiveSavedEventConfirmations(),
                clientProfile.getReceivePersonalizedRecommendations(),
                clientProfile.getReceiveReservationConfirmations(),
                clientProfile.getReceiveWeeklySummary(),
                toInterestCategoryResponses(interestCategories)
        );
    }

    private List<ClientInterestCategoryResponse> toInterestCategoryResponses(
            List<ClientInterestCategory> interestCategories
    ) {
        return interestCategories.stream()
                .map(ClientInterestCategory::getCategory)
                .map(this::toInterestCategoryResponse)
                .toList();
    }

    private ClientInterestCategoryResponse toInterestCategoryResponse(
            Category category
    ) {
        return new ClientInterestCategoryResponse(
                //category.getId(),
                category.getName()
        );
    }
}