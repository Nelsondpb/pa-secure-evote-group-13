package ue;

import java.io.Serializable;
import java.util.Arrays;
import java.util.UUID;
/**
 * Representa um voto encriptado associado a um token de autenticação único.
 * Essa classe é serializável para permitir persistência em disco.
 */


public class Voto implements Serializable {
    private static final long serialVersionUID = 1L;
    private final byte[] votoEncriptado;
    private final UUID token;

    /**
     * Construtor para criar um objeto Voto.
     *
     * @param votoEncriptado dados encriptados do voto
     * @param token token de autenticação do eleitor
     */
    public Voto(byte[] votoEncriptado, UUID token) {
        this.votoEncriptado = Arrays.copyOf(votoEncriptado, votoEncriptado.length);
        this.token = token;
    }

    /**
     * Retorna uma cópia do voto encriptado.
     *
     * @return array de bytes representando o voto
     */
    public byte[] getVotoEncriptado() {
        return Arrays.copyOf(votoEncriptado, votoEncriptado.length);
    }

    /**
     * Retorna o token de autenticação associado ao voto.
     *
     * @return token UUID
     */
    public UUID getToken() {
        return token;
    }
}