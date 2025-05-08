package eleitor;

import shared.CertificadoEleitor;
import shared.NetworkUtils;
import shared.SSLUtils;
import javax.net.ssl.SSLSocket;
import java.io.IOException;
import java.security.PublicKey;

public class ARClient {
    private static final String AR_HOST = "localhost";
    private static final int AR_PORT = 9090;

    public static CertificadoEleitor enviarCertificadoParaAR(
            String identificacao, PublicKey chavePublica) throws Exception {

        SSLSocket socket = null;
        try {
            socket = (SSLSocket) SSLUtils.criarContextoSSL()
                    .getSocketFactory().createSocket(AR_HOST, AR_PORT);

            // Configurar timeout para evitar bloqueios
            socket.setSoTimeout(10000);

            CertificadoEleitor certificado = new CertificadoEleitor(identificacao, chavePublica);
            NetworkUtils.sendObject(socket, certificado);

            // Receber resposta antes de fechar
            CertificadoEleitor resposta = (CertificadoEleitor) NetworkUtils.receiveObject(socket);
            return resposta;

        } finally {
            if (socket != null && !socket.isClosed()) {
                try {
                    socket.close();
                } catch (IOException e) {
                    System.err.println("Erro ao fechar socket: " + e.getMessage());
                }
            }
        }
    }
}