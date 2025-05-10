package ar;

import shared.CertificadoEleitor;
import shared.NetworkUtils;
import javax.net.ssl.SSLSocket;
import java.io.IOException;

public class ARClientHandler extends Thread {
    private final SSLSocket socket;
    private final AutoridadeRegisto ar;

    public ARClientHandler(SSLSocket socket, AutoridadeRegisto ar) {
        this.socket = socket;
        this.ar = ar;
    }

    @Override
    public void run() {
        try {
            CertificadoEleitor certificado = (CertificadoEleitor) NetworkUtils.receiveObject(socket);
            System.out.println("📥 Recebido pedido de registro para: " + certificado.getIdentificacao());

            ar.registarEleitor(certificado);

            NetworkUtils.sendObject(socket, certificado);
            System.out.println("📤 Enviado certificado assinado para: " + certificado.getIdentificacao());
        } catch (Exception e) {
            System.err.println("❌ Erro no handler: " + e.getMessage());
        } finally {
            try {
                if (!socket.isClosed()) {
                    socket.close();
                }
            } catch (IOException e) {
                System.err.println("⚠️ Erro ao fechar socket: " + e.getMessage());
            }
        }
    }
}