package ar;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;


/**
 * Serviço responsável por notificar (via logging) a revogação de certificados de eleitores.
 */
public class NotificadorService {
    private static final Logger logger = LogManager.getLogger(NotificadorService.class);

    /**
     * Notifica a revogação de um certificado de eleitor.
     *
     * @param identificacaoEleitor o identificador do eleitor cujo certificado foi revogado
     */
    public void notificarRevogacao(String identificacaoEleitor) {
        String mensagem = String.format(
                "NOTIFICAÇÃO: O certificado do eleitor %s foi revogado. ",
                identificacaoEleitor
        );
        logger.info(mensagem);
    }
}