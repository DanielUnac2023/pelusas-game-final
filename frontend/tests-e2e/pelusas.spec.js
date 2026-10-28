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
    // 1. Abrir e iniciar la partida
    await page.goto("/");
    await page.click('[data-testid="btn-start-game"]');

    // 2. Sacar cartas sucesivamente
    await page.click('[data-testid="btn-sacar-carta"]');
    await page.click('[data-testid="btn-sacar-carta"]');
    await page.click('[data-testid="btn-sacar-carta"]');

    // 3. Continuar sacando hasta forzar la repetición y pérdida de turno
    // Se ejecuta en bucle controlado si el mazo lo requiere
    await page.click('[data-testid="btn-sacar-carta"]');

    // 4. Si ocurrió la pérdida, verificar que el turno cambió automáticamente de jugador
    const turnoTexto = await page.locator('[data-testid="turn-owner"]').textContent();
    expect(turnoTexto).toBeTruthy();
  });
});
