import { test, expect } from "@playwright/test";

test.describe("Pruebas Funcionales Obligatorias - Pelusas", () => {
  test("Caso 1: Turno Exitoso", async ({ page }) => {
    // 1. Abrir la aplicación
    await page.goto("/");

    // 2. Configurar la partida con 2 jugadores
    await page.fill('[data-testid="input-player-0"]', "Jugador 1");
    await page.fill('[data-testid="input-player-1"]', "Jugador 2");

    // 3. Iniciar el juego
    await page.click('[data-testid="btn-start-game"]');

    // Verificar que estamos en la pantalla del tablero
    await expect(page.locator('[data-testid="turn-owner"]')).toContainText("Turno actual: Jugador 1");

    // 4. Realizar un turno: Sacar carta
    await page.click('[data-testid="btn-sacar-carta"]');

    // Verificar que aparece al menos 1 carta en la mesa
    const cartas = page.locator('[data-testid="carta-item"]');
    await expect(cartas).toHaveCount(1);

    // 5. Finalizar voluntariamente el turno: Plantarse
    await page.click('[data-testid="btn-plantarse"]');

    // 6. Verificar que el turno pasa al Jugador 2
    await expect(page.locator('[data-testid="turn-owner"]')).toContainText("Turno actual: Jugador 2");
  });

  test("Caso 2: Turno Perdido por carta repetida con >= 3 cartas", async ({ page }) => {
    const MAX_INTENTOS = 40;

    // 1. Abrir e iniciar la partida (nombres por defecto: Jugador 1 y Jugador 2)
    await page.goto("/");
    await page.click('[data-testid="btn-start-game"]');
    await expect(page.locator('[data-testid="turn-owner"]')).toContainText("Turno actual: Jugador 1");

    const cartas = page.locator('[data-testid="carta-item"]');
    let cartasEnMesa = 0;
    let perdioTurno = false;

    // 2. Sacar cartas hasta que el backend dé la pérdida de turno
    for (let i = 0; i < MAX_INTENTOS; i++) {
      const [respuesta] = await Promise.all([
        page.waitForResponse((r) => r.url().includes("/sacar")),
        page.click('[data-testid="btn-sacar-carta"]'),
      ]);
      const body = await respuesta.json();

      // Si la partida terminó sin pérdida, el escenario no se pudo reproducir
      expect(body.estadoPartida, "La partida terminó antes de perder el turno").toBe("EN_PROGRESO");

      if (body.jugadorActualIndex !== 0) {
        perdioTurno = true;   // el turno pasó al siguiente jugador
        break;
      }

      cartasEnMesa++;
      // Sincroniza con la UI y comprueba que la carta se agregó
      await expect(cartas).toHaveCount(cartasEnMesa);
    }

    // 3. Verificaciones de la regla de pérdida
    expect(perdioTurno, "No se produjo pérdida de turno en el máximo de intentos").toBe(true);
    expect(cartasEnMesa).toBeGreaterThanOrEqual(3);        // solo se pierde con 3 o más cartas

    await expect(page.locator('[data-testid="turn-owner"]')).toContainText("Turno actual: Jugador 2");
    await expect(cartas).toHaveCount(0);                   // las cartas se descartaron
  });
});
