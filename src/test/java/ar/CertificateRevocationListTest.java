package ar;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import shared.CertificadoEleitor;

import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.PublicKey;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Testes unitários para a classe CertificateRevocationList.
 */
class CertificateRevocationListTest {

    private CertificateRevocationList crl;
    private PublicKey publicKey;

    @BeforeEach
    void setUp() throws Exception {
        crl = new CertificateRevocationList();
        KeyPairGenerator kpg = KeyPairGenerator.getInstance("RSA");
        kpg.initialize(2048);
        publicKey = kpg.generateKeyPair().getPublic();
    }

    @Test
    void testAdicionarRevogacaoEVerificar() {
        CertificadoEleitor cert = new CertificadoEleitor("eleitor1", publicKey);
        assertFalse(crl.isRevogado("eleitor1"));

        crl.adicionarRevogacao(cert);
        assertTrue(crl.isRevogado("eleitor1"));
    }

    @Test
    void testGetCRLRetornaTodosOsRevogados() {
        CertificadoEleitor cert1 = new CertificadoEleitor("eleitor1", publicKey);
        CertificadoEleitor cert2 = new CertificadoEleitor("eleitor2", publicKey);

        crl.adicionarRevogacao(cert1);
        crl.adicionarRevogacao(cert2);

        List<CertificadoEleitor> lista = crl.getCRL();
        assertEquals(2, lista.size());
        assertTrue(lista.contains(cert1));
        assertTrue(lista.contains(cert2));
    }
}

