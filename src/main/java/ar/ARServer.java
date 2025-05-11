package ar;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import shared.SSLUtils;
import javax.net.ssl.SSLServerSocket;
import javax.net.ssl.SSLSocket;
import java.io.IOException;

public class ARServer {
    private static final Logger logger = LogManager.getLogger(ARServer.class);
    private static final Logger securityLogger = LogManager.getLogger("SecurityLogger");

    private final AutoridadeRegisto ar;
    private SSLServerSocket serverSocket;

    public ARServer(AutoridadeRegisto ar) {
        this.ar = ar;
    }

    public void start() {
        try {
            serverSocket = SSLUtils.criarSocketServidor(9090);
            serverSocket.setSoTimeout(0);

            logger.info("🔒 Servidor AR iniciado na porta 9090");
            securityLogger.info("AR_SERVER_STARTED");

            while (!serverSocket.isClosed()) {
                try {
                    SSLSocket clientSocket = (SSLSocket) serverSocket.accept();
                    logger.debug("Nova conexão recebida: {}",
                            clientSocket.getInetAddress().getHostAddress());

                    new ARClientHandler(clientSocket, ar).start();
                } catch (IOException e) {
                    if (!serverSocket.isClosed()) {
                        logger.error("Erro ao aceitar conexão: {}", e.getMessage());
                    }
                }
            }
        } catch (Exception e) {
            logger.fatal("Erro crítico no servidor AR: {}", e.getMessage(), e);
            System.exit(1);
        } finally {
            stop();
        }
    }

    public void stop() {
        try {
            if (serverSocket != null && !serverSocket.isClosed()) {
                serverSocket.close();
                logger.info("Servidor AR encerrado com sucesso");
            }
        } catch (IOException e) {
            logger.error("Erro ao encerrar servidor: {}", e.getMessage());
        }
    }
}