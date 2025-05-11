package shared;

import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Testes unitários para a classe PacoteVoto.
 */
class PacoteVotoTest {

    @Test
    void testCriacaoEPersistenciaPacoteVoto() throws Exception {
        byte[] voto = "voto123".getBytes();
        byte[] chave = "chave456".getBytes();

        PacoteVoto pacote = new PacoteVoto(voto, chave);
        assertNotNull(pacote);
        assertTrue(pacote.verificarIntegridade());

        byte[] serialized = pacote.toByteArray();
        PacoteVoto recuperado = PacoteVoto.fromByteArray(serialized);

        assertNotNull(recuperado);
        assertTrue(recuperado.verificarIntegridade());
        assertArrayEquals(pacote.getVotoEncriptado(), recuperado.getVotoEncriptado());
        assertArrayEquals(pacote.getChaveEncriptada(), recuperado.getChaveEncriptada());
    }

    @Test
    void testVerificacaoDeIntegridadeComDadosAlterados() throws Exception {
        byte[] voto = "votoABC".getBytes();
        byte[] chave = "chaveXYZ".getBytes();

        PacoteVoto pacote = new PacoteVoto(voto, chave);
        byte[] modificado = pacote.toByteArray();

        // Altera os dados serializados para corromper o pacote
        modificado[modificado.length - 1] ^= 1; // inverter 1 bit

        PacoteVoto corrompido = PacoteVoto.fromByteArray(modificado);
        assertFalse(corrompido.verificarIntegridade());
    }

    @Test
    void testSerializacaoNulaDisparaErro() {
        assertThrows(NullPointerException.class, () -> new PacoteVoto(null, null));
    }
}
