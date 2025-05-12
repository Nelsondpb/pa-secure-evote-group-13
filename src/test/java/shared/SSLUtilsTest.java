package shared;

import org.junit.jupiter.api.Test;

import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLServerSocket;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Testes para a classe SSLUtils.
 */
class SSLUtilsTest {

    @Test
    void testCriarContextoSSL() throws Exception {
        SSLContext context = SSLUtils.criarContextoSSL();
        assertNotNull(context);
        assertEquals("TLSv1.3", context.getProtocol());
    }

    @Test
    void testCriarSocketServidor() throws Exception {
        SSLServerSocket serverSocket = SSLUtils.criarSocketServidor(0); // porta aleatória
        assertNotNull(serverSocket);
        assertTrue(serverSocket.getLocalPort() > 0);
        serverSocket.close();
    }
}
