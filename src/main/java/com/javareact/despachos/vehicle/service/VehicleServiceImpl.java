package com.javareact.despachos.vehicle.service;

import com.javareact.despachos.vehicle.model.Vehicle;
import com.javareact.despachos.vehicle.repository.VehicleRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * Servicio reactivo para la gestión de vehículos.
 */
@Service
@RequiredArgsConstructor
public class VehicleServiceImpl
        implements VehicleService {

    private final VehicleRepository vehicleRepository;

    @Override
    public Mono<Vehicle> save(
            final Vehicle vehicle) {

        return this.vehicleRepository.save(vehicle);
    }

    @Override
    public Flux<Vehicle> findAll() {

        return this.vehicleRepository.findAll();
    }

    @Override
    public Mono<Vehicle> findById(
            final Long id) {

        return this.vehicleRepository.findById(id);
    }
}