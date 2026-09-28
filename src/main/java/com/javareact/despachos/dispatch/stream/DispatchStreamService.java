package com.javareact.despachos.dispatch.stream;

import com.javareact.despachos.dispatch.dto.DispatchResponse;
import org.springframework.http.codec.ServerSentEvent;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Sinks;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class DispatchStreamService {

    private final Map<Long, Sinks.Many<DispatchResponse>> dispatchStreams = new ConcurrentHashMap<>();
    private final Sinks.Many<DispatchResponse> hotBoardSink = Sinks.many().multicast().onBackpressureBuffer();

    public void publish(DispatchResponse dispatch) {
        if (dispatch == null || dispatch.id() == null) {
            return;
        }

        Sinks.Many<DispatchResponse> sink = dispatchStreams.computeIfAbsent(
                dispatch.id(),
                ignored -> Sinks.many().multicast().onBackpressureBuffer()
        );

        sink.tryEmitNext(dispatch);
        hotBoardSink.tryEmitNext(dispatch);
    }

    public Flux<ServerSentEvent<DispatchResponse>> stream(Long dispatchId) {
        Sinks.Many<DispatchResponse> sink = dispatchStreams.computeIfAbsent(
                dispatchId,
                ignored -> Sinks.many().multicast().onBackpressureBuffer()
        );

        return sink.asFlux()
                .map(event -> ServerSentEvent.<DispatchResponse>builder()
                        .event("dispatch")
                        .id(String.valueOf(event.id()))
                        .data(event)
                        .build());
    }

    public Flux<ServerSentEvent<DispatchResponse>> hotBoardStream() {
        return hotBoardSink.asFlux()
                .map(event -> ServerSentEvent.<DispatchResponse>builder()
                        .event("hot-board")
                        .id(String.valueOf(event.id()))
                        .data(event)
                        .build());
    }
}
