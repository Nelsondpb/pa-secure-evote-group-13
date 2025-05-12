package shared;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import javax.crypto.SecretKey;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Testes unitários para a classe CryptoUtils.
 */
class CryptoUtilsTest {

    private KeyPair keyPair;
    private SecretKey aesKey;

    @BeforeEach
    void setUp() throws Exception {
        KeyPairGenerator keyGen = KeyPairGenerator.getInstance("RSA");
        keyGen.initialize(2048);
        keyPair = keyGen.generateKeyPair();

        aesKey = CryptoUtils.generateAESKey();
    }

    @Test
    void testEncryptAndDecryptAES() throws Exception {
        String mensagem = "mensagem secreta";
        byte[] encriptado = CryptoUtils.encryptAES(mensagem.getBytes(), aesKey);
        byte[] desencriptado = CryptoUtils.decryptAES(encriptado, aesKey);

        assertEquals(mensagem, new String(desencriptado));
    }

    @Test
    void testEncryptAndDecryptRSA() throws Exception {
        String mensagem = "chave segura";
        byte[] encriptado = CryptoUtils.encryptRSA(mensagem.getBytes(), keyPair.getPublic());
        byte[] desencriptado = CryptoUtils.decryptRSA(encriptado, keyPair.getPrivate());

        assertEquals(mensagem, new String(desencriptado));
    }

    @Test
    void testDecryptAESWithWrongKeyFails() throws Exception {
        SecretKey outraChave = CryptoUtils.generateAESKey();
        String dados = "teste";
        byte[] encriptado = CryptoUtils.encryptAES(dados.getBytes(), aesKey);

        assertThrows(Exception.class, () -> {
            CryptoUtils.decryptAES(encriptado, outraChave);
        });
    }

    @Test
    void testDecryptRSAWithWrongKeyFails() throws Exception {
        KeyPair outraPar = KeyPairGenerator.getInstance("RSA").generateKeyPair();
        String dados = "teste RSA";
        byte[] encriptado = CryptoUtils.encryptRSA(dados.getBytes(), keyPair.getPublic());

        assertThrows(Exception.class, () -> {
            CryptoUtils.decryptRSA(encriptado, outraPar.getPrivate());
        });
    }

    @Test
    void testGenerateAESKeyNotNull() throws Exception {
        SecretKey key = CryptoUtils.generateAESKey();
        assertNotNull(key);
        assertEquals("AES", key.getAlgorithm());
    }
}
