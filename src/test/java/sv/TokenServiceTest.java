package sv;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Testes unitários para a classe TokenService.
 */
class TokenServiceTest {

    private TokenService tokenService;

    @BeforeEach
    void setUp() {
        tokenService = new TokenService();
    }

    @Test
    void testEmitirToken() {
        UUID token = tokenService.emitirToken();
        assertNotNull(token);
        assertTrue(tokenService.validarToken(token));
    }

    @Test
    void testValidarTokenInvalido() {
        UUID token = UUID.randomUUID();
        assertFalse(tokenService.validarToken(token));
    }

    @Test
    void testTokenNaoPodeSerUsadoDuasVezes() {
        UUID token = tokenService.emitirToken();
        assertTrue(tokenService.validarToken(token));
        assertFalse(tokenService.validarToken(token));
    }
}