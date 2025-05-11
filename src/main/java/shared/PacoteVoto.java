package shared;

import java.io.*;
import java.security.*;
import java.util.Arrays;

/**
 * Representa um pacote de voto contendo o voto encriptado, a chave AES encriptada,
 * e um hash para verificar a integridade do conteúdo.
 */
public class PacoteVoto implements Serializable {
    private static final long serialVersionUID = 1L;
    private final byte[] votoEncriptado;
    private final byte[] chaveEncriptada;
    private final byte[] hash;

    /**
     * Construtor que recebe os dados encriptados e gera o hash de integridade.
     *
     * @param votoEncriptado dados do voto encriptado
     * @param chaveEncriptada chave AES encriptada
     * @throws NoSuchAlgorithmException se o algoritmo SHA-256 não estiver disponível
     */
    public PacoteVoto(byte[] votoEncriptado, byte[] chaveEncriptada) throws NoSuchAlgorithmException {
        this.votoEncriptado = votoEncriptado;
        this.chaveEncriptada = chaveEncriptada;
        this.hash = calcularHash();
    }

    /**
     * Calcula o hash SHA-256 dos dados concatenados do voto e chave.
     *
     * @return hash gerado
     * @throws NoSuchAlgorithmException se SHA-256 não estiver disponível
     */
    private byte[] calcularHash() throws NoSuchAlgorithmException {
        MessageDigest digest = MessageDigest.getInstance("SHA-256");
        byte[] dados = concatenarArrays(votoEncriptado, chaveEncriptada);
        return digest.digest(dados);
    }

    /**
     * Concatena dois arrays de bytes.
     *
     * @param a primeiro array
     * @param b segundo array
     * @return array resultante da concatenação
     */
    private byte[] concatenarArrays(byte[] a, byte[] b) {
        byte[] result = new byte[a.length + b.length];
        System.arraycopy(a, 0, result, 0, a.length);
        System.arraycopy(b, 0, result, a.length, b.length);
        return result;
    }

    /**
     * Verifica a integridade dos dados comparando o hash armazenado com um novo hash calculado.
     *
     * @return true se os hashes coincidem, false caso contrário
     * @throws NoSuchAlgorithmException se SHA-256 não estiver disponível
     */
    public boolean verificarIntegridade() throws NoSuchAlgorithmException {
        byte[] novoHash = calcularHash();
        return Arrays.equals(this.hash, novoHash);
    }

    /**
     * Serializa o objeto para um array de bytes.
     *
     * @return array de bytes representando o objeto
     * @throws IOException se ocorrer erro na escrita
     */
    public byte[] toByteArray() throws IOException {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        ObjectOutputStream out = new ObjectOutputStream(bos);
        out.writeObject(this);
        return bos.toByteArray();
    }

    /**
     * Reconstrói um objeto PacoteVoto a partir de um array de bytes serializado.
     *
     * @param data array de bytes contendo o objeto serializado
     * @return objeto PacoteVoto
     * @throws IOException se ocorrer erro na leitura
     * @throws ClassNotFoundException se a classe do objeto não for encontrada
     */
    public static PacoteVoto fromByteArray(byte[] data) throws IOException, ClassNotFoundException {
        ByteArrayInputStream bis = new ByteArrayInputStream(data);
        ObjectInputStream in = new ObjectInputStream(bis);
        return (PacoteVoto) in.readObject();
    }

    // Getters

    /**
     * Retorna o voto encriptado.
     *
     * @return array de bytes do voto encriptado
     */
    public byte[] getVotoEncriptado() { return votoEncriptado; }

    /**
     * Retorna a chave AES encriptada.
     *
     * @return array de bytes da chave encriptada
     */
    public byte[] getChaveEncriptada() { return chaveEncriptada; }
}
