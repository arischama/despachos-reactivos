package com.javareact.despachos.dispatch.controller;

import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;


@RestController
@RequestMapping("/external")
public class ExternalSimulatorController {

    private final AtomicInteger scoreRiesgoSimulado = new AtomicInteger(30);
    private final AtomicLong latenciaRiesgoMs = new AtomicLong(0);
    private final AtomicInteger fallasTarifa = new AtomicInteger(0);

    public record SimuladorConfig(Integer fallasTarifa, Long latenciaRiesgoMs, Integer scoreRiesgo) {}

    @PutMapping("/simulator")
    public Mono<Map<String, Object>> configurarSimulador(@RequestBody SimuladorConfig config) {
        if (config.scoreRiesgo() != null) scoreRiesgoSimulado.set(config.scoreRiesgo());
        if (config.latenciaRiesgoMs() != null) latenciaRiesgoMs.set(config.latenciaRiesgoMs());
        if (config.fallasTarifa() != null) fallasTarifa.set(config.fallasTarifa());

        return Mono.just(Map.of(
                "status", "ok",
                "scoreRiesgo", scoreRiesgoSimulado.get(),
                "latenciaRiesgoMs", latenciaRiesgoMs.get(),
                "fallasTarifa", fallasTarifa.get()
        ));
    }

    @DeleteMapping("/simulator")
    public Mono<Void> resetearSimulador() {
        scoreRiesgoSimulado.set(30);
        latenciaRiesgoMs.set(0);
        fallasTarifa.set(0);
        return Mono.empty();
    }

    @GetMapping("/tarifa/{ciudad}")
    public Mono<Map<String, Object>> obtenerTarifa(@PathVariable String ciudad) {
        if (fallasTarifa.getAndDecrement() > 0) {
            return Mono.error(new RuntimeException("Error simulado en servicio de tarifas"));
        }
        return Mono.just(Map.of("montoBase", 150000.0, "moneda", "COP"));
    }

    @GetMapping("/clima/{ciudad}")
    public Mono<Map<String, Object>> obtenerClima(@PathVariable String ciudad) {
        return Mono.just(Map.of("estadoClima", "BUENO", "rutaHabilitada", true));
    }

    @GetMapping("/riesgo/{ciudad}")
    public Mono<Map<String, Object>> obtenerRiesgo(@PathVariable String ciudad) {
        Mono<Map<String, Object>> respuesta = Mono.just(Map.of("scoreRiesgo", scoreRiesgoSimulado.get()));

        if (latenciaRiesgoMs.get() > 0) {
            return respuesta.delayElement(Duration.ofMillis(latenciaRiesgoMs.get()));
        }
        return respuesta;
    }
}