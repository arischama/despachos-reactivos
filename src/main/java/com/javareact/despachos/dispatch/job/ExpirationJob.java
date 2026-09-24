package com.javareact.despachos.dispatch.job;

import com.javareact.despachos.dispatch.repository.DispatchRepository;
import com.javareact.despachos.dispatch.repository.DispatchPackageRepository;
import com.javareact.despachos.capacity.service.CapacityService;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.Disposable;

import java.time.Duration;

@Slf4j
@Component
@RequiredArgsConstructor
public class ExpirationJob {

    private final DispatchRepository dispatchRepository;
    private final DispatchPackageRepository dispatchPackageRepository;
    private final CapacityService capacityService;
    private Disposable subscription;

    @PostConstruct
    public void start() {
        subscription = Flux.interval(Duration.ofSeconds(30))
                .onBackpressureDrop(tick -> log.warn("Tick {} omitido por acumulación", tick))
                .concatMap(tick -> expireOrders()
                        .onErrorResume(ex -> {
                            log.error("Fallo durante expiración: {}", ex.getMessage());
                            return Mono.empty();
                        }))
                .subscribe();
    }

    private Mono<Void> expireOrders() {
        return dispatchRepository.findExpired("ASSIGNED")
                .flatMap(dispatch -> dispatchPackageRepository.findByDispatchId(dispatch.id())
                        .flatMap(pkg -> capacityService.release(pkg.vehicleId(), pkg.weightKg()))
                        .then(dispatchRepository.save(dispatch.withStatus("EXPIRED"))))
                .then();
    }

    @PreDestroy
    public void stop() {
        if (subscription != null && !subscription.isDisposed()) {
            subscription.dispose();
        }
    }
}
