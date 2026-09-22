package com.javareact.despachos.capacity.saga;

import com.javareact.despachos.capacity.model.Reservation;
import com.javareact.despachos.capacity.service.CapacityService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Saga responsable de compensar reservas realizadas.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class AssignmentSaga {

    private final CapacityService capacityService;

    public Mono<List<Reservation>> reserveAll(final List<Reservation> reservations) {

        final List<Reservation> completed = new CopyOnWriteArrayList<>();

        return Flux.fromIterable(reservations)
                .concatMap(reservation ->
                        this.capacityService.reserve(reservation.vehicleId(), reservation.weightKg())
                                .doOnNext(vehicle -> completed.add(reservation)))
                .then(Mono.just(List.copyOf(completed)))
                .onErrorResume(error -> this.compensate(completed).then(Mono.error(error)));
    }

    private Mono<Void> compensate(final List<Reservation> reservations) {

        return Flux.fromIterable(reservations)
                .concatMap(reservation ->
                        this.capacityService.release(reservation.vehicleId(), reservation.weightKg())
                                .onErrorResume(error -> {
                                    log.error("Falló compensación del vehículo {}", reservation.vehicleId(), error);
                                    return Mono.empty();
                                }))
                .then();
    }
}