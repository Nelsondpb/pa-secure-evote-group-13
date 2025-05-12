package shared;

import javax.crypto.*;
import javax.crypto.spec.GCMParameterSpec;
import java.security.*;
import java.util.Arrays;

/**
 * Classe  para operações de criptografia utilizando os algoritmos AES e RSA.
 * Fornece métodos para gerar chaves, encriptar e desencriptar dados.
 */
public class CryptoUtils {
    private static final String RSA_ALGORITHM = "RSA";
    private static final String AES_ALGORITHM = "AES";
    private static final String AES_TRANSFORMATION = "AES/GCM/NoPadding";
    private static final int GCM_TAG_LENGTH = 128;
    private static final int AES_KEY_SIZE = 256;
    private static final int RSA_KEY_SIZE = 2048;


    /**
     * Gera uma chave simétrica AES com tamanho de 256 bits.
     * @return chave AES gerada
     * @throws NoSuchAlgorithmException se o algoritmo AES não estiver disponível
     */
    public static SecretKey generateAESKey() throws NoSuchAlgorithmException {
        KeyGenerator keyGen = KeyGenerator.getInstance(AES_ALGORITHM);
        keyGen.init(AES_KEY_SIZE);
        return keyGen.generateKey();
    }

    /**
     * Encripta dados com AES usando GCM e IV aleatório.
     * @param data dados a encriptar
     * @param key chave AES usada para encriptação
     * @return vetor IV concatenado com os dados encriptados
     * @throws Exception em caso de erro durante a encriptação
     */

    public static byte[] encryptAES(byte[] data, SecretKey key) throws Exception {
        Cipher cipher = Cipher.getInstance(AES_TRANSFORMATION);
        byte[] iv = new byte[12]; // 96 bits para GCM
        new SecureRandom().nextBytes(iv);

        GCMParameterSpec spec = new GCMParameterSpec(GCM_TAG_LENGTH, iv);
        cipher.init(Cipher.ENCRYPT_MODE, key, spec);

        byte[] encrypted = cipher.doFinal(data);
        byte[] result = new byte[iv.length + encrypted.length];
        System.arraycopy(iv, 0, result, 0, iv.length);
        System.arraycopy(encrypted, 0, result, iv.length, encrypted.length);

        return result;
    }

    /**
     * Desencripta dados encriptados com AES em modo GCM.
     * @param encryptedData dados encriptados com IV
     * @param key chave AES para desencriptação
     * @return dados desencriptados
     * @throws Exception em caso de falha na desencriptação
     */
    public static byte[] decryptAES(byte[] encryptedData, SecretKey key) throws Exception {
        Cipher cipher = Cipher.getInstance(AES_TRANSFORMATION);

        byte[] iv = Arrays.copyOfRange(encryptedData, 0, 12);
        byte[] cipherText = Arrays.copyOfRange(encryptedData, 12, encryptedData.length);

        GCMParameterSpec spec = new GCMParameterSpec(GCM_TAG_LENGTH, iv);
        cipher.init(Cipher.DECRYPT_MODE, key, spec);

        return cipher.doFinal(cipherText);
    }

    /**
     * Encripta dados usando criptografia assimétrica RSA.
     * @param data dados a encriptar
     * @param publicKey chave pública usada para encriptação
     * @return dados encriptados
     * @throws Exception em caso de erro na encriptação
     */
    public static byte[] encryptRSA(byte[] data, PublicKey publicKey) throws Exception {
        Cipher cipher = Cipher.getInstance(RSA_ALGORITHM);
        cipher.init(Cipher.ENCRYPT_MODE, publicKey);
        return cipher.doFinal(data);
    }

    /**
     * Desencripta dados encriptados com RSA.
     * @param data dados encriptados
     * @param privateKey chave privada para desencriptação
     * @return dados desencriptados
     * @throws Exception em caso de falha na desencriptação
     */
    public static byte[] decryptRSA(byte[] data, PrivateKey privateKey) throws Exception {
        Cipher cipher = Cipher.getInstance(RSA_ALGORITHM);
        cipher.init(Cipher.DECRYPT_MODE, privateKey);
        return cipher.doFinal(data);
    }
}