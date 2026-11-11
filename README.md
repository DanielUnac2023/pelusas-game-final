# Pelusas Digital - Proyecto Final

Aplicación web interactiva para jugar una versión digital del juego de mesa **Pelusas**, con las reglas de negocio implementadas en el backend y automatización de pruebas a múltiples niveles.

---

## 👥 Integrantes

- **Daniel** (DanielUnac2023) - _Desarrollador_ - desarrollador3@unioncolombianadelsur.org
- **Integrante 2** - _Rol / Correo_ (completar)

> Completar con los nombres, roles y correos reales de todos los integrantes del equipo.

---

## 🛠️ Tecnologías

- **Backend:** Java 21, Spring Boot 4.1, Spring Data JPA (Hibernate), API REST, Lombok.
- **Frontend:** Vue 3, Vue Router, Vite, Tailwind CSS.
- **Base de datos:** H2 en memoria (desarrollo y pruebas); configurable a PostgreSQL.
- **Pruebas backend:** JUnit 5, Mockito, Spring MockMvc, AssertJ.
- **Cobertura:** JaCoCo (con regla de 100 % de líneas y ramas en `verify`).
- **Mutación:** PIT / Pitest (umbral mínimo 90 %).
- **Pruebas funcionales:** Playwright (JavaScript).
- **CI:** GitHub Actions (`.github/workflows/ci.yml`).

---

## 🏗️ Arquitectura

Cliente-servidor con **cliente delgado**:

```
Vue 3 (views + services/api.js)  --HTTP/JSON-->  GameController
                                                      |
                                                  GameService  (reglas del juego)
                                                      |
                                            Repositories (Spring Data JPA)
                                                      |
                                                 H2 / PostgreSQL
```

- **Frontend** (`frontend/`): presentación y captura de eventos. Sin lógica de negocio.
- **Backend** (`backend/`), paquete `edu.unac.backend`:
  - `controller`: endpoints REST bajo `/api/partidas`.
  - `service`: reglas (turnos, pérdida de turno, robo, guardado, desempate, histórico).
  - `model`: entidades JPA (`Partida`, `Jugador`, `Mazo`, `Carta`, `PartidaHistorico`).
  - `repository`: interfaces Spring Data JPA.
  - `dto`: objetos de transferencia consumidos por el frontend.
- **Documentación de la API:** [`CURL_API.md`](CURL_API.md).

### Configuración de JPA y base de datos

Definida en `backend/src/main/resources/application.properties`: H2 en memoria (`jdbc:h2:mem:pelusasdb`), `spring.jpa.hibernate.ddl-auto=update` y consola H2 en `http://localhost:8080/h2-console` (usuario `sa`, sin contraseña). Para usar PostgreSQL, agregar la dependencia del driver y cambiar `spring.datasource.*` y `spring.jpa.hibernate.ddl-auto`.

---

## 📋 Requisitos

- **JDK 21** o superior.
- **Maven 3.8+** (o el wrapper incluido `mvnw`).
- **Node.js** `^22.18.0` o `>=24.12.0` (ver `engines` en `frontend/package.json`) y npm.
- Navegador Chromium para Playwright (se instala con el comando de abajo).

---

## 🚀 Instalación

```bash
git clone https://github.com/DanielUnac2023/pelusas-game-final.git
cd pelusas-game-final

# Frontend
cd frontend
npm install
npx playwright install chromium
cd ..
```

El backend descarga sus dependencias automáticamente con Maven.

## ⚙️ Configuración

- Backend: `backend/src/main/resources/application.properties` (puerto `8080`, base de datos y JPA).
- Frontend: la URL del backend está en `frontend/src/services/api.js` (`http://localhost:8080/api/partidas`).
- Playwright: `frontend/playwright.config.js` (usa `http://localhost:5173` y levanta Vite automáticamente).

## ▶️ Ejecución

```bash
# Terminal 1 - Backend (http://localhost:8080)
cd backend
./mvnw spring-boot:run        # Windows: mvnw.cmd spring-boot:run

# Terminal 2 - Frontend (http://localhost:5173)
cd frontend
npm run dev
```

---

## 🧪 Pruebas

Todas las pruebas del backend viven en `backend/src/test/java/edu/unac/backend/`.

### Pruebas unitarias

Modelos y servicio con Mockito (`model/*Test`, `service/GameService*Test`, `ModelosSimplesTest`):

```bash
cd backend
./mvnw test
```

### Pruebas de integración

`GameControllerTest` y `BackendApplicationTests` levantan el contexto de Spring completo con JPA/H2 y consumen los endpoints REST con MockMvc. Se ejecutan junto con las unitarias:

```bash
cd backend
./mvnw test -Dtest=GameControllerTest,BackendApplicationTests
```

### Cobertura (JaCoCo, exigencia 100 %)

```bash
cd backend
./mvnw clean verify
```

Genera el reporte en `backend/target/site/jacoco/index.html` y **falla el build** si la cobertura de líneas o ramas baja del 100 %. Una copia de la evidencia está en [`reports/jacoco/`](reports/jacoco/index.html).

### Pruebas de mutación (Pitest, exigencia ≥ 90 %)

```bash
cd backend
./mvnw pitest:mutationCoverage
```

Reporte en `backend/target/pit-reports/<fecha>/index.html`; falla si el mutation score es menor a 90 %. Copia de la evidencia en [`reports/`](reports/index.html).

### Pruebas funcionales (Playwright)

Requiere el **backend corriendo** en el puerto 8080 (Playwright levanta el frontend solo):

```bash
cd frontend
npm run test:e2e          # modo headless
npm run test:e2e:ui       # modo interactivo
```

Archivos de prueba: `frontend/tests-e2e/pelusas.spec.js`.

---

## 📊 Resultados (última ejecución)

| Métrica | Resultado | Meta |
|---|---|---|
| Pruebas backend | 100 % en verde | Todas |
| Cobertura de líneas | 100 % (353/353) | 100 % |
| Cobertura de ramas | 100 % (96/96) | 100 % |
| Mutation score (Pitest) | 98 % (159/163) | ≥ 90 % |

## 📁 Entregables

| Entregable | Ubicación |
|---|---|
| Backend / Frontend | `backend/`, `frontend/` |
| Pruebas unitarias e integración | `backend/src/test/` |
| Pruebas funcionales | `frontend/tests-e2e/` |
| Configuración JPA / BD | `backend/src/main/resources/application.properties` |
| CURL de la API | `CURL_API.md` |
| Reporte de cobertura | `reports/jacoco/` |
| Reporte de mutación | `reports/index.html` |
