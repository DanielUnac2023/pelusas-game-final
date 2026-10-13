import { createRouter, createWebHistory } from "vue-router";
import GameSetup from "../views/GameSetup.vue";
import GameBoard from "../views/Board.vue";

const routes = [
  {
    path: "/",
    name: "Setup",
    component: GameSetup,
  },
  {
    path: "/game/:id",
    name: "Game",
    component: GameBoard,
  },
];

const router = createRouter({
  history: createWebHistory(),
  routes,
});

export default router;
