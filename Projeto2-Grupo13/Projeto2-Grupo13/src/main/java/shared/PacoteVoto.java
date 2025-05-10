package shared;

import java.io.*;
import java.security.*;
import java.util.Arrays;

public class PacoteVoto implements Serializable {
    private static final long serialVersionUID = 1L;
    private final byte[] votoEncriptado;
    private final byte[] chaveEncriptada;
    private final byte[] hash;

    public PacoteVoto(byte[] votoEncriptado, byte[] chaveEncriptada) throws NoSuchAlgorithmException {
        this.votoEncriptado = votoEncriptado;
        this.chaveEncriptada = chaveEncriptada;
        this.hash = calcularHash();
    }

    private byte[] calcularHash() throws NoSuchAlgorithmException {
        MessageDigest digest = MessageDigest.getInstance("SHA-256");
        byte[] dados = concatenarArrays(votoEncriptado, chaveEncriptada);
        return digest.digest(dados);
    }

    private byte[] concatenarArrays(byte[] a, byte[] b) {
        byte[] result = new byte[a.length + b.length];
        System.arraycopy(a, 0, result, 0, a.length);
        System.arraycopy(b, 0, result, a.length, b.length);
        return result;
    }

    public boolean verificarIntegridade() throws NoSuchAlgorithmException {
        byte[] novoHash = calcularHash();
        return Arrays.equals(this.hash, novoHash);
    }

    public byte[] toByteArray() throws IOException {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        ObjectOutputStream out = new ObjectOutputStream(bos);
        out.writeObject(this);
        return bos.toByteArray();
    }

    public static PacoteVoto fromByteArray(byte[] data) throws IOException, ClassNotFoundException {
        ByteArrayInputStream bis = new ByteArrayInputStream(data);
        ObjectInputStream in = new ObjectInputStream(bis);
        return (PacoteVoto) in.readObject();
    }

    public byte[] getVotoEncriptado() { return votoEncriptado; }
    public byte[] getChaveEncriptada() { return chaveEncriptada; }
}