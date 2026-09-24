package com.javareact.despachos.dispatch.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.Instant;

public record DispatchResponse(
        @JsonProperty("id") Long id,
        @JsonProperty("estado") String status,
        @JsonProperty("tarifa") Double fare,
        @JsonProperty("scoreRiesgo") Integer riskScore,
        @JsonProperty("expiraEn") Instant expiresAt
) {
}
