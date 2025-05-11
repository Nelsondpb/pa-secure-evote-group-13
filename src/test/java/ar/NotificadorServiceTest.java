package ar;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Testes unitários para a classe NotificadorService.
 */
class NotificadorServiceTest {

    /**
     * Verifica se o método de notificação executa sem exceções.
     */
    @Test
    void testNotificarRevogacao() {
        NotificadorService notificador = new NotificadorService();
        assertDoesNotThrow(() -> notificador.notificarRevogacao("eleitor123"));
    }
}
