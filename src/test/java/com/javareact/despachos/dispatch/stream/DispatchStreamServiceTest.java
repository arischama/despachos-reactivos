package com.javareact.despachos.dispatch.stream;

import com.javareact.despachos.dispatch.dto.DispatchResponse;
import org.junit.jupiter.api.Test;
import org.springframework.http.codec.ServerSentEvent;
import reactor.test.StepVerifier;

import java.time.Instant;

import static org.junit.jupiter.api.Assertions.assertEquals;

class DispatchStreamServiceTest {

    @Test
    void shouldPublishDispatchEventsToStreamByDispatchId() {
        DispatchStreamService streamService = new DispatchStreamService();
        DispatchResponse response = new DispatchResponse(1L, "ASSIGNED", 150000.0, 25, Instant.now());

        StepVerifier.create(streamService.stream(response.id()).take(1))
                .then(() -> streamService.publish(response))
                .assertNext(event -> {
                    assertEquals(response.id(), event.data().id());
                    assertEquals("dispatch", event.event());
                })
                .verifyComplete();
    }
}
