// Configuración base de la API REST que apunta a tu backend de Spring Boot
const API_BASE_URL = "http://localhost:8080/api/partidas";

export const GameAPI = {
  // Crear una nueva partida (recibe array de nombres: ["Carlos", "Ana"])
  async crearPartida(nombresJugadores) {
    const response = await fetch(API_BASE_URL, {
      method: "POST",
      headers: { "Content-Type": "application/json" },
      body: JSON.stringify(nombresJugadores),
    });
    return await response.json();
  },

  // Obtener estado actual
  async obtenerPartida(idPartida) {
    const response = await fetch(`${API_BASE_URL}/${idPartida}`);
    return await response.json();
  },

  // Guardar pelusas
  async guardarPelusas(idPartida) {
    const response = await fetch(`${API_BASE_URL}/${idPartida}/guardar`, { method: "POST" });
    return await response.json();
  },

  // Sacar carta
  async sacarCarta(idPartida) {
    const response = await fetch(`${API_BASE_URL}/${idPartida}/sacar`, { method: "POST" });
    return await response.json();
  },

  // Robar cartas
  async robarCartas(idPartida, valorCarta) {
    const response = await fetch(`${API_BASE_URL}/${idPartida}/robar?valorCarta=${valorCarta}`, { method: "POST" });
    return await response.json();
  },

  // Plantarse
  async plantarse(idPartida) {
    const response = await fetch(`${API_BASE_URL}/${idPartida}/plantarse`, { method: "POST" });
    return await response.json();
  },
};
