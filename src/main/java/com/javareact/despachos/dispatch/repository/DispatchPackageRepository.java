package com.javareact.despachos.dispatch.repository;
import com.javareact.despachos.dispatch.model.DispatchPackage;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;

@Repository
public interface DispatchPackageRepository extends ReactiveCrudRepository<DispatchPackage, Long> {

    Flux<DispatchPackage> findByDispatchId(Long dispatchId);
}
