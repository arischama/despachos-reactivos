# Servicio Reactivo de Despachos de Envíos (Spring WebFlux + R2DBC)

Este proyecto implementa un servicio reactivo y no bloqueante para la gestión y orquestación de despachos de envíos, desarrollado con **Spring Boot 3**, **Spring WebFlux**, **Spring Data R2DBC** y **Project Reactor**, conectándose a una base de datos **PostgreSQL**.

---

## 👥 Información del Equipo y Cobertura

* **Proyecto:** Sistema de Despachos Reactivo
* **Integrantes:** 

  **Luis Carlos Ariza Camacho (arischama)** usuario de github: **arischama**
  
  **Cristian Camilo López (crcalope)** usuario de github: **cristianca23**

  **Maria Cristina Carmona Clavijo ()** usuario de github: ****

* **Paquete Base:** `com.javareact.despachos`

## 🚀 Requisitos Previos

1. **Java JDK 21** o superior instalado.
2. **Docker** y **Docker Compose** en ejecución.
3. **Gradle Wrapper** (incluido en el repositorio como `./gradlew`).

---

## 🛠️ Instrucciones para Levantamiento del Proyecto

### Paso 1: Iniciar la Base de Datos PostgreSQL en Docker

Asegúrate de tener corriendo el contenedor de PostgreSQL con las credenciales configuradas para R2DBC (base de datos `despachos`, usuario `postgres`, contraseña `postgres` en puerto `5432`):

```bash
docker compose up -d
```

*Nota: La estructura de tablas y secuencias autoincrementables (`BIGSERIAL`) se ejecuta automáticamente al iniciar mediante `src/main/resources/schema.sql`.*

---

### Paso 2: Compilar y Ejecutar la Aplicación Spring Boot

Ejecuta el siguiente comando en la raíz del proyecto para iniciar el servidor en el puerto **8081**:

```bash
# En macOS / Linux
./gradlew bootRun

# En Windows (PowerShell)
.\gradlew.bat bootRun
```

Verifica en los logs que la aplicación inicie correctamente sin errores de conexión a PostgreSQL.

---

## 🧪 Secuencia de Pruebas E2E (Verificación de Funcionalidad)

A continuación se detalla la secuencia de comandos `curl` para validar el funcionamiento completo de las Fases F2, F3 y F4:

### 1. Crear Vehículo Inicial (F1)
```bash
curl -i -X POST http://localhost:8081/api/vehiculos \
  -H "Content-Type: application/json" \
  -d '{"placa": "ABC123", "cupoKg": 500, "ciudad": "BOG"}'
```
* **Respuesta esperada:** HTTP `201 Created` con el objeto del vehículo registrado (`id: 1`).

---

### 2. Crear Despacho en Flujo Feliz (F2 + F3 + F4)
```bash
curl -i -X POST http://localhost:8081/api/despachos \
  -H "Content-Type: application/json" \
  -H "X-Traza-Id: traza-123" \
  -d '{
    "clienteId": 101,
    "ciudad": "BOG",
    "paquetes": [{"vehiculoId": 1, "pesoKg": 100}]
  }'
```
* **Respuesta esperada:** HTTP `201 Created` con estado `"ASSIGNED"`.

---

### 3. Confirmar Asignación de Despacho (F4)
```bash
curl -i -X POST http://localhost:8081/api/despachos/1/confirm
```
* **Respuesta esperada:** HTTP `200 OK` actualizando el estado del despacho a `"EN_RUTA"`.

---

### 4. Probar Reintentos y Fallback de Tarifa (F3 Resiliencia)
Inyecta 3 fallas consecutivas en el simulador de tarifas:
```bash
curl -i -X PUT http://localhost:8081/external/simulator \
  -H "Content-Type: application/json" \
  -d '{"fallasTarifa": 3}'
```
Crea un nuevo despacho:
```bash
curl -i -X POST http://localhost:8081/api/despachos \
  -H "Content-Type: application/json" \
  -d '{
    "clienteId": 102,
    "ciudad": "BOG",
    "paquetes": [{"vehiculoId": 1, "pesoKg": 50}]
  }'
```
* **Respuesta esperada:** HTTP `201 Created`. El cliente aplicó reintentos exponenciales (`retryWhen`) y recurrió a la tarifa base de respaldo (`150000.0`).

---

### 5. Probar Rechazo por Zona Riesgosa y Compensación de la Saga (F2 + F3)
Configura el score de riesgo en un valor crítico (95):
```bash
curl -i -X PUT http://localhost:8081/external/simulator \
  -H "Content-Type: application/json" \
  -d '{"scoreRiesgo": 95}'
```
Intenta crear un despacho:
```bash
curl -i -X POST http://localhost:8081/api/despachos \
  -H "Content-Type: application/json" \
  -d '{
    "clienteId": 103,
    "ciudad": "BOG",
    "paquetes": [{"vehiculoId": 1, "pesoKg": 200}]
  }'
```
* **Respuesta esperada:** HTTP `422 Unprocessable Entity` (`ZonaRiesgosaException`).
* **Verificación de Compensación:** Al consultar `GET /api/vehiculos/1`, se comprueba que el cupo de 200 kg reservado previamente fue devuelto íntegramente al vehículo.

Restablece el simulador:
```bash
curl -i -X DELETE http://localhost:8081/external/simulator
```