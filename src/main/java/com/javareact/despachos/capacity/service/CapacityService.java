package com.javareact.despachos.capacity.service;

import com.javareact.despachos.vehicle.model.Vehicle;
import reactor.core.publisher.Mono;

public interface CapacityService {

    Mono<Vehicle> reserve(
            Long vehicleId,
            Integer weightKg);

    Mono<Void> release(
            Long vehicleId,
            Integer weightKg);
}