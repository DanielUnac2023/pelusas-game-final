<script setup>
import { ref, onMounted, computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { GameAPI } from '../services/api.js'

const route = useRoute()
const router = useRouter()

const partidaId = route.params.id
const gameState = ref(null)
const cargando = ref(true)
const errorMensaje = ref('')

// Cargar estado inicial de la partida desde Spring Boot
const cargarPartida = async () => {
  try {
    cargando.value = true
    errorMensaje.value = ''
    gameState.value = await GameAPI.obtenerPartida(partidaId)
  } catch (err) {
    errorMensaje.value = 'Error al obtener el estado de la partida desde el backend.'
    console.error(err)
  } finally {
    cargando.value = false
  }
}

// Ejecutar acciones del juego (Sacar, Guardar, Plantarse)
const ejecutarAccion = async (tipoAccion, parametro = null) => {
  try {
    if (tipoAccion === 'sacar') {
      gameState.value = await GameAPI.sacarCarta(partidaId)
    } else if (tipoAccion === 'guardar') {
      gameState.value = await GameAPI.guardarPelusas(partidaId)
    } else if (tipoAccion === 'plantarse') {
      gameState.value = await GameAPI.plantarse(partidaId)
    } else if (tipoAccion === 'robar') {
      gameState.value = await GameAPI.robarCartas(partidaId, parametro)
    }
  } catch (err) {
    console.error(`Error ejecutando acción ${tipoAccion}:`, err)
  }
}

// Saber si la partida terminó
const esFinalizada = computed(() => {
  return gameState.value && gameState.value.estadoPartida === 'FINALIZADA'
})

onMounted(() => {
  cargarPartida()
})
</script>

<template>
  <main class="min-h-screen bg-slate-950 text-slate-100 font-sans p-6">
    
    <!-- Pantalla de Carga -->
    <div v-if="cargando" class="flex flex-col items-center justify-center min-h-[60vh]">
      <div class="text-amber-400 font-bold text-xl animate-pulse">Cargando tablero...</div>
    </div>

    <!-- Error de Conexión -->
    <div v-else-if="errorMensaje" class="max-w-md mx-auto my-10 bg-red-900/50 border border-red-500 p-6 rounded-2xl text-center">
      <p class="text-red-200 font-bold mb-4">{{ errorMensaje }}</p>
      <button @click="router.push('/')" class="bg-slate-800 hover:bg-slate-700 text-white font-bold px-4 py-2 rounded-xl text-xs">
        Volver al Inicio
      </button>
    </div>

    <!-- Tablero de Juego Activo -->
    <div v-else-if="gameState" class="max-w-5xl mx-auto space-y-6">
      
      <!-- Encabezado de la Mesa -->
      <header class="bg-slate-900 p-4 rounded-2xl border border-slate-800 flex justify-between items-center shadow-lg">
        <div>
          <span class="text-xs font-bold text-slate-500 uppercase">Partida #{{ gameState.idPartida }}</span>
          <h1 class="text-xl font-black text-amber-400" data-testid="turn-owner">
            Turno actual: {{ gameState.nombreJugadorActual }}
          </h1>
        </div>

        <!-- Info del Mazo -->
        <div class="bg-slate-800 px-4 py-2 rounded-xl border border-slate-700 text-right">
          <span class="text-xs text-slate-400 block font-bold">Mazo Restante</span>
          <span class="text-lg font-black text-emerald-400">{{ gameState.cartasRestantesMazo }} cartas</span>
        </div>
      </header>

      <!-- Panel de Alertas / Eventos del Backend -->
      <div class="bg-slate-900/60 border border-amber-500/20 p-3 rounded-xl text-center text-sm font-semibold text-amber-200">
        📢 {{ gameState.mensajeEvento }}
      </div>

      <!-- Panel de Controles de Acción (Para el Jugador Actual) -->
      <div v-if="!esFinalizada" class="bg-slate-900 p-6 rounded-2xl border border-slate-800 flex flex-wrap gap-4 justify-center">
        
        <button 
          @click="ejecutarAccion('guardar')"
          data-testid="btn-guardar"
          class="bg-emerald-600 hover:bg-emerald-500 font-bold px-6 py-3 rounded-xl transition cursor-pointer text-sm"
        >
          📥 Guardar Pelusas
        </button>

        <button 
          @click="ejecutarAccion('sacar')"
          data-testid="btn-sacar-carta"
          class="bg-amber-400 hover:bg-amber-300 text-slate-950 font-black px-8 py-3 rounded-xl transition cursor-pointer text-sm uppercase shadow-lg shadow-amber-400/10"
        >
          🃏 Sacar Carta
        </button>

        <button 
          @click="ejecutarAccion('plantarse')"
          data-testid="btn-plantarse"
          class="bg-slate-800 hover:bg-slate-700 text-slate-300 font-bold px-6 py-3 rounded-xl border border-slate-700 transition cursor-pointer text-sm"
        >
          🛑 Plantarse
        </button>
      </div>

      <!-- Mensaje si la partida ha finalizado -->
      <div v-else class="bg-emerald-900/40 border border-emerald-500 p-6 rounded-2xl text-center space-y-3">
        <h2 class="text-2xl font-black text-emerald-300">🎉 ¡Partida Finalizada!</h2>
        <p class="text-slate-300 text-sm">Se agotó el mazo. Revisa el conteo final de puntos abajo.</p>
        <button @click="router.push('/')" class="bg-emerald-500 text-slate-950 font-bold px-6 py-2 rounded-xl text-xs uppercase">
          Nueva Partida
        </button>
      </div>

      <!-- Renderizado de las Mesas de Todos los Jugadores (2 a 6) -->
      <section class="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-4">
        <div 
          v-for="jugador in gameState.jugadores" 
          :key="jugador.id"
          class="bg-slate-900 p-5 rounded-2xl border transition-all"
          :class="jugador.nombre === gameState.nombreJugadorActual ? 'border-amber-400/60 ring-1 ring-amber-400/30' : 'border-slate-800'"
        >
          <!-- Nombre y Puntuación Acumulada -->
          <div class="flex justify-between items-center mb-3">
            <span class="font-bold text-slate-200">{{ jugador.nombre }}</span>
            <span class="bg-amber-500/10 text-amber-400 text-xs px-2.5 py-1 rounded-lg font-black border border-amber-500/20">
              {{ jugador.puntosTotales }} Pts
            </span>
          </div>

          <!-- Cartas expuestas en mesa de este jugador (Cartas Delante) -->
          <div class="space-y-1">
            <span class="text-xs text-slate-500 font-bold uppercase">Cartas en mesa:</span>
            <div class="flex flex-wrap gap-2 min-h-[40px] items-center">
              <span 
                v-for="(carta, idx) in jugador.cartasDelante" 
                :key="idx"
                data-testid="carta-item"
                class="bg-indigo-600 text-white font-black w-8 h-10 rounded-lg flex items-center justify-center text-base shadow"
              >
                {{ carta.valor }}
              </span>
              <span v-if="!jugador.cartasDelante || jugador.cartasDelante.length === 0" class="text-xs italic text-slate-600">
                Sin cartas
              </span>
            </div>
          </div>
        </div>
      </section>

    </div>
  </main>
</template>