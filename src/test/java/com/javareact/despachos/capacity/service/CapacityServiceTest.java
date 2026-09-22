package com.javareact.despachos.capacity.service;

import com.javareact.despachos.shared.exception.CapacityUnavailableException;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import reactor.test.StepVerifier;

@SpringBootTest
class CapacityServiceTest {

    @Autowired
    private CapacityService capacityService;

    @Test
    void shouldReserveCapacity() {

        StepVerifier.create(this.capacityService.reserve(1L, 100)).assertNext(vehicle -> {

            assert vehicle.getCapacityKg() == 400;
            assert vehicle.getReservedKg() == 100;

        }).verifyComplete();
    }

    @Test
    void shouldThrowExceptionWhenCapacityIsInsufficient() {

        StepVerifier.create(this.capacityService.reserve(1L, 99999))
                .expectError(CapacityUnavailableException.class)
                .verify();
    }
}