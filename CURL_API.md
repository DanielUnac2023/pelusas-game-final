# CURL - Documentación de la API REST Pelusas

## Base URL
`http://localhost:8080/api/partidas`

---

## Endpoints

### 1. Crear una nueva partida
**Método:** POST  
**URL:** `/api/partidas`

#### Descripción
Crea una nueva partida de Pelusas con entre 2 y 6 jugadores.

#### Solicitud
```bash
curl -X POST http://localhost:8080/api/partidas \
  -H "Content-Type: application/json" \
  -d '["Jugador 1", "Jugador 2"]'
```

#### Parámetros
- `nombresJugadores` (required): Arreglo de cadenas con los nombres de los jugadores
  - Mínimo 2 jugadores
  - Máximo 6 jugadores
  - Cada nombre debe tener entre 3 y 15 caracteres
  - No puede ser nombres prohibidos: "popo", "perro", "loca", "estupido", "jorge nitales", "benito camelo"

#### Respuesta exitosa (200)
```json
{
  "idPartida": 1,
  "estadoPartida": "EN_PROGRESO",
  "cartasRestantesMazo": 110,
  "jugadorActualIndex": 0,
  "nombreJugadorActual": "Jugador 1",
  "mensajeEvento": "Partida creada exitosamente.",
  "accionesPermitidas": [
    "SACAR_CARTA",
    "PLANTARSE",
    "GUARDAR_PELUSAS",
    "ROBAR"
  ],
  "jugadores": [
    {
      "id": 1,
      "nombre": "Jugador 1",
      "cartasDelante": [],
      "puntosTotales": 0
    },
    {
      "id": 2,
      "nombre": "Jugador 2",
      "cartasDelante": [],
      "puntosTotales": 0
    }
  ]
}
```

#### Respuestas de error
- **400** (cantidad de jugadores inválida):
  ```json
  {"accionesPermitidas":null,"cartasRestantesMazo":0,"estadoPartida":null,"idPartida":null,"jugadorActualIndex":0,"jugadores":null,"mensajeEvento":"La partida debe tener entre 2 y 6 jugadores.","nombreJugadorActual":null}
  ```
- **400** (nombre inválido - muy corto o largo):
  ```json
  {"accionesPermitidas":null,"cartasRestantesMazo":0,"estadoPartida":null,"idPartida":null,"jugadorActualIndex":0,"jugadores":null,"mensajeEvento":"El nombre 'Jo' tiene menos de 3 caracteres.","nombreJugadorActual":null}
  ```
- **400** (nombre prohibido):
  ```json
  {"accionesPermitidas":null,"cartasRestantesMazo":0,"estadoPartida":null,"idPartida":null,"jugadorActualIndex":0,"jugadores":null,"mensajeEvento":"El nombre 'popo' no está permitido.","nombreJugadorActual":null}
  ```

---

### 2. Obtener estado actual de una partida
**Método:** GET  
**URL:** `/api/partidas/{id}`

#### Descripción
Retorna el estado actual de una partida específica.

#### Solicitud
```bash
curl -X GET http://localhost:8080/api/partidas/1
```

#### Respuesta exitosa (200)
```json
{
  "idPartida": 1,
  "estadoPartida": "EN_PROGRESO",
  "cartasRestantesMazo": 109,
  "jugadorActualIndex": 0,
  "nombreJugadorActual": "Jugador 1",
  "mensajeEvento": "Estado actual de la partida.",
  "accionesPermitidas": [
    "SACAR_CARTA",
    "PLANTARSE",
    "GUARDAR_PELUSAS",
    "ROBAR"
  ],
  "jugadores": [...]
}
```

#### Errores
- **500** - Partida no existe (el servicio lanza `IllegalArgumentException` sin manejador global):
  ```json
  {"timestamp":"...","status":500,"error":"Internal Server Error","path":"/api/partidas/999999"}
  ```
- **400** - ID no numérico:
  ```json
  {"timestamp":"2026-11-10T...","status":400,"message":"Failed to convert value of type 'java.lang.String' to required type 'java.lang.Long'", ...}
  ```

---

### 3. Guardar pelusas
**Método:** POST  
**URL:** `/api/partidas/{id}/guardar`

#### Descripción
Guarda las cartas del jugador actual en el pozo de puntos seguro. Solo permitido al inicio del turno, antes de sacar una carta.

#### Solicitud
```bash
curl -X POST http://localhost:8080/api/partidas/1/guardar
```

#### Respuesta exitosa (200)
```json
{
  "idPartida": 1,
  "estadoPartida": "EN_PROGRESO",
  "cartasRestantesMazo": 109,
  "jugadorActualIndex": 0,
  "nombreJugadorActual": "Jugador 1",
  "mensajeEvento": "Pelusas guardadas en el pozo seguro.",
  "accionesPermitidas": [...],
  "jugadores": [...]
}
```

#### Errores (400)
- Si la partida no está en progreso:
  ```json
  {"mensajeEvento":"La partida no está en curso.","...}
  ```
- Si el jugador no tiene cartas delante:
  ```json
  {"mensajeEvento":"No tienes cartas delante para guardar.","...}
  ```
- Si no es el inicio del turno:
  ```json
  {"mensajeEvento":"Solo puedes guardar al inicio del turno, antes de sacar una carta.","...}
  ```

---

### 4. Sacar una carta
**Método:** POST  
**URL:** `/api/partidas/{id}/sacar`

#### Descripción
Saca una carta del mazo. Aplica la regla de pérdida de turno si el jugador tiene ≥3 cartas en mesa y la carta repetida un valor existente.

#### Solicitud
```bash
curl -X POST http://localhost:8080/api/partidas/1/sacar
```

#### Respuesta exitosa (200) - Jugador normal
```json
{
  "idPartida": 1,
  "estadoPartida": "EN_PROGRESO",
  "cartasRestantesMazo": 109,
  "jugadorActualIndex": 0,
  "nombreJugadorActual": "Jugador 1",
  "mensajeEvento": "Carta sacada del mazo.",
  "accionesPermitidas": [...],
  "jugadores": [...]
}
```

#### Respuesta exitosa (200) - Pérdida de turno
```json
{
  "idPartida": 1,
  "estadoPartida": "EN_PROGRESO",
  "cartasRestantesMazo": 108,
  "jugadorActualIndex": 1,
  "nombreJugadorActual": "Jugador 2",
  "mensajeEvento": "Carta sacada del mazo.",
  "accionesPermitidas": [...],
  "jugadores": [...]
}
```

#### Comportamiento especial y errores
- **200** - Si el mazo se agota, la respuesta trae `"estadoPartida":"FINALIZADA"` y se registra la partida en el histórico.
- **500** - Partida ya finalizada o inexistente (excepción sin manejador global).

---

### 5. Robar cartas
**Método:** POST  
**URL:** `/api/partidas/{id}/robar`

#### Descripción
Roba cartas de igual valor de los oponentes. Solo permitido después de sacar una carta.

#### Solicitud
```bash
curl -X POST http://localhost:8080/api/partidas/1/robar
```

#### Respuesta exitosa (200)
```json
{
  "idPartida": 1,
  "estadoPartida": "EN_PROGRESO",
  "cartasRestantesMazo": 108,
  "jugadorActualIndex": 0,
  "nombreJugadorActual": "Jugador 1",
  "mensajeEvento": "Cartas robadas a rivales con éxito.",
  "accionesPermitidas": [...],
  "jugadores": [...]
}
```

#### Errores (400)
- **400** - Debe sacar una carta primero:
  ```json
  {"mensajeEvento":"Debes sacar una carta primero para poder robar.","...}
  ```
- **400** - No hay cartas de ese valor para robar:
  ```json
  {"mensajeEvento":"No hay cartas de valor X para robar a ningún oponente.","...}
  ```

---

### 6. Plantarse
**Método:** POST  
**URL:** `/api/partidas/{id}/plantarse`

#### Descripción
Finaliza el turno del jugador actual y pasa al siguiente.

#### Solicitud
```bash
curl -X POST http://localhost:8080/api/partidas/1/plantarse
```

#### Respuesta exitosa (200)
```json
{
  "idPartida": 1,
  "estadoPartida": "EN_PROGRESO",
  "cartasRestantesMazo": 109,
  "jugadorActualIndex": 1,
  "nombreJugadorActual": "Jugador 2",
  "mensajeEvento": "Jugador se ha plantado. Cambiando de turno.",
  "accionesPermitidas": [...],
  "jugadores": [...]
}
```

#### Errores (400)
- **400** - Si el jugador no tiene cartas delante:
  ```json
  {"mensajeEvento":"Debe sacar al menos una carta antes de poder plantarse.","...}
  ```
- **500** - Partida inexistente.

---

### 7. Obtener historial de partidas
**Método:** GET  
**URL:** `/api/partidas/historico`

#### Descripción
Retorna el historial de todas las partidas finalizadas.

#### Solicitud
```bash
curl -X GET http://localhost:8080/api/partidas/historico
```

#### Respuesta exitosa (200)
```json
[
  {
    "id": 1,
    "partidaId": "1",
    "fechaFin": "2026-11-10T14:30:00",
    "ganadorNombre": "Jugador 1",
    "ganadorPuntaje": 42,
    "resumenJugadores": "Jugador 1 (42 pts - Cartas: [10, 8, 5, 2, 1])"
  }
]
```

---

## Flujo completo de prueba (copiar y pegar)

```bash
# 1. Crear partida (anotar idPartida de la respuesta)
curl -X POST http://localhost:8080/api/partidas -H "Content-Type: application/json" -d '["Ana","Beto"]'
# 2. Sacar carta
curl -X POST http://localhost:8080/api/partidas/1/sacar
# 3. Robar (400 si ningun rival tiene ese valor)
curl -X POST http://localhost:8080/api/partidas/1/robar
# 4. Plantarse
curl -X POST http://localhost:8080/api/partidas/1/plantarse
# 5. Guardar pelusas (turno siguiente, antes de sacar)
curl -X POST http://localhost:8080/api/partidas/1/guardar
# 6. Consultar estado e historial
curl http://localhost:8080/api/partidas/1
curl http://localhost:8080/api/partidas/historico
```
