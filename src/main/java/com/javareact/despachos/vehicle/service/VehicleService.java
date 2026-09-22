package com.javareact.despachos.vehicle.service;

import com.javareact.despachos.vehicle.model.Vehicle;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface VehicleService {

    Mono<Vehicle> save(Vehicle vehicle);

    Flux<Vehicle> findAll();

    Mono<Vehicle> findById(Long id);
}