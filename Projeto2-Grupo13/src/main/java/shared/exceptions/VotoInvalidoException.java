package shared.exceptions;

/**
 * Exceção lançada quando um voto é inválido.
 */
public class VotoInvalidoException extends Exception {
    public VotoInvalidoException(String message) {
        super(message);
    }
}