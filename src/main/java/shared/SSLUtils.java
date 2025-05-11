package shared;

import javax.net.ssl.*;
import java.io.InputStream;
import java.security.KeyStore;

/**
 * Utilitários para criação de contexto SSL e sockets seguros
 * utilizando certificados do tipo PKCS12.
 */
public class SSLUtils {

    /**
     * Cria um contexto SSL configurado com um keystore PKCS12.
     * O keystore deve estar localizado em "certificates/keystore.p12"
     * dentro do classpath e protegido pela senha "password".
     *
     * @return um {@link SSLContext} configurado para uso com TLSv1.3
     * @throws Exception se houver erro ao carregar o keystore ou configurar o contexto
     */
    public static SSLContext criarContextoSSL() throws Exception {
        KeyStore ks = KeyStore.getInstance("PKCS12");
        try (InputStream is = SSLUtils.class.getClassLoader()
                .getResourceAsStream("certificates/keystore.p12")) {
            ks.load(is, "password".toCharArray());
        }

        KeyManagerFactory kmf = KeyManagerFactory.getInstance("SunX509");
        kmf.init(ks, "password".toCharArray());

        TrustManagerFactory tmf = TrustManagerFactory.getInstance("SunX509");
        tmf.init(ks);

        SSLContext sslContext = SSLContext.getInstance("TLSv1.3");
        sslContext.init(kmf.getKeyManagers(), tmf.getTrustManagers(), null);
        return sslContext;
    }

    /**
     * Cria um socket de servidor SSL escutando na porta especificada.
     *
     * @param porta número da porta para escutar conexões
     * @return uma instância de {@link SSLServerSocket}
     * @throws Exception se houver erro ao configurar o socket seguro
     */
    public static SSLServerSocket criarSocketServidor(int porta) throws Exception {
        SSLContext sslContext = criarContextoSSL();
        SSLServerSocketFactory ssf = sslContext.getServerSocketFactory();
        SSLServerSocket serverSocket = (SSLServerSocket) ssf.createServerSocket(porta);
        return serverSocket;
    }
}