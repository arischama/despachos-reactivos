package com.javareact.despachos.vehicle.repository;

import com.javareact.despachos.vehicle.model.Vehicle;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface VehicleRepository
        extends ReactiveCrudRepository<Vehicle, Long> {
}