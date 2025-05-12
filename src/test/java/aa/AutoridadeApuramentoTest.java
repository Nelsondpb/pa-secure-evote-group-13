package aa;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.util.Collections;
import java.util.List;

class AutoridadeApuramentoTest {

    private KeyPair generateRSAKeyPair() throws Exception {
        KeyPairGenerator keyGen = KeyPairGenerator.getInstance("RSA");
        keyGen.initialize(2048);
        return keyGen.generateKeyPair();
    }

    @Test
    void testApurarVotos() {
        // Preparar
        KeyPair keys;
        try {
            keys = generateRSAKeyPair();
        } catch (Exception e) {
            fail("Falha ao gerar par de chaves RSA: " + e.getMessage());
            return;
        }
        AutoridadeApuramento aa = new AutoridadeApuramento(keys.getPrivate(), keys.getPublic());

        // Executar
        List<String> votos = List.of("Alice", "Bob", "Alice", "Charlie");
        var resultados = aa.apurarVotos(votos);

        // Verificar
        assertEquals(2, resultados.get("Alice"));
        assertEquals(1, resultados.get("Bob"));
        assertEquals(1, resultados.get("Charlie"));
    }

    @Test
    void testGerarRelatorioOrdenado() {
        KeyPair keys;
        try {
            keys = generateRSAKeyPair();
        } catch (Exception e) {
            fail("Falha ao gerar par de chaves RSA: " + e.getMessage());
            return;
        }
        AutoridadeApuramento aa = new AutoridadeApuramento(keys.getPrivate(), keys.getPublic());

        aa.apurarVotos(List.of("X", "Y", "X", "Z", "Y", "X"));
        String relatorio = aa.gerarRelatorio();


        assertTrue(relatorio.startsWith("- X: 3 votos\n- Y: 2 votos\n- Z: 1 votos\n"));

        assertTrue(relatorio.contains("=== RELATÓRIO DE APURAMENTO ==="));
        assertTrue(relatorio.contains("Total de votos apurados: 6"));

        assertTrue(relatorio.endsWith("=== FIM DO RELATÓRIO ==="));
    }

    @Test
    void testGetChavePublicaAA() {
        KeyPair keys;
        try {
            keys = generateRSAKeyPair();
        } catch (Exception e) {
            fail("Falha ao gerar par de chaves RSA: " + e.getMessage());
            return;
        }
        AutoridadeApuramento aa = new AutoridadeApuramento(keys.getPrivate(), keys.getPublic());

        PublicKey pub = aa.getChavePublicaAA();
        assertSame(keys.getPublic(), pub);
    }

    @Test
    void testDesencriptarVotosEmpty() throws Exception {
        KeyPair keys = generateRSAKeyPair();
        AutoridadeApuramento aa = new AutoridadeApuramento(keys.getPrivate(), keys.getPublic());

        // Se a lista de votos encriptados for vazia, deve retornar lista vazia sem exceção
        List<String> resultado = aa.desencriptarVotos(Collections.emptyList());
        assertNotNull(resultado);
        assertTrue(resultado.isEmpty());
    }
}
