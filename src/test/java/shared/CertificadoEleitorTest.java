package shared;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.*;
import java.security.*;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Testes unitários para a classe CertificadoEleitor.
 */
class CertificadoEleitorTest {

    private KeyPair keyPair;
    private CertificadoEleitor certificado;

    @BeforeEach
    void setUp() throws Exception {
        KeyPairGenerator keyGen = KeyPairGenerator.getInstance("RSA");
        keyGen.initialize(2048);
        keyPair = keyGen.generateKeyPair();
        certificado = new CertificadoEleitor("eleitor123", keyPair.getPublic());
    }

    @Test
    void testInicializacao() {
        assertEquals("eleitor123", certificado.getIdentificacao());
        assertNotNull(certificado.getChavePublica());
        assertTrue(certificado.isValido());
        assertFalse(certificado.isRevogado());
    }

    @Test
    void testRevogarCertificado() {
        certificado.revogar();
        assertTrue(certificado.isRevogado());
        assertFalse(certificado.isValido());
    }

    @Test
    void testAssinaturaEVerificacao() throws Exception {
        Signature sig = Signature.getInstance("SHA256withRSA");
        sig.initSign(keyPair.getPrivate());
        sig.update(certificado.getDadosParaAssinatura());
        byte[] assinatura = sig.sign();

        certificado.setAssinatura(assinatura);
        assertTrue(certificado.verificarAssinatura(keyPair.getPublic()));
    }

    @Test
    void testVerificacaoAssinaturaInvalida() throws Exception {
        Signature sig = Signature.getInstance("SHA256withRSA");
        sig.initSign(keyPair.getPrivate());
        sig.update("dados falsos".getBytes());
        byte[] assinaturaFalsa = sig.sign();

        certificado.setAssinatura(assinaturaFalsa);
        assertFalse(certificado.verificarAssinatura(keyPair.getPublic()));
    }

    @Test
    void testSerializacaoEDesserializacao() throws Exception {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        ObjectOutputStream oos = new ObjectOutputStream(bos);
        oos.writeObject(certificado);

        ByteArrayInputStream bis = new ByteArrayInputStream(bos.toByteArray());
        ObjectInputStream ois = new ObjectInputStream(bis);
        CertificadoEleitor desserializado = (CertificadoEleitor) ois.readObject();

        assertEquals(certificado.getIdentificacao(), desserializado.getIdentificacao());
        assertNotNull(desserializado.getChavePublica());
    }

    @Test
    void testToPemFormatContemDados() {
        String pem = certificado.toPemFormat();
        assertTrue(pem.contains("BEGIN CERTIFICATE"));
        assertTrue(pem.contains("eleitor123") || pem.contains("CERTIFICATE"));
    }
}