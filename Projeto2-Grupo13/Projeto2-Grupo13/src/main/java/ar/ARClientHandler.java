package ar;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import shared.CertificadoEleitor;
import shared.NetworkUtils;
import javax.net.ssl.SSLSocket;
import java.io.IOException;


public class ARClientHandler extends Thread {
    private static final Logger logger = LogManager.getLogger(ARClientHandler.class);
    private static final Logger securityLogger = LogManager.getLogger("SecurityLogger");

    private final SSLSocket socket;
    private final AutoridadeRegisto ar;

    public ARClientHandler(SSLSocket socket, AutoridadeRegisto ar) {
        this.socket = socket;
        this.ar = ar;
    }

    @Override
    public void run() {
        try {
            logger.debug("Iniciando handler para cliente: {}",
                    socket.getInetAddress().getHostAddress());

            CertificadoEleitor certificado = (CertificadoEleitor) NetworkUtils.receiveObject(socket);
            logger.info("Processando registro para: {}", certificado.getIdentificacao());

            ar.registarEleitor(certificado);
            NetworkUtils.sendObject(socket, certificado);

            securityLogger.info("CERTIFICATE_ISSUED: {}", certificado.getIdentificacao());
            logger.info("Registro concluído para: {}", certificado.getIdentificacao());
        } catch (Exception e) {
            logger.error("Erro no handler do cliente: {}", e.getMessage(), e);
        } finally {
            try {
                if (!socket.isClosed()) {
                    socket.close();
                }
            } catch (IOException e) {
                logger.warn("Erro ao fechar socket: {}", e.getMessage());
            }
        }
    }
}