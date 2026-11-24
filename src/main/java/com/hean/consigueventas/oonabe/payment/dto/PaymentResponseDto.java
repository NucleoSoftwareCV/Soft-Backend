package com.hean.consigueventas.oonabe.payment.dto;

import com.hean.consigueventas.oonabe.campaign.dto.CampaignDtos.EntrySummary;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class PaymentResponseDto {
    private String status;
    private String transactionId;
    private String errorMessage;
    /** Participaciones en campañas promocionales generadas por este pago (vacía si ninguna). */
    private List<EntrySummary> campaignEntries = List.of();

    public PaymentResponseDto(String status, String transactionId, String errorMessage) {
        this(status, transactionId, errorMessage, List.of());
    }
}
