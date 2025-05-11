package shared;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Testes unitários para a classe ConfigManager.
 */
class ConfigManagerTest {

    private ConfigManager configManager;

    @BeforeEach
    void setUp() {
        configManager = new ConfigManager();
    }

    @Test
    void testCandidatosCarregados() {
        List<String> candidatos = configManager.getCandidatos();
        assertNotNull(candidatos);
        assertTrue(candidatos.size() > 0);
    }

    @Test
    void testInfoCandidatoExistente() {
        String info = configManager.getInfoCandidato("Elon Musk");
        assertTrue(info.contains("Marte"));
    }

    @Test
    void testInfoCandidatoInexistente() {
        String info = configManager.getInfoCandidato("Candidato Ficticio");
        assertEquals("Sem informação", info);
    }

    @Test
    void testCandidatoValido() {
        assertTrue(configManager.isCandidatoValido("Donald Trump"));
    }

    @Test
    void testCandidatoInvalido() {
        assertFalse(configManager.isCandidatoValido("Fantasma Político"));
    }
}
