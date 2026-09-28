package com.javareact.despachos.dispatch.controller;

import com.javareact.despachos.dispatch.dto.DispatchCreateRequest;
import com.javareact.despachos.dispatch.dto.DispatchResponse;
import com.javareact.despachos.dispatch.service.DispatchService;
import com.javareact.despachos.dispatch.stream.DispatchStreamService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.codec.ServerSentEvent;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;


@RestController
@RequestMapping("/api/despachos")
@RequiredArgsConstructor
public class DispatchController {

    private final DispatchService dispatchService;
    private final DispatchStreamService dispatchStreamService;

    @GetMapping(value = "/{id}/stream", produces = "application/x-ndjson")
    public Flux<String> stream(@PathVariable Long id) {
        return dispatchStreamService.stream(id)
                .map(dispatchStreamService::toNdjson);
    }

    @GetMapping(value = "/{id}/sse", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<ServerSentEvent<DispatchResponse>> streamSse(@PathVariable Long id) {
        return dispatchStreamService.stream(id)
                .map(event -> ServerSentEvent.<DispatchResponse>builder()
                        .id(String.valueOf(event.id()))
                        .event("dispatch")
                        .data(event)
                        .build());
    }

    @GetMapping(value = "/hot", produces = "application/x-ndjson")
    public Flux<String> hotBoard() {
        return dispatchStreamService.hotBoardStream()
                .map(dispatchStreamService::toNdjson);
    }

    @GetMapping(value = "/hot/sse", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<ServerSentEvent<DispatchResponse>> hotBoardSse() {
        return dispatchStreamService.hotBoardStream()
                .map(event -> ServerSentEvent.<DispatchResponse>builder()
                        .id(String.valueOf(event.id()))
                        .event("dispatch")
                        .data(event)
                        .build());
    }

    @PostMapping(value = "/events", consumes = "application/x-ndjson")
    public Mono<Void> consumeEvents(@RequestBody Flux<DispatchResponse> events) {
        return dispatchStreamService.consume(events);
    }

    @PostMapping(value = "/sink")
    @ResponseStatus(HttpStatus.CREATED)
    public Mono<DispatchResponse> publishToSink(@RequestBody DispatchResponse event) {
        dispatchStreamService.publish(event);
        return Mono.just(event);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Mono<DispatchResponse> create(@Valid @RequestBody DispatchCreateRequest request) {
        return dispatchService.createDispatch(request);
    }

    @PostMapping("/{id}/confirm")
    public Mono<DispatchResponse> confirm(@PathVariable Long id) {
        return dispatchService.confirmDispatch(id);
    }
}
