package com.javareact.despachos.dispatch.stream;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.javareact.despachos.dispatch.dto.DispatchResponse;
import org.springframework.stereotype.Service;
import reactor.core.publisher.BufferOverflowStrategy;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.publisher.Sinks;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class DispatchStreamService {

    private static final int STREAM_BUFFER_SIZE = 256;

    private final ObjectMapper objectMapper = new ObjectMapper();
    private final Map<Long, Sinks.Many<DispatchResponse>> dispatchStreams = new ConcurrentHashMap<>();
    private final Sinks.Many<DispatchResponse> hotBoardSink = Sinks.many().multicast().onBackpressureBuffer(STREAM_BUFFER_SIZE);
    private final Sinks.Many<DispatchResponse> reportSink = Sinks.many().multicast().onBackpressureBuffer(STREAM_BUFFER_SIZE);

    public void publish(DispatchResponse dispatch) {
        if (dispatch == null || dispatch.id() == null) {
            return;
        }

        Sinks.Many<DispatchResponse> sink = dispatchStreams.computeIfAbsent(
                dispatch.id(),
                ignored -> Sinks.many().multicast().onBackpressureBuffer(STREAM_BUFFER_SIZE)
        );

        sink.tryEmitNext(dispatch);
        hotBoardSink.tryEmitNext(dispatch);
        reportSink.tryEmitNext(dispatch);
    }

    public Flux<DispatchResponse> stream(Long dispatchId) {
        Sinks.Many<DispatchResponse> sink = dispatchStreams.computeIfAbsent(
                dispatchId,
                ignored -> Sinks.many().multicast().onBackpressureBuffer(STREAM_BUFFER_SIZE)
        );

        return sink.asFlux()
                .onBackpressureBuffer(STREAM_BUFFER_SIZE, __ -> {}, BufferOverflowStrategy.DROP_OLDEST)
                .limitRate(32);
    }

    public Flux<DispatchResponse> hotBoardStream() {
        return hotBoardSink.asFlux()
                .onBackpressureBuffer(STREAM_BUFFER_SIZE, __ -> {}, BufferOverflowStrategy.DROP_OLDEST)
                .limitRate(32);
    }

    public Flux<DispatchResponse> reportStream() {
        return reportSink.asFlux()
                .onBackpressureBuffer(STREAM_BUFFER_SIZE, __ -> {}, BufferOverflowStrategy.DROP_OLDEST)
                .limitRate(32);
    }

    public Mono<Void> consume(Flux<DispatchResponse> events) {
        return events
                .doOnNext(this::publish)
                .then();
    }

    public String toNdjson(DispatchResponse event) {
        try {
            return objectMapper.writeValueAsString(event) + "\n";
        } catch (JsonProcessingException e) {
            throw new IllegalStateException("No se pudo serializar el evento de despacho a NDJSON", e);
        }
    }
}
