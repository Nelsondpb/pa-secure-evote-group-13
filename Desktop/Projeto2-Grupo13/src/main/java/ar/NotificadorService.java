package ar;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class NotificadorService {
    private static final Logger logger = LogManager.getLogger(NotificadorService.class);

    public void notificarRevogacao(String identificacaoEleitor) {
        String mensagem = String.format(
                "NOTIFICAÇÃO: O certificado do eleitor %s foi revogado. ",
                identificacaoEleitor
        );
        logger.info(mensagem);
    }
}