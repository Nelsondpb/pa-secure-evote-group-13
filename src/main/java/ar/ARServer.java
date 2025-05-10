package ar;

import shared.SSLUtils;
import javax.net.ssl.SSLServerSocket;
import javax.net.ssl.SSLSocket;
import java.io.IOException;

public class ARServer {
    private final AutoridadeRegisto ar;
    private SSLServerSocket serverSocket;

    public ARServer(AutoridadeRegisto ar) {
        this.ar = ar;
    }

    public void start() {
        try {
            serverSocket = SSLUtils.criarSocketServidor(9090);
            serverSocket.setSoTimeout(0);

            System.out.println("🔒 AR Server SSL rodando na porta 9090");

            while (!serverSocket.isClosed()) {
                try {
                    SSLSocket clientSocket = (SSLSocket) serverSocket.accept();
                    new ARClientHandler(clientSocket, ar).start();
                } catch (IOException e) {
                    if (!serverSocket.isClosed()) {
                        System.err.println("Erro ao aceitar conexão: " + e.getMessage());
                    }
                }
            }
        } catch (Exception e) {
            System.err.println("Erro no servidor AR: " + e.getMessage());
        } finally {
            stop();
        }
    }

    public void stop() {
        try {
            if (serverSocket != null && !serverSocket.isClosed()) {
                serverSocket.close();
            }
        } catch (IOException e) {
            System.err.println("Erro ao parar servidor: " + e.getMessage());
        }
    }
}