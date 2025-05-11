package shared;

import java.io.*;
import java.security.*;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;
import java.util.Date;

public class CertificadoEleitor implements Serializable {
    private static final long serialVersionUID = 1L;

    private final String identificacao;
    private transient PublicKey chavePublica;
    private byte[] assinatura;
    private byte[] chavePublicaBytes;
    private final Date dataEmissao;
    private final Date dataExpiracao;
    private boolean revogado;

    public CertificadoEleitor(String identificacao, PublicKey chavePublica) {
        this.identificacao = identificacao;
        this.chavePublica = chavePublica;
        this.chavePublicaBytes = chavePublica.getEncoded();
        this.dataEmissao = new Date();
        this.dataExpiracao = calcularDataExpiracao();
        this.revogado = false;
    }

    private Date calcularDataExpiracao() {
        long umAnoEmMillis = 365L * 24 * 60 * 60 * 1000;
        return new Date(dataEmissao.getTime() + umAnoEmMillis);
    }

    private void writeObject(ObjectOutputStream out) throws IOException {
        out.defaultWriteObject();
        out.writeObject(this.chavePublicaBytes);
    }

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

    public boolean isValido() {
        Date agora = new Date();
        return !revogado && agora.before(dataExpiracao);
    }

    public void revogar() {
        this.revogado = true;
    }

    public byte[] getDadosParaAssinatura() {
        String dados = identificacao + Base64.getEncoder().encodeToString(chavePublicaBytes);
        return dados.getBytes();
    }

    public void setAssinatura(byte[] assinatura) {
        this.assinatura = assinatura;
    }

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
            System.err.println("Erro na verificação: " + e.getMessage());
            return false;
        }
    }

    public String toPemFormat() {
        return "-----BEGIN CERTIFICATE-----\n" +
                Base64.getEncoder().encodeToString(this.getDadosParaAssinatura()) +
                "\n-----END CERTIFICATE-----\n" +
                "Assinatura: " + (assinatura != null ? Base64.getEncoder().encodeToString(assinatura) : "NÃO ASSINADO") +
                "\nValidade: " + (isValido() ? "VÁLIDO" : "INVÁLIDO") +
                "\nExpira em: " + dataExpiracao;
    }

    public String getIdentificacao() { return identificacao; }
    public PublicKey getChavePublica() { return chavePublica; }
    public byte[] getAssinatura() { return assinatura; }
    public Date getDataEmissao() { return dataEmissao; }
    public Date getDataExpiracao() { return dataExpiracao; }
    public boolean isRevogado() { return revogado; }
}