package eleitor;

import shared.CertificadoEleitor;
import shared.NetworkUtils;
import shared.SSLUtils;
import javax.net.ssl.SSLSocket;
import java.io.IOException;
import java.security.PublicKey;
/**
 * Cliente responsável por se conectar à Autoridade de Registo (AR) via SSL
 * e submeter um certificado de eleitor para assinatura.
 */
public class ARClient {
    private static final String AR_HOST = "localhost";
    private static final int AR_PORT = 9090;


    /**
     * Envia um certificado para a AR para ser assinado.
     *
     * @param identificacao identificação única do eleitor
     * @param chavePublica  chave pública do eleitor
     * @return o certificado assinado pela AR
     * @throws Exception em caso de erro de conexão ou falha de protocolo
     */
    public static CertificadoEleitor enviarCertificadoParaAR(
            String identificacao, PublicKey chavePublica) throws Exception {

        SSLSocket socket = null;
        try {
            socket = (SSLSocket) SSLUtils.criarContextoSSL()
                    .getSocketFactory().createSocket(AR_HOST, AR_PORT);

            socket.setSoTimeout(10000);

            CertificadoEleitor certificado = new CertificadoEleitor(identificacao, chavePublica);
            NetworkUtils.sendObject(socket, certificado);

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