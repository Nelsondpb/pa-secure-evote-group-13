package ue;

import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Testes unitários para a classe Voto.
 */
class VotoTest {

    @Test
    void testGetVotoEncriptadoReturnsCopy() {
        byte[] original = "votoTest".getBytes();
        UUID token = UUID.randomUUID();
        Voto voto = new Voto(original, token);

        byte[] returned = voto.getVotoEncriptado();
        assertArrayEquals(original, returned);

        // Alterar o array retornado não deve afetar o original
        returned[0] = 0;
        assertNotEquals(returned[0], voto.getVotoEncriptado()[0]);
    }

    @Test
    void testGetToken() {
        UUID token = UUID.randomUUID();
        Voto voto = new Voto("votoTest".getBytes(), token);
        assertEquals(token, voto.getToken());
    }
}