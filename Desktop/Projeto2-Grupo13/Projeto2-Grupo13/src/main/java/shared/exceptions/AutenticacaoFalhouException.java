package shared.exceptions;

/**
 * Exceção lançada quando a autenticação de um eleitor falha.
 */
public class AutenticacaoFalhouException extends Exception {
    public AutenticacaoFalhouException(String message) {
        super(message);
    }
}