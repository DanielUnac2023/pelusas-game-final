# Pelusas Digital - Proyecto Final

Aplicación web interactiva para jugar una versión digital del juego de mesa **Pelusas**, enfocada en la implementación estricta de reglas de negocio en el backend y la automatización de pruebas a múltiples niveles.

---

## 👥 Integrantes

- **Nombre Integrante 1** - _Rol / Correo_
- **Nombre Integrante 2** - _Rol / Correo_

---

## 🛠️ Tecnologías Utilizadas

- **Backend:** Java 17+, Spring Boot, Spring Data JPA, REST API.
- **Frontend:** Vue 3, Vite, Tailwind CSS.
- **Base de Datos:** H2 Database (Desarrollo/Pruebas) / PostgreSQL (Producción).
- **Pruebas Backend:** JUnit 5, Mockito, Pitest (Pruebas de Mutación).
- **Pruebas Frontend/E2E:** Playwright (Pruebas Funcionales con JavaScript).

---

## 🏗️ Arquitectura

El sistema utiliza una **Arquitectura Cliente-Servidor (Cliente Delgado)**:

- **Frontend (Vue 3):** Encargado de la presentación de la interfaz de usuario y la captura de eventos. No contiene lógica de negocio.
- **Backend (Spring Boot):** Contiene y valida todas las reglas del juego (conteo de mazo, validación de turnos, pérdida de turno, robo y persistencia).
- **API REST:** Comunicación desacoplada mediante intercambio de JSONs con métodos HTTP adecuados.

---

## 📋 Requisitos Previos

- **Java JDK:** 17 o superior.
- **Node.js:** v18.0.0 o superior.
- **Maven:** 3.8+ (o utilizar el wrapper `./mvnw`).

---

## 🚀 Instalación y Configuración

### 1. Clonar el repositorio

```bash
git clone [https://github.com/DanielUnac2023/pelusas-game-final.git]
cd pelusas-game
```
