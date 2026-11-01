<template>
  <div class="historico-container">
    <h1>🏆 Historial de Partidas - Pelusas</h1>
    
    <div v-if="loading" class="loading">Cargando historial...</div>
    <div v-else-if="error" class="error">{{ error }}</div>
    
    <table v-else-if="historico.length > 0" class="tabla-historico">
      <thead>
        <tr>
          <th>ID Partida</th>
          <th>Fecha / Hora</th>
          <th>Ganador</th>
          <th>Puntaje</th>
          <th>Resumen de Jugadores</th>
        </tr>
      </thead>
      <tbody>
        <tr v-for="item in historico" :key="item.id || item.partidaId">
          <td>{{ item.partidaId }}</td>
          <td>{{ formatearFecha(item.fechaFin) }}</td>
          <td class="ganador">🥇 {{ item.ganadorNombre }}</td>
          <td><strong>{{ item.ganadorPuntaje }} pts</strong></td>
          <td>{{ item.resumenJugadores }}</td>
        </tr>
      </tbody>
    </table>

    <div v-else class="empty">No hay partidas registradas en el historial.</div>
  </div>
</template>

<script>
export default {
  name: 'History',
  data() {
    return {
      historico: [],
      loading: true,
      error: null
    };
  },
  mounted() {
    this.obtenerHistorial();
  },
  methods: {
    async obtenerHistorial() {
      try {
        const response = await fetch('http://localhost:8080/api/partidas/historico');
        if (!response.ok) throw new Error('Error al conectar con la API de histórico');
        this.historico = await response.json();
      } catch (err) {
        this.error = 'No se pudo cargar el historial. Asegúrate de que el backend esté corriendo.';
        console.error(err);
      } finally {
        this.loading = false;
      }
    },
    formatearFecha(fechaStr) {
      if (!fechaStr) return 'N/A';
      const fecha = new Date(fechaStr);
      return fecha.toLocaleString();
    }
  }
};
</script>

<style scoped>
.historico-container {
  max-width: 900px;
  margin: 30px auto;
  padding: 20px;
  font-family: Arial, sans-serif;
}

h1 {
  text-align: center;
  color: #2c3e50;
  margin-bottom: 25px;
}

.tabla-historico {
  width: 100%;
  border-collapse: collapse;
  box-shadow: 0 4px 6px rgba(0, 0, 0, 0.1);
  border-radius: 8px;
  overflow: hidden;
}

.tabla-historico th {
  background-color: #34495e;
  color: white;
  padding: 12px;
  text-align: left;
}

.tabla-historico td {
  padding: 12px;
  border-bottom: 1px solid #ddd;
}

.tabla-historico tr:hover {
  background-color: #f5f5f5;
}

.ganador {
  color: #27ae60;
  font-weight: bold;
}

.loading, .error, .empty {
  text-align: center;
  margin-top: 20px;
  font-size: 1.1em;
}

.error {
  color: #e74c3c;
}
</style>