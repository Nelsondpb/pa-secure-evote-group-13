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

/**
 * Representa um eleitor que pode registrar-se na AR, autenticar-se no servidor de votação
 * e submeter seu voto criptografado à urna eletrônica.
 */
public class Eleitor {
    private static final Logger logger = LogManager.getLogger(Eleitor.class);

    private final String identificacao;
    private final KeyPair parChaves;
    private CertificadoEleitor certificado;
    private UUID tokenVoto;


    /**
     * Construtor que gera um novo par de chaves para o eleitor.
     *
     * @param identificacao identificação única do eleitor
     * @throws NoSuchAlgorithmException se o algoritmo RSA não estiver disponível
     */
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


    /**
     * Registra o eleitor na AR (Autoridade de Registo) e recebe um certificado assinado.
     *
     * @return o certificado assinado
     * @throws Exception se ocorrer erro na comunicação ou rejeição do certificado
     */
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
    /**
     * Autentica o eleitor junto ao servidor de votação.
     *
     * @param servidorVotacao instância do servidor de votação
     * @throws AutenticacaoFalhouException se o certificado for inválido ou rejeitado
     */

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

    /**
     * Submete o voto criptografado para a urna eletrônica.
     *
     * @param opcaoVoto   string representando a escolha do voto
     * @param urna        urna eletrônica receptora do voto
     * @param chavePublicaAA chave pública da Autoridade de Apuramento (AA)
     * @throws Exception se ocorrer erro ao encriptar ou enviar o voto
     */

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

    /** @return a identificação do eleitor */
    public String getIdentificacao() { return identificacao; }

    /** @return a chave pública do eleitor */
    public PublicKey getChavePublica() { return parChaves.getPublic(); }

    /** @return o certificado assinado recebido da AR */
    public CertificadoEleitor getCertificado() { return certificado; }

    /** @return o token de autenticação para votação */
    public UUID getTokenVoto() { return tokenVoto; }
}