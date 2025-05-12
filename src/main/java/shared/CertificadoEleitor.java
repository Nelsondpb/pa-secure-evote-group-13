package shared;

import java.io.*;
import java.security.*;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;
import java.util.Date;

/**
 * Representa um certificado digital de eleitor contendo
 * a identificacao, chave publica, datas de validade e assinatura digital.
 * Pode ser serializado e verificado usando chaves RSA.
 */
public class CertificadoEleitor implements Serializable {
    private static final long serialVersionUID = 1L;

    private final String identificacao;
    private transient PublicKey chavePublica;
    private byte[] assinatura;
    private byte[] chavePublicaBytes;
    private final Date dataEmissao;
    private final Date dataExpiracao;
    private boolean revogado;

    /**
     * Construtor de certificado de eleitor.
     * @param identificacao identificador unico do eleitor
     * @param chavePublica chave publica do eleitor
     */
    public CertificadoEleitor(String identificacao, PublicKey chavePublica) {
        this.identificacao = identificacao;
        this.chavePublica = chavePublica;
        this.chavePublicaBytes = chavePublica.getEncoded();
        this.dataEmissao = new Date();
        this.dataExpiracao = calcularDataExpiracao();
        this.revogado = false;
    }

    /**
     * Calcula a data de expiracao do certificado (1 ano apos emissao).
     * @return data de expiracao
     */
    private Date calcularDataExpiracao() {
        long umAnoEmMillis = 365L * 24 * 60 * 60 * 1000;
        return new Date(dataEmissao.getTime() + umAnoEmMillis);
    }

    /**
     * Serializa o objeto, incluindo a chave publica codificada.
     */
    private void writeObject(ObjectOutputStream out) throws IOException {
        out.defaultWriteObject();
        out.writeObject(this.chavePublicaBytes);
    }

    /**
     * Desserializa o objeto e reconstrói a chave publica.
     */
    private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException {
        in.defaultReadObject();
        this.chavePublicaBytes = (byte[]) in.readObject();
        try {
            this.chavePublica = KeyFactory.getInstance("RSA")
                    .generatePublic(new X509EncodedKeySpec(chavePublicaBytes));
        } catch (Exception e) {
            throw new IOException("Falha ao reconstruir PublicKey", e);
        }
    }

    /**
     * Verifica se o certificado esta valido (nao revogado e nao expirado).
     * @return true se valido, false caso contrario
     */
    public boolean isValido() {
        Date agora = new Date();
        return !revogado && agora.before(dataExpiracao);
    }

    /**
     * Marca o certificado como revogado.
     */
    public void revogar() {
        this.revogado = true;
    }

    /**
     * Retorna os dados brutos usados para assinar/verificar o certificado.
     * @return bytes dos dados de assinatura
     */
    public byte[] getDadosParaAssinatura() {
        String dados = identificacao + Base64.getEncoder().encodeToString(chavePublicaBytes);
        return dados.getBytes();
    }

    /**
     * Define a assinatura digital do certificado.
     * @param assinatura bytes da assinatura
     */
    public void setAssinatura(byte[] assinatura) {
        this.assinatura = assinatura;
    }

    /**
     * Verifica se a assinatura digital é valida usando a chave publica da AR.
     * @param chavePublicaAR chave usada para verificacao
     * @return true se a assinatura for valida
     */
    public boolean verificarAssinatura(PublicKey chavePublicaAR) {
        if (assinatura == null || chavePublicaAR == null) {
            return false;
        }

        try {
            Signature sig = Signature.getInstance("SHA256withRSA");
            sig.initVerify(chavePublicaAR);
            sig.update(this.getDadosParaAssinatura());
            return sig.verify(this.assinatura);
        } catch (Exception e) {
            System.err.println("Erro na verificacao: " + e.getMessage());
            return false;
        }
    }

    /**
     * Formata o certificado em uma representacao estilo PEM.
     * @return string formatada com dados e assinatura
     */
    public String toPemFormat() {
        return "-----BEGIN CERTIFICATE-----\n" +
                Base64.getEncoder().encodeToString(this.getDadosParaAssinatura()) +
                "\n-----END CERTIFICATE-----\n" +
                "Assinatura: " + (assinatura != null ? Base64.getEncoder().encodeToString(assinatura) : "NÃO ASSINADO") +
                "\nValidade: " + (isValido() ? "VÃLIDO" : "INVÃLIDO") +
                "\nExpira em: " + dataExpiracao;
    }

    // Getters

    /** @return identificacao do eleitor */
    public String getIdentificacao() { return identificacao; }

    /** @return chave publica do eleitor */
    public PublicKey getChavePublica() { return chavePublica; }

    /** @return assinatura digital do certificado */
    public byte[] getAssinatura() { return assinatura; }

    /** @return data de emissao do certificado */
    public Date getDataEmissao() { return dataEmissao; }

    /** @return data de expiracao do certificado */
    public Date getDataExpiracao() { return dataExpiracao; }

    /** @return true se o certificado estiver revogado */
    public boolean isRevogado() { return revogado; }
}