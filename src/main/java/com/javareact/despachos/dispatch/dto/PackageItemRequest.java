package com.javareact.despachos.dispatch.dto;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotNull;
public record PackageItemRequest(
        @NotNull(message = "El vehiculoId es obligatorio")
        @JsonProperty("vehiculoId")
        Long vehicleId, @NotNull(message = "El pesoKg es obligatorio")
        @JsonProperty("pesoKg")
        Integer weightKg
) {

}