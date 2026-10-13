<script setup>
import { ref, computed } from "vue";
import { useRouter } from "vue-router";

const router = useRouter();

const jugadores = ref([
  { id: 1, nombre: "Jugador 1" },
  { id: 2, nombre: "Jugador 2" },
]);

const jugadorInicialId = ref(1);

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
  return jugadores.value.length >= 2 && jugadores.value.length <= 6 && jugadores.value.every((j) => j.nombre.trim() !== "");
});

const crearEIniciarPartida = () => {
  if (!esValido.value) return;
  router.push("/game/101");
};
</script>

<template>
  <!-- Contenedor Centrado de Pantalla Completa -->
  <main class="min-h-screen flex items-center justify-center p-4">
    <!-- Tarjeta Principal -->
    <div class="bg-slate-900 p-8 rounded-3xl border border-slate-800">
      <!-- Encabezado -->
      <header class="text-center pb-10">
        <span
          class="inline-block bg-amber-500/10 text-amber-400 border border-amber-500/30 text-xs font-bold px-3 py-1 rounded-full uppercase"
        >
          Juego de Mesa Digital
        </span>
        <h1 class="text-4xl font-black text-white">PELUSAS</h1>
        <p class="text-slate-400 text-sm">Configuración de la partida</p>
      </header>

      <!-- Lista de Jugadores -->
      <section class="flex flex-col gap-4">
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
          <span
            class="w-6 h-6 rounded-lg bg-slate-700 text-xs font-bold text-slate-300 flex items-center justify-center shrink-0"
          >
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
          :disabled="!esValido"
          data-testid="btn-start-game"
          class="w-full py-4 bg-amber-400 hover:bg-amber-300 disabled:bg-slate-800 disabled:text-slate-600 text-slate-950 font-black rounded-2xl transition-colors uppercase text-xs cursor-pointer disabled:cursor-not-allowed"
        >
          Crear e Iniciar Partida
        </button>
      </section>
    </div>
  </main>
</template>
