import { defineConfig, devices } from "@playwright/test";

export default defineConfig({
  testDir: "./tests-e2e",
  fullyParallel: true,
  use: {
    // URL base de tu frontend en Vue 3
    baseURL: "http://localhost:5173",
    trace: "on-first-retry",
  },

  // Inicia automáticamente el servidor de desarrollo de Vite si no está prendido
  webServer: {
    command: "npm run dev",
    url: "http://localhost:5173",
    reuseExistingServer: !process.env.CI,
    timeout: 120 * 1000,
  },

  projects: [
    {
      name: "chromium",
      use: { ...devices["Desktop Chrome"] },
    },
  ],
});
