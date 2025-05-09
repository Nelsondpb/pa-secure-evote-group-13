package shared.exceptions;

/**
 * Exceção lançada quando um certificado é inválido.
 */
public class CertificadoInvalidoException extends Exception {
    public CertificadoInvalidoException(String message) {
        super(message);
    }
}