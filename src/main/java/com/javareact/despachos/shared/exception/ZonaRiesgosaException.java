package com.javareact.despachos.shared.exception;

public class ZonaRiesgosaException extends RuntimeException {
    public ZonaRiesgosaException(String city, Integer riskScore) {
        super("La ciudad " + city + " no está habilitada por alto score de riesgo: " + riskScore);
    }
}
