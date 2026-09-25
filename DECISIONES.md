# Decisiones de diseño

> Máximo una página. Una decisión por bloque, en el formato de abajo.
> Las cuatro primeras son **obligatorias**; agregar las propias si las hay.
> Se evalúa el argumento, no la extensión. "Porque así sale en la demo" no es un argumento.

**Formato:** *Elegimos X · Descartamos Y · Porque Z · Se rompe si W*

---

### 1. flatMap vs concatMap (obligatoria)

- **Elegimos:** `concatMap` para ejecutar las reservas y compensaciones en `AssignmentSaga`.

- **Descartamos:** `flatMap`.

- **Porque:** la saga debe conocer con precisión qué reservas fueron exitosas para poder compensarlas en caso de error. `concatMap` garantiza procesamiento secuencial y preserva el orden de ejecución, permitiendo llevar un registro consistente de las reservas realizadas antes de una falla. Esto simplifica la compensación y hace más predecible el comportamiento del flujo.

- **Se rompe si:** un despacho contiene una gran cantidad de paquetes, ya que la ejecución secuencial aumenta la latencia total. En ese escenario sería necesario evaluar `flatMap` con concurrencia acotada y un mecanismo explícito para registrar y compensar las reservas realizadas en paralelo.

## 2. Estrategia de backpressure del tablero (obligatoria)

- **Elegimos:** <`onBackpressureLatest`>
- **Descartamos:** <`onBackpressureBuffer`, `limitRate`>
- **Porque:** <el tablero muestra estado actual; a un operador con red lenta le sirve el último evento,
  no una cola de eventos viejos. Bufferear termina en OOM con un cliente colgado>
- **Se rompe si:** <el tablero se usara para auditoría: ahí perder eventos es inaceptable y habría que
  persistir el stream>

## 3. Hot y no cold en el tablero (obligatoria)

- **Elegimos:** <`Sinks.many().multicast()` + `publish().refCount()`>
- **Descartamos:** <un `Flux` frío por suscriptor>
- **Porque:** <cada suscriptor frío abriría su propia consulta y su propio job; con 10 operadores conectados
  serían 10 fuentes haciendo el mismo trabajo y viendo datos distintos>
- **Se rompe si:** <no hay suscriptores: `refCount` corta la fuente y se pierden los eventos de ese intervalo;
  lo asumimos porque nadie los está mirando>

### 4. Dónde empieza y termina la transacción (obligatoria)

- **Elegimos:** mantener la reserva de cupo como una operación atómica independiente mediante `UPDATE ... RETURNING`.

- **Descartamos:** realizar lectura previa con `findById` seguida de actualización mediante `save`.

- **Porque:** la validación y actualización ocurren en una única operación SQL, evitando condiciones de carrera bajo concurrencia.

- **Se rompe si:** el proceso falla después de reservar capacidad y antes de completar el flujo de negocio; en ese caso la reversión debe realizarse mediante la saga de compensación.

---

## 5. Llamadas simultáneas con `Mono.zip` y la personalización de la resiliencia por cliente

* **Opción:** Invocar los tres servicios externos (`rate`, `weather`, `risk`) en paralelo con `Mono.zip` en `CarrierClient.java`, empleando mecanismos de resiliencia distintos:
  * *Rate:* Retrasos exponenciales con estrategia de retroceso (`retryWhen(Retry.backoff(3, 200ms))`) para manejar fallos temporales, cayendo de vuelta a la tasa de catálogo base.
  * *Weather:* Almacenamiento en caché de la respuesta por 10 minutos (`.cache(Duration.ofMinutes(10))`).
  * *Risk:* Límite duro de 800 ms de tiempo de espera (`.timeout(Duration.ofMillis(800))`) junto con una puntuación inicial conservadora de riesgo.
* **Opciones rechazadas:** *Invocar los tres llamadas externas secuencialmente a través de `flatMap`. *Usar un mecanismo de resiliencia uniforme para todos los clientes.
* **Razonamiento:** La serialización de las peticiones daría como resultado un sobrecarga de red (I/O) acumulativo.Usar `Mono.zip` permite reducir la latencia general al de el componente más lento solamente (SLA < 1s). Además los perfiles de fallo de los tres servicios externos son distintos y por lo tanto la personalización de la resiliencia por cliente es necesaria para evitar fallos en cascada.>

---
# 6. Rastreabilidad con 'Contexto de Reactor' en lugar de 'ThreadLocal' o Parámetros

* **Lo que decidimos:** Usar el identificador 'X-Traza-Id' a través de 'TraceWebFilter' y 'contextWrite()' seguido de un uso desacoplado en capas profundas a través de 'deferContextual()'.
* **Lo que descartamos:** El uso de 'ThreadLocal' (MDC legado) y el paso del 'trazaId' como parámetro a cada método de servicio y repositorio.
* **Por qué:** La ejecución de la cadena reactiva en Spring WebFlux se realiza en el pool del bucle de eventos en diferentes hilos, por lo que 'ThreadLocal' pierde sus valores y se contamina con los valores de la solicitud de otros usuarios. El paso manual del parámetro a través de diferentes métodos contamina la firma del código de dominio. 'Contexto de Reactor' utiliza la suscripción del flujo reactivo "hacia arriba" para hacerlo seguro entre hilos.