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

    @GetMapping(value = "/{id}/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<ServerSentEvent<DispatchResponse>> stream(@PathVariable Long id) {
        return dispatchStreamService.stream(id);
    }

    @GetMapping(value = "/hot", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<ServerSentEvent<DispatchResponse>> hotBoard() {
        return dispatchStreamService.hotBoardStream();
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
