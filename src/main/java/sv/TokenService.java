package sv;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;
/**
 * Serviço responsável pela emissão e validação de tokens de autenticação.
 * Cada token é único e só pode ser validado uma vez.
 */
public class TokenService {

    /** Conjunto que armazena os tokens emitidos que ainda não foram usados. */
    private final Set<UUID> tokensEmitidos = new HashSet<>();

    /**
     * Emite um novo token UUID e o armazena internamente.
     *
     * @return token gerado
     */
    public UUID emitirToken() {
        UUID token = UUID.randomUUID();
        tokensEmitidos.add(token);
        return token;
    }

    /**
     * Valida se o token foi emitido e ainda não foi usado.
     * Após a validação bem-sucedida, o token é removido para impedir reutilização.
     *
     * @param token o token a ser validado
     * @return true se for válido, false caso contrário
     */

    public boolean validarToken(UUID token) {
        return tokensEmitidos.remove(token);
    }
}