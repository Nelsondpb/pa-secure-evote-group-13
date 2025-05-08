package shared;

import javax.net.ssl.*;
import java.io.InputStream;
import java.security.KeyStore;

public class SSLUtils {
    public static SSLContext criarContextoSSL() throws Exception {
        // 1. Carregar keystore
        KeyStore ks = KeyStore.getInstance("PKCS12");
        try (InputStream is = SSLUtils.class.getClassLoader()
                .getResourceAsStream("certificates/keystore.p12")) {
            ks.load(is, "password".toCharArray());
        }

        // 2. Configurar KeyManager
        KeyManagerFactory kmf = KeyManagerFactory.getInstance("SunX509");
        kmf.init(ks, "password".toCharArray());

        // 3. Configurar TrustManager
        TrustManagerFactory tmf = TrustManagerFactory.getInstance("SunX509");
        tmf.init(ks);

        // 4. Criar SSLContext
        SSLContext sslContext = SSLContext.getInstance("TLSv1.3");
        sslContext.init(kmf.getKeyManagers(), tmf.getTrustManagers(), null);
        return sslContext;
    }

    public static SSLServerSocket criarSocketServidor(int porta) throws Exception {
        SSLContext sslContext = criarContextoSSL();
        SSLServerSocketFactory ssf = sslContext.getServerSocketFactory();
        SSLServerSocket serverSocket = (SSLServerSocket) ssf.createServerSocket(porta);
        return serverSocket;
    }
}