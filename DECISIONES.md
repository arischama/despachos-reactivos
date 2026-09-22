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

## Decisiones propias

## 5. <título>

- **Elegimos:** <>
- **Descartamos:** <>
- **Porque:** <>
- **Se rompe si:** <>
