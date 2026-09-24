package com.javareact.despachos.capacity.saga;

import com.javareact.despachos.capacity.service.CapacityService;
import com.javareact.despachos.dispatch.dto.PackageItemRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

@Slf4j
@Component
@RequiredArgsConstructor
public class AssignmentSaga {

    private final CapacityService capacityService;

    public record ReservedItem(Long vehicleId, Integer weightKg) {}

    public Mono<Void> reserveAll(List<PackageItemRequest> packages) {
        List<ReservedItem> successfulReservations = new CopyOnWriteArrayList<>();

        return Flux.fromIterable(packages)
                .concatMap(pkg -> capacityService.reserve(pkg.vehicleId(), pkg.weightKg())
                        .doOnNext(veh -> successfulReservations.add(new ReservedItem(pkg.vehicleId(), pkg.weightKg()))))
                .then()
                .onErrorResume(error -> compensate(successfulReservations)
                        .then(Mono.error(error)));
    }


    public Mono<Void> compensate(List<ReservedItem> reservations) {
        log.warn("Iniciando compensación de la Saga para {} vehículos", reservations.size());
        return Flux.fromIterable(reservations)
                .concatMap(item -> capacityService.release(item.vehicleId(), item.weightKg())
                        .onErrorResume(ex -> {
                            log.error("Error compensando vehículo {}: {}", item.vehicleId(), ex.getMessage());
                            return Mono.empty();
                        }))
                .then();
    }
}
