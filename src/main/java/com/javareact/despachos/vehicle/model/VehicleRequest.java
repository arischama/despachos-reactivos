package com.javareact.despachos.vehicle.model;

import com.fasterxml.jackson.annotation.JsonProperty;

public record VehicleRequest(

        @JsonProperty("id")
        Long id,

        @JsonProperty("placa")
        String licensePlate,

        @JsonProperty("ciudad")
        String city,

        @JsonProperty("cupoKg")
        Integer capacityKg
) {
}