package ar;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import shared.CertificadoEleitor;

import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Testes unitários para a classe AutoridadeRegisto.
 */
class AutoridadeRegistoTest {

    private AutoridadeRegisto ar;
    private PrivateKey privateKey;
    private PublicKey publicKey;

    @BeforeEach
    void setUp() throws Exception {
        KeyPairGenerator kpg = KeyPairGenerator.getInstance("RSA");
        kpg.initialize(2048);
        KeyPair keyPair = kpg.generateKeyPair();
        privateKey = keyPair.getPrivate();
        publicKey = keyPair.getPublic();
        ar = new AutoridadeRegisto(privateKey, publicKey);
    }

    @Test
    void testRegistarEleitorComSucesso() throws Exception {
        CertificadoEleitor cert = new CertificadoEleitor("eleitor1", publicKey);
        ar.registarEleitor(cert);

        List<CertificadoEleitor> registrados = ar.getEleitoresRegistados();
        assertEquals(1, registrados.size());
        assertEquals("eleitor1", registrados.get(0).getIdentificacao());
        assertNotNull(registrados.get(0).getAssinatura());
    }

    @Test
    void testRegistarEleitorDuplicadoLancaExcecao() throws Exception {
        CertificadoEleitor cert = new CertificadoEleitor("eleitor1", publicKey);
        ar.registarEleitor(cert);
        Exception ex = assertThrows(Exception.class, () -> ar.registarEleitor(cert));
        assertEquals("Eleitor já registrado", ex.getMessage());
    }

    @Test
    void testValidarCertificadoComSucesso() throws Exception {
        CertificadoEleitor cert = new CertificadoEleitor("eleitor1", publicKey);
        ar.registarEleitor(cert);
        assertTrue(ar.validarCertificado(cert));
    }

    @Test
    void testValidarCertificadoNaoRegistradoFalha() throws Exception {
        CertificadoEleitor cert = new CertificadoEleitor("eleitor2", publicKey);
        assertFalse(ar.validarCertificado(cert));
    }

    @Test
    void testRevogarCertificado() throws Exception {
        CertificadoEleitor cert = new CertificadoEleitor("eleitor3", publicKey);
        ar.registarEleitor(cert);
        ar.revogarCertificado("eleitor3");

        assertFalse(ar.validarCertificado(cert));
        List<CertificadoEleitor> revogados = ar.getCertificadosRevogados();
        assertEquals(1, revogados.size());
        assertEquals("eleitor3", revogados.get(0).getIdentificacao());
    }
}
