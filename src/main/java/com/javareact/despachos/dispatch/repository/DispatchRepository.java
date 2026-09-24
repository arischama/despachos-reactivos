package com.javareact.despachos.dispatch.repository;

import com.javareact.despachos.dispatch.model.Dispatch;
import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;

@Repository
public interface DispatchRepository extends ReactiveCrudRepository<Dispatch, Long> {

    @Query("SELECT * FROM despacho WHERE estado = :status AND expira_en < NOW()")
    Flux<Dispatch> findExpired(String status);
}
