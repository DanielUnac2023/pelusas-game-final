<template>
  <div class="min-h-screen bg-[#0a0f1d] text-slate-100 flex flex-col items-center p-4 sm:p-6 font-sans">
    
    <!-- Contenedor Principal acorde al diseño del Setup -->
    <div class="w-full max-w-4xl bg-[#111a2e] border border-slate-700/60 rounded-2xl p-6 sm:p-8 shadow-2xl backdrop-blur-sm mt-4 sm:mt-8">
      
      <!-- Encabezado con badge y título estilizado -->
      <div class="text-center mb-8">
        <span class="inline-block px-3 py-1 text-xs font-semibold tracking-wider text-amber-400 uppercase bg-amber-500/10 border border-amber-500/30 rounded-full mb-3">
          Estadísticas & Resultados
        </span>
        <h1 class="text-3xl sm:text-4xl font-extrabold tracking-tight text-white flex items-center justify-center gap-2">
          <span>🏆</span> Historial de Partidas
        </h1>
        <p class="text-slate-400 text-sm mt-1">Registro permanente de partidas finalizadas en Pelusas</p>
      </div>

      <!-- Estado: Cargando -->
      <div v-if="loading" class="flex flex-col items-center justify-center py-12 text-slate-400 gap-3">
        <div class="w-8 h-8 border-4 border-amber-400 border-t-transparent rounded-full animate-spin"></div>
        <p class="text-sm font-medium">Cargando historial...</p>
      </div>

      <!-- Estado: Error -->
      <div v-else-if="error" class="bg-red-500/10 border border-red-500/30 text-red-400 p-4 rounded-xl text-center text-sm my-6">
        ⚠️ {{ error }}
      </div>

      <!-- Tabla de Partidas -->
      <div v-else-if="historico.length > 0" class="overflow-x-auto rounded-xl border border-slate-700/50 bg-[#0d1424]">
        <table class="w-full text-left text-sm border-collapse">
          <thead>
            <tr class="bg-[#162238] border-b border-slate-700/70 text-slate-300 uppercase text-xs tracking-wider">
              <th class="py-3.5 px-4 font-semibold text-center w-24">ID</th>
              <th class="py-3.5 px-4 font-semibold">Fecha / Hora</th>
              <th class="py-3.5 px-4 font-semibold">Ganador</th>
              <th class="py-3.5 px-4 font-semibold text-center">Puntaje</th>
              <th class="py-3.5 px-4 font-semibold">Resumen de Jugadores</th>
            </tr>
          </thead>
          <tbody class="divide-y divide-slate-800">
            <tr 
              v-for="item in historico" 
              :key="item.id || item.partidaId"
              class="hover:bg-slate-800/50 transition-colors"
            >
              <td class="py-3.5 px-4 font-mono text-slate-400 text-center font-bold">
                #{{ item.partidaId }}
              </td>
              <td class="py-3.5 px-4 text-slate-300 whitespace-nowrap">
                {{ formatearFecha(item.fechaFin) }}
              </td>
              <td class="py-3.5 px-4 font-semibold text-amber-400 flex items-center gap-1.5 whitespace-nowrap">
                <span>🥇</span> {{ item.ganadorNombre }}
              </td>
              <td class="py-3.5 px-4 text-center whitespace-nowrap">
                <span class="inline-block bg-amber-500/10 text-amber-300 font-bold px-2.5 py-1 rounded-md border border-amber-500/20">
                  {{ item.ganadorPuntaje }} pts
                </span>
              </td>
              <td class="py-3.5 px-4 text-slate-300 text-xs sm:text-sm">
                {{ item.resumenJugadores }}
              </td>
            </tr>
          </tbody>
        </table>
      </div>

      <!-- Estado: Sin partidas -->
      <div v-else class="text-center py-12 text-slate-500 bg-[#0d1424] rounded-xl border border-slate-800">
        <p class="text-base font-medium">No hay partidas registradas en el historial.</p>
        <p class="text-xs text-slate-600 mt-1">Completa una partida para ver los resultados aquí.</p>
      </div>

      <!-- Botón para volver al Inicio / Setup -->
      <div class="mt-8 flex justify-center">
        <router-link 
          to="/" 
          class="w-full sm:w-auto px-6 py-3 bg-slate-800 hover:bg-slate-700 text-slate-200 font-medium rounded-xl border border-slate-700 transition-all text-center flex items-center justify-center gap-2 shadow-lg"
        >
          <span>←</span> Volver a Configurar Partida
        </router-link>
      </div>

    </div>
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