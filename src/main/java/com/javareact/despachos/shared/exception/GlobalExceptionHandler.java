package com.javareact.despachos.shared.exception;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(ZonaRiesgosaException.class)
    public ProblemDetail handleZonaRiesgosa(ZonaRiesgosaException ex) {
        log.warn("Despacho rechazado por riesgo: {}", ex.getMessage());
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(HttpStatus.UNPROCESSABLE_ENTITY, ex.getMessage());
        problem.setTitle("Zona riesgosa");
        return problem;
    }

    @ExceptionHandler(CapacityUnavailableException.class)
    public ProblemDetail handleCapacityUnavailable(CapacityUnavailableException ex) {
        log.warn("Capacidad no disponible: {}", ex.getMessage());
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(HttpStatus.CONFLICT, ex.getMessage());
        problem.setTitle("Capacidad no disponible");
        return problem;
    }
}
