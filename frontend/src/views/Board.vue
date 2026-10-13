<template>
  <div class="min-h-screen bg-slate-950 text-white p-6 flex flex-col items-center justify-center">
    <h1 class="text-3xl font-black text-amber-400 mb-2">TABLERO DE JUEGO</h1>
    <p class="text-slate-400 text-sm mb-6">Partida ID: {{ $route.params.id }}</p>
    <router-link to="/" class="px-4 py-2 bg-slate-800 border border-slate-700 rounded-xl text-xs hover:bg-slate-700 transition-colors">
      ← Volver a Configuración
    </router-link>
  </div>
</template>
