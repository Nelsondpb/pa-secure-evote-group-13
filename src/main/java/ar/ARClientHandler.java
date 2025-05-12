package ar;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import shared.CertificadoEleitor;
import shared.NetworkUtils;
import javax.net.ssl.SSLSocket;
import java.io.IOException;

/**
 * Thread responsável por processar requisições de clientes à Autoridade de Registo.
 * <p>
 * Aceita e trata três tipos de mensagens:
 * <ul>
 *   <li>Strings iniciadas com "REVOGAR:" para revogação de certificado</li>
 *   <li>Instâncias de {@link CertificadoEleitor} para registro de eleitor</li>
 *   <li>Qualquer outro tipo, resultando em erro de tipo inválido</li>
 * </ul>
 */

public class ARClientHandler extends Thread {
    private static final Logger logger = LogManager.getLogger(ARClientHandler.class);
    private static final Logger securityLogger = LogManager.getLogger("SecurityLogger");

    private final SSLSocket socket;
    private final AutoridadeRegisto ar;


    /**
     * Cria um novo handler para atender um cliente via socket SSL.
     *
     * @param socket conexão SSL com o cliente
     * @param ar     instância de {@link AutoridadeRegisto} para operações de registro/revogação
     */

    public ARClientHandler(SSLSocket socket, AutoridadeRegisto ar) {
        this.socket = socket;
        this.ar = ar;
    }


    /**
     * Processa a requisição do cliente:
     * <ol>
     *   <li>Recebe objeto via {@link NetworkUtils#receiveObject(SSLSocket)}</li>
     *   <li>Se for String começando com "REVOGAR:", chama {@link AutoridadeRegisto#revogarCertificado(String)}</li>
     *   <li>Se for {@link CertificadoEleitor}, chama {@link AutoridadeRegisto#registarEleitor(CertificadoEleitor)}</li>
     *   <li>Em outros casos, envia código de erro "ERRO:TIPO_INVALIDO"</li>
     *   <li>Em caso de exceção, envia "ERRO:<mensagem>"</li>
     * </ol>
     * Após o tratamento, garante o fechamento do socket.
     */
    @Override
    public void run() {
        try {
            logger.debug("Iniciando handler para cliente: {}",
                    socket.getInetAddress().getHostAddress());

            Object input = NetworkUtils.receiveObject(socket);

            if (input instanceof String && ((String) input).startsWith("REVOGAR:")) {
                String idEleitor = ((String) input).split(":")[1];
                ar.revogarCertificado(idEleitor);
                NetworkUtils.sendObject(socket, "CERTIFICADO_REVOGADO:" + idEleitor);
                logger.info("Certificado revogado para: {}", idEleitor);
            } else if (input instanceof CertificadoEleitor) {
                CertificadoEleitor certificado = (CertificadoEleitor) input;
                logger.info("Processando registro para: {}", certificado.getIdentificacao());

                ar.registarEleitor(certificado);
                NetworkUtils.sendObject(socket, certificado);

                securityLogger.info("CERTIFICATE_ISSUED: {}", certificado.getIdentificacao());
                logger.info("Registro concluído para: {}", certificado.getIdentificacao());
            } else {
                logger.warn("Tipo de objeto recebido inválido");
                NetworkUtils.sendObject(socket, "ERRO:TIPO_INVALIDO");
            }
        } catch (Exception e) {
            logger.error("Erro no handler do cliente: {}", e.getMessage(), e);
            try {
                NetworkUtils.sendObject(socket, "ERRO:" + e.getMessage());
            } catch (IOException ioException) {
                logger.error("Falha ao enviar mensagem de erro", ioException);
            }
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