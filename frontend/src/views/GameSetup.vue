<script setup>
import { ref, computed } from "vue";
import { useRouter } from "vue-router";
import { GameAPI } from "../services/api.js"; // Conexión directa a Spring Boot

const router = useRouter();

const jugadores = ref([
  { id: 1, nombre: "Jugador 1" },
  { id: 2, nombre: "Jugador 2" },
]);

const jugadorInicialId = ref(1);
const cargando = ref(false);
const errorMensaje = ref("");

const agregarJugador = () => {
  if (jugadores.value.length < 6) {
    jugadores.value.push({
      id: Date.now(),
      nombre: `Jugador ${jugadores.value.length + 1}`,
    });
  }
};

const removerJugador = (id) => {
  if (jugadores.value.length > 2) {
    jugadores.value = jugadores.value.filter((j) => j.id !== id);
    if (jugadorInicialId.value === id) {
      jugadorInicialId.value = jugadores.value[0].id;
    }
  }
};

const esValido = computed(() => {
  return (
    jugadores.value.length >= 2 &&
    jugadores.value.length <= 6 &&
    jugadores.value.every((j) => j.nombre.trim() !== "")
  );
});

// Función de integración REAL con el Backend
const crearEIniciarPartida = async () => {
  if (!esValido.value || cargando.value) return;

  cargando.value = true;
  errorMensaje.value = "";

  try {
    // 1. Extraer el arreglo de nombres de jugadores para el backend
    const nombresJugadores = jugadores.value.map((j) => j.nombre.trim());

    // 2. Invocar la API REST de Spring Boot (@PostMapping /api/partidas)
    const partidaCreada = await GameAPI.crearPartida(nombresJugadores);

    // 3. Redirigir al tablero con el ID real retornado por el backend
    if (partidaCreada && partidaCreada.idPartida) {
      router.push(`/game/${partidaCreada.idPartida}`);
    } else {
      throw new Error("Respuesta inválida del servidor");
    }
  } catch (error) {
    console.error("Error al crear la partida en el backend:", error);
    errorMensaje.value = "No se pudo conectar con el servidor Spring Boot. Asegúrate de que esté encendido en http://localhost:8080.";
  } finally {
    cargando.value = false;
  }
};
</script>

<template>
  <main class="min-h-screen w-full bg-slate-950 text-slate-100 flex items-center justify-center p-4 font-sans">
    
    <div class="w-full max-w-md bg-slate-900 p-8 rounded-3xl border border-slate-800 shadow-xl space-y-6">
      
      <!-- Encabezado -->
      <header class="text-center space-y-2">
        <span class="inline-block bg-amber-500/10 text-amber-400 border border-amber-500/30 text-xs font-bold px-3 py-1 rounded-full uppercase">
          Juego de Mesa Digital
        </span>
        <h1 class="text-4xl font-black text-white">PELUSAS</h1>
        <p class="text-slate-400 text-sm">Configuración de la partida</p>
      </header>

      <!-- Mensaje de Error si el Backend no responde -->
      <div v-if="errorMensaje" class="bg-red-900/50 border border-red-500 text-red-200 text-xs p-3.5 rounded-xl">
        ⚠️ {{ errorMensaje }}
      </div>

      <!-- Lista de Jugadores (2 a 6) -->
      <section class="space-y-3">
        <div class="flex justify-between text-xs font-bold text-slate-400 px-1 uppercase">
          <span>Jugadores ({{ jugadores.length }}/6)</span>
          <span>¿Inicia?</span>
        </div>

        <div
          v-for="(jugador, index) in jugadores"
          :key="jugador.id"
          class="flex items-center gap-3 bg-slate-800/80 p-3 rounded-2xl border border-slate-700 focus-within:border-amber-400"
        >
          <!-- Número de Jugador -->
          <span class="w-6 h-6 rounded-lg bg-slate-700 text-xs font-bold text-slate-300 flex items-center justify-center shrink-0">
            {{ index + 1 }}
          </span>

          <!-- Nombre -->
          <input
            type="text"
            v-model="jugador.nombre"
            placeholder="Nombre del jugador"
            class="bg-transparent flex-1 text-sm font-semibold text-white outline-none placeholder-slate-500"
            :data-testid="`input-player-${index}`"
          />

          <!-- Botón Eliminar -->
          <button
            v-if="jugadores.length > 2"
            @click="removerJugador(jugador.id)"
            title="Eliminar jugador"
            class="text-slate-400 hover:text-red-400 px-2 py-1 text-xs font-bold cursor-pointer"
          >
            ✕
          </button>

          <!-- Radio Iniciar -->
          <input
            type="radio"
            name="jugadorInicial"
            :value="jugador.id"
            v-model="jugadorInicialId"
            class="accent-amber-400 w-5 h-5 cursor-pointer"
            :data-testid="`radio-inicial-${index}`"
          />
        </div>
      </section>

      <!-- Botón Agregar Jugador -->
      <button
        v-if="jugadores.length < 6"
        @click="agregarJugador"
        data-testid="btn-add-player"
        class="w-full py-3 bg-slate-800 hover:bg-slate-700 border border-slate-700 text-slate-300 font-bold rounded-2xl text-xs flex items-center justify-center gap-2 cursor-pointer"
      >
        <span class="text-amber-400 text-base font-black">+</span> Agregar otro jugador
      </button>

      <!-- Nota Informativa -->
      <div class="bg-slate-950/60 p-3.5 rounded-xl border border-slate-800 text-xs text-slate-400 flex items-start gap-2">
        <span class="text-amber-400">📌</span>
        <p>El jugador seleccionado con el botón circular iniciará el primer turno.</p>
      </div>

      <!-- Botón Iniciar Partida -->
      <button
        @click="crearEIniciarPartida"
        :disabled="!esValido || cargando"
        data-testid="btn-start-game"
        class="w-full py-4 bg-amber-400 hover:bg-amber-300 disabled:bg-slate-800 disabled:text-slate-600 text-slate-950 font-black rounded-2xl transition-colors uppercase text-xs cursor-pointer disabled:cursor-not-allowed flex items-center justify-center gap-2"
      >
        <span v-if="cargando">Creando Partida...</span>
        <span v-else>Crear e Iniciar Partida</span>
      </button>

    </div>
  </main>
</template>