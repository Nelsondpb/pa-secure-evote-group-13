package eleitor;

import ar.AutoridadeRegisto;
import shared.CertificadoEleitor;
import shared.exceptions.AutenticacaoFalhouException;
import shared.exceptions.VotoInvalidoException;
import sv.ServidorVotacao;
import ue.UrnaEletronica;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
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


    public CertificadoEleitor registarNaAR(AutoridadeRegisto autoridadeRegisto)
            throws NoSuchAlgorithmException, InvalidKeyException, SignatureException {
        this.certificado = autoridadeRegisto.registarEleitor(identificacao, parChaves.getPublic());
        return certificado;
    }

    public void autenticarNoSV(ServidorVotacao servidorVotacao) throws AutenticacaoFalhouException {
        if (certificado == null) {
            throw new AutenticacaoFalhouException("Eleitor não possui certificado válido");
        }

        this.tokenVoto = servidorVotacao.autenticarEleitor(certificado);
    }


    public void votar(String opcaoVoto, UrnaEletronica urnaEletronica, PublicKey chavePublicaAA)
            throws NoSuchAlgorithmException, NoSuchPaddingException, InvalidKeyException,
            IllegalBlockSizeException, BadPaddingException, VotoInvalidoException {
        if (tokenVoto == null) {
            throw new IllegalStateException("Eleitor não autenticado no SV");
        }

        try {
            Cipher cipher = Cipher.getInstance("RSA");
            cipher.init(Cipher.ENCRYPT_MODE, chavePublicaAA);
            byte[] votoEncriptado = cipher.doFinal(opcaoVoto.getBytes());

            urnaEletronica.receberVoto(votoEncriptado, tokenVoto);
        } catch (NoSuchAlgorithmException | NoSuchPaddingException | InvalidKeyException |
                 IllegalBlockSizeException | BadPaddingException e) {
            throw new VotoInvalidoException("Erro ao encriptar o voto: " + e.getMessage());
        }
    }


    public String getIdentificacao() {
        return identificacao;
    }

    public PublicKey getChavePublica() {
        return parChaves.getPublic();
    }

    public CertificadoEleitor getCertificado() {
        return certificado;
    }
}