package com.javareact.despachos.dispatch.client;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.javareact.despachos.shared.exception.ZonaRiesgosaException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;
import reactor.util.retry.Retry;

import java.time.Duration;
import java.util.concurrent.TimeoutException;


@Component
public class CarrierClient {

    private final WebClient webClient;

    @Autowired
    public CarrierClient(WebClient.Builder builder) {
        this.webClient = builder.baseUrl("http://localhost:8081/external").build();
    }

    public record RateResult(@JsonProperty("montoBase") Double baseFare) {}
    public record WeatherResult(@JsonProperty("estadoClima") String status, @JsonProperty("rutaHabilitada") Boolean routeEnabled) {}
    public record RiskResult(@JsonProperty("scoreRiesgo") Integer score) {}

    public record EvaluationResult(Double fare, String weatherStatus, Integer riskScore) {}


    public Mono<RateResult> fetchRate(String city) {
        return webClient.get()
                .uri("/tarifa/{city}", city)
                .retrieve()
                .bodyToMono(RateResult.class)
                .retryWhen(Retry.backoff(3, Duration.ofMillis(200))
                        .filter(ex -> !(ex instanceof IllegalArgumentException)))
                .onErrorResume(ex -> Mono.just(new RateResult(150000.0)));
    }


    public Mono<WeatherResult> fetchWeather(String city) {
        return webClient.get()
                .uri("/clima/{city}", city)
                .retrieve()
                .bodyToMono(WeatherResult.class)
                .cache(Duration.ofMinutes(10))
                .onErrorReturn(new WeatherResult("NORMAL", true));
    }

    public Mono<RiskResult> fetchRisk(String city) {
        return webClient.get()
                .uri("/riesgo/{city}", city)
                .retrieve()
                .bodyToMono(RiskResult.class)
                .timeout(Duration.ofMillis(800))
                .onErrorReturn(TimeoutException.class, new RiskResult(30))
                .onErrorReturn(new RiskResult(50));
    }

    public Mono<EvaluationResult> evaluate(String city) {
        return Mono.zip(fetchRate(city), fetchWeather(city), fetchRisk(city))
                .flatMap(tuple -> {
                    RateResult rate = tuple.getT1();
                    WeatherResult weather = tuple.getT2();
                    RiskResult risk = tuple.getT3();

                    if (risk.score() > 80) {
                        return Mono.error(new ZonaRiesgosaException(city, risk.score()));
                    }

                    return Mono.just(new EvaluationResult(
                            rate.baseFare(),
                            weather.status(),
                            risk.score()
                    ));
                });
    }
}