package com.javareact.despachos.shared.filter;

import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;
import reactor.core.publisher.Mono;

import java.util.Optional;
import java.util.UUID;

/**
 * WebFilter reactivo para propagar el trazaId mediante el Reactor Context.
 */
@Component
public class TraceWebFilter implements WebFilter {

    public static final String TRACE_KEY = "trazaId";

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, WebFilterChain chain) {
        String traceId = Optional.ofNullable(exchange.getRequest().getHeaders().getFirst("X-Traza-Id"))
                .orElseGet(() -> UUID.randomUUID().toString().substring(0, 8));

        exchange.getResponse().getHeaders().add("X-Traza-Id", traceId);

        return chain.filter(exchange)
                .contextWrite(context -> context.put(TRACE_KEY, traceId));
    }
}
