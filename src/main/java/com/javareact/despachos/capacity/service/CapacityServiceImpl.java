package com.javareact.despachos.capacity.service;

import com.javareact.despachos.shared.exception.CapacityUnavailableException;
import com.javareact.despachos.vehicle.model.Vehicle;
import lombok.RequiredArgsConstructor;
import org.springframework.r2dbc.core.DatabaseClient;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

/**
 * Maneja reservas de cupo utilizando una operación
 * atómica UPDATE ... RETURNING.
 */
@Service
@RequiredArgsConstructor
public class CapacityServiceImpl implements CapacityService {

    private final DatabaseClient databaseClient;

    @Override
    public Mono<Vehicle> reserve(final Long vehicleId, final Integer weightKg) {

        return this.databaseClient.sql("""
                        UPDATE vehiculo
                           SET cupo_kg = cupo_kg - :weight,
                               reservado_kg = reservado_kg + :weight
                         WHERE id = :vehicleId
                           AND cupo_kg >= :weight
                        RETURNING id,
                                  placa,
                                  ciudad,
                                  cupo_kg,
                                  reservado_kg
                        """)
                .bind("vehicleId", vehicleId)
                .bind("weight", weightKg)
                .map((row, metadata) -> Vehicle.builder()
                        .id(row.get("id", Long.class))
                        .placa(row.get("placa", String.class))
                        .ciudad(row.get("ciudad", String.class))
                        .capacityKg(row.get("cupo_kg", Integer.class))
                        .reservedKg(row.get("reservado_kg", Integer.class))
                        .build())
                .one()
                .switchIfEmpty(Mono.error(new CapacityUnavailableException(vehicleId, weightKg)));
    }

    @Override
    public Mono<Void> release(final Long vehicleId, final Integer weightKg) {

        return this.databaseClient.sql("""
                UPDATE vehiculo
                   SET cupo_kg = cupo_kg + :weight,
                       reservado_kg = reservado_kg - :weight
                 WHERE id = :vehicleId
                """).bind("vehicleId", vehicleId).bind("weight", weightKg).fetch().rowsUpdated().then();
    }
}