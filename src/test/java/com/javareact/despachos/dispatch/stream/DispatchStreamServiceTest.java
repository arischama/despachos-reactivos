package com.javareact.despachos.dispatch.stream;

import com.javareact.despachos.dispatch.dto.DispatchResponse;
import org.junit.jupiter.api.Test;
import reactor.core.publisher.Flux;
import reactor.test.StepVerifier;

import java.time.Instant;

import static org.junit.jupiter.api.Assertions.assertEquals;

class DispatchStreamServiceTest {

    @Test
    void shouldPublishDispatchEventsToNdjsonStreamByDispatchId() {
        DispatchStreamService streamService = new DispatchStreamService();
        DispatchResponse response = new DispatchResponse(1L, "ASSIGNED", 150000.0, 25, Instant.now());

        StepVerifier.create(streamService.stream(response.id()).take(1))
                .then(() -> streamService.publish(response))
                .assertNext(event -> {
                    assertEquals(response.id(), event.id());
                    assertEquals("ASSIGNED", event.status());
                })
                .verifyComplete();
    }

    @Test
    void shouldConsumeClientStreamAndPublishEvents() {
        DispatchStreamService streamService = new DispatchStreamService();
        DispatchResponse response = new DispatchResponse(7L, "EN_RUTA", 210000.0, 30, Instant.now());

        StepVerifier.create(
                        streamService.consume(Flux.just(response))
                                .thenMany(streamService.stream(response.id()).take(1)))
                .assertNext(event -> {
                    assertEquals(response.id(), event.id());
                    assertEquals("EN_RUTA", event.status());
                })
                .verifyComplete();
    }
}
