import { createRouter, createWebHistory } from "vue-router";
import GameSetup from "../views/GameSetup.vue";
import Board from "../views/Board.vue";
import History from "../views/History.vue";

const routes = [
  { path: "/", name: "Setup", component: GameSetup },
  { path: "/game/:id", name: "Board", component: Board, props: true },
  { path: "/history", name: "History", component: History },
];

const router = createRouter({
  history: createWebHistory(),
  routes,
});

export default router;
