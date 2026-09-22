package com.javareact.despachos.shared.exception;

public class CapacityUnavailableException
        extends RuntimeException {

    public CapacityUnavailableException(
            final Long vehicleId,
            final Integer weightKg) {

        super(
                String.format(
                        "El vehículo %d no tiene cupo suficiente para reservar %d kg.",
                        vehicleId,
                        weightKg
                )
        );
    }
}