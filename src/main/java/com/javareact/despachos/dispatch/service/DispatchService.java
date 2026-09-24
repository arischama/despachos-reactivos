package com.javareact.despachos.dispatch.service;

import com.javareact.despachos.dispatch.client.CarrierClient;
import com.javareact.despachos.dispatch.dto.DispatchCreateRequest;
import com.javareact.despachos.dispatch.dto.DispatchResponse;
import com.javareact.despachos.dispatch.model.Dispatch;
import com.javareact.despachos.dispatch.model.DispatchPackage;
import com.javareact.despachos.dispatch.repository.DispatchPackageRepository;
import com.javareact.despachos.dispatch.repository.DispatchRepository;
import com.javareact.despachos.capacity.saga.AssignmentSaga;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.reactive.TransactionalOperator;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.time.Instant;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class DispatchService {

    private final DispatchRepository dispatchRepository;
    private final DispatchPackageRepository dispatchPackageRepository;
    private final AssignmentSaga assignmentSaga;
    private final CarrierClient carrierClient;
    private final TransactionalOperator transactionalOperator;

    public Mono<DispatchResponse> createDispatch(DispatchCreateRequest request) {
        Dispatch initialDispatch = Dispatch.builder()
                .customerId(request.customerId())
                .city(request.city())
                .status("RECEIVED")
                .createdAt(Instant.now())
                .build();

        return dispatchRepository.save(initialDispatch)
                .flatMap(savedDispatch ->
                        assignmentSaga.reserveAll(request.packages())
                                .then(carrierClient.evaluate(request.city()))
                                .flatMap(eval -> {
                                    Instant expiresAt = Instant.now().plus(Duration.ofMinutes(15));
                                    Dispatch assignedDispatch = savedDispatch
                                            .withStatus("ASSIGNED")
                                            .withFare(eval.fare())
                                            .withRiskScore(eval.riskScore())
                                            .withExpiresAt(expiresAt);

                                    List<DispatchPackage> packagesToSave = request.packages().stream()
                                            .map(pkg -> new DispatchPackage(null, savedDispatch.id(), pkg.vehicleId(), pkg.weightKg()))
                                            .toList();

                                    return dispatchRepository.save(assignedDispatch)
                                            .flatMap(d -> dispatchPackageRepository.saveAll(packagesToSave).then(Mono.just(d)))
                                            .as(transactionalOperator::transactional)
                                            .map(d -> new DispatchResponse(d.id(), d.status(), d.fare(), d.riskScore(), d.expiresAt()));
                                })
                                .onErrorResume(error -> assignmentSaga.compensate(
                                        request.packages().stream().map(p -> new AssignmentSaga.ReservedItem(p.vehicleId(), p.weightKg())).toList()
                                ).then(Mono.error(error)))
                );
    }

    public Mono<DispatchResponse> confirmDispatch(Long id) {
        return dispatchRepository.findById(id)
                .switchIfEmpty(Mono.error(new RuntimeException("Despacho no encontrado")))
                .flatMap(dispatch -> {
                    if (!"ASSIGNED".equals(dispatch.status())) {
                        return Mono.error(new IllegalStateException("El despacho no está en estado ASSIGNED"));
                    }
                    return dispatchRepository.save(dispatch.withStatus("EN_RUTA"));
                })
                .as(transactionalOperator::transactional)
                .map(d -> new DispatchResponse(d.id(), d.status(), d.fare(), d.riskScore(), d.expiresAt()));
    }
}