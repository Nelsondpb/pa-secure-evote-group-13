package eleitor;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import ar.AutoridadeRegisto;
import shared.*;
import shared.exceptions.*;
import sv.ServidorVotacao;
import ue.UrnaEletronica;

import javax.crypto.SecretKey;
import java.security.*;
import java.util.UUID;

public class Eleitor {
    private static final Logger logger = LogManager.getLogger(Eleitor.class);

    private final String identificacao;
    private final KeyPair parChaves;
    private CertificadoEleitor certificado;
    private UUID tokenVoto;

    public Eleitor(String identificacao) throws NoSuchAlgorithmException {
        if (identificacao == null || identificacao.isBlank()) {
            throw new IllegalArgumentException("Identificação do eleitor não pode ser vazia");
        }
        this.identificacao = identificacao;

        KeyPairGenerator keyGen = KeyPairGenerator.getInstance("RSA");
        keyGen.initialize(2048);
        this.parChaves = keyGen.generateKeyPair();

        logger.debug("Novo eleitor criado: {}", identificacao);
    }

    public CertificadoEleitor registarNaAR() throws Exception {
        try {
            this.certificado = ARClient.enviarCertificadoParaAR(identificacao, parChaves.getPublic());
            logger.info("Eleitor {} registrado com sucesso na AR", identificacao);
            return certificado;
        } catch (Exception e) {
            logger.error("Falha no registro do eleitor {}: {}", identificacao, e.getMessage());
            throw e;
        }
    }

    public void autenticarNoSV(ServidorVotacao servidorVotacao) throws AutenticacaoFalhouException {
        if (certificado == null) {
            throw new AutenticacaoFalhouException("Eleitor não possui certificado válido");
        }

        try {
            this.tokenVoto = servidorVotacao.autenticarEleitor(certificado);
            logger.info("Eleitor {} autenticado com sucesso. Token: {}",
                    identificacao, tokenVoto);
        } catch (Exception e) {
            logger.error("Falha na autenticação do eleitor {}: {}", identificacao, e.getMessage());
            throw e;
        }
    }

    public void votar(String opcaoVoto, UrnaEletronica urna, PublicKey chavePublicaAA) throws Exception {
        try {
            if (tokenVoto == null) {
                throw new IllegalStateException("Eleitor não autenticado");
            }

            if (opcaoVoto == null || opcaoVoto.isBlank()) {
                throw new IllegalArgumentException("Opção de voto inválida");
            }

            logger.debug("Eleitor {} votando em: {}", identificacao, opcaoVoto);

            SecretKey chaveAES = CryptoUtils.generateAESKey();
            byte[] votoEncriptado = CryptoUtils.encryptAES(opcaoVoto.getBytes(), chaveAES);
            byte[] chaveEncriptada = CryptoUtils.encryptRSA(chaveAES.getEncoded(), chavePublicaAA);

            PacoteVoto pacote = new PacoteVoto(votoEncriptado, chaveEncriptada);
            urna.receberVoto(pacote.toByteArray(), tokenVoto);

            logger.info("Voto do eleitor {} registrado com sucesso", identificacao);
        } catch (Exception e) {
            logger.error("Erro no voto do eleitor {}: {}", identificacao, e.getMessage());
            throw e;
        }
    }
    public String getIdentificacao() { return identificacao; }
    public PublicKey getChavePublica() { return parChaves.getPublic(); }
    public CertificadoEleitor getCertificado() { return certificado; }
    public UUID getTokenVoto() { return tokenVoto; }
}