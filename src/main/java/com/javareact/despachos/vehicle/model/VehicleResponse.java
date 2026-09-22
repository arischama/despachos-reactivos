package com.javareact.despachos.vehicle.model;

import com.fasterxml.jackson.annotation.JsonProperty;

public record VehicleResponse(

        @JsonProperty("id")
        Long id,

        @JsonProperty("placa")
        String placa,

        @JsonProperty("ciudad")
        String ciudad,

        @JsonProperty("cupoKg")
        Integer cupoKg,

        @JsonProperty("reservadoKg")
        Integer reservadoKg
) {
}
