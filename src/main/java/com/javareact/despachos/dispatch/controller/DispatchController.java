package com.javareact.despachos.dispatch.controller;

import com.javareact.despachos.dispatch.dto.DispatchCreateRequest;
import com.javareact.despachos.dispatch.dto.DispatchResponse;
import com.javareact.despachos.dispatch.service.DispatchService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;


@RestController
@RequestMapping("/api/despachos")
@RequiredArgsConstructor
public class DispatchController {

    private final DispatchService dispatchService;

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
