package com.javareact.despachos.vehicle.controller;

import com.javareact.despachos.vehicle.model.Vehicle;
import com.javareact.despachos.vehicle.model.VehicleRequest;
import com.javareact.despachos.vehicle.model.VehicleResponse;
import com.javareact.despachos.vehicle.service.VehicleService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/api/vehiculos")
@RequiredArgsConstructor
public class VehicleController {

    private final VehicleService vehicleService;

    @GetMapping
    public Flux<VehicleResponse> findAll() {

        return this.vehicleService.findAll()
                .map(this::toResponse);
    }

    @GetMapping("/{id}")
    public Mono<VehicleResponse> findById(
            @PathVariable final Long id) {

        return this.vehicleService.findById(id)
                .map(this::toResponse);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Mono<VehicleResponse> create(
            @RequestBody final VehicleRequest request) {

        return this.vehicleService.save(
                        Vehicle.builder()
                                .id(request.id())
                                .placa(request.licensePlate())
                                .ciudad(request.city())
                                .capacityKg(request.capacityKg())
                                .reservedKg(0)
                                .build())
                .map(this::toResponse);
    }

    private VehicleResponse toResponse(
            final Vehicle vehicle) {

        return new VehicleResponse(
                vehicle.getId(),
                vehicle.getPlaca(),
                vehicle.getCiudad(),
                vehicle.getCapacityKg(),
                vehicle.getReservedKg()
        );
    }
}