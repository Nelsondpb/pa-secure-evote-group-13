package eleitor;

import ar.AutoridadeRegisto;
import shared.CertificadoEleitor;
import shared.CryptoUtils;
import shared.PacoteVoto;
import shared.exceptions.AutenticacaoFalhouException;
import sv.ServidorVotacao;
import ue.UrnaEletronica;

import javax.crypto.SecretKey;
import java.security.*;
import java.util.UUID;

public class Eleitor {
    private final String identificacao;
    private final KeyPair parChaves;
    private CertificadoEleitor certificado;
    private UUID tokenVoto;

    public Eleitor(String identificacao) throws NoSuchAlgorithmException {
        this.identificacao = identificacao;
        KeyPairGenerator keyGen = KeyPairGenerator.getInstance("RSA");
        keyGen.initialize(2048);
        this.parChaves = keyGen.generateKeyPair();
    }

    public CertificadoEleitor registarNaAR() throws Exception {
        this.certificado = ARClient.enviarCertificadoParaAR(identificacao, parChaves.getPublic());
        return certificado;
    }

    public void autenticarNoSV(ServidorVotacao servidorVotacao) throws AutenticacaoFalhouException {
        if (certificado == null) {
            throw new AutenticacaoFalhouException("Eleitor não possui certificado válido");
        }
        this.tokenVoto = servidorVotacao.autenticarEleitor(certificado);
    }

    public void votar(String opcaoVoto, UrnaEletronica urna, PublicKey chavePublicaAA) throws Exception {
        if (tokenVoto == null) {
            throw new IllegalStateException("Eleitor não autenticado");
        }

        SecretKey chaveAES = CryptoUtils.generateAESKey();
        byte[] votoEncriptado = CryptoUtils.encryptAES(opcaoVoto.getBytes(), chaveAES);
        byte[] chaveEncriptada = CryptoUtils.encryptRSA(chaveAES.getEncoded(), chavePublicaAA);

        PacoteVoto pacote = new PacoteVoto(votoEncriptado, chaveEncriptada);
        urna.receberVoto(pacote.toByteArray(), tokenVoto);
    }

    public String getIdentificacao() { return identificacao; }
    public PublicKey getChavePublica() { return parChaves.getPublic(); }
    public CertificadoEleitor getCertificado() { return certificado; }
    public UUID getTokenVoto() { return tokenVoto; }
}