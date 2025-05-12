package shared;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 *  log para padronizar mensagens de informação, erro, debug e segurança.
 */
public class LoggerUtil {
    private static final Logger logger = LogManager.getLogger(LoggerUtil.class);

    /**
     * Registra uma mensagem informativa no log padrão.
     * @param message mensagem a ser registrada
     */
    public static void logInfo(String message) {
        logger.info(message);
    }

    /**
     * Registra uma mensagem de erro com exceção no log padrão.
     * @param message mensagem de erro
     * @param throwable exceção associada ao erro
     */
    public static void logError(String message, Throwable throwable) {
        logger.error(message, throwable);
    }

    /**
     * Registra uma mensagem de debug no log padrão.
     * @param message mensagem para debugging
     */
    public static void logDebug(String message) {
        logger.debug(message);
    }

    /**
     * Registra um evento de segurança no log de segurança.
     * @param event descrição do evento de segurança
     */
    public static void logSecurityEvent(String event) {
        Logger securityLogger = LogManager.getLogger("SecurityLogger");
        securityLogger.info("[SECURITY] " + event);
    }
}
