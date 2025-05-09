package ar;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import shared.CertificadoEleitor;
import java.security.*;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.List;

/**
 * Classe que representa a Autoridade de Registo
 */
public class AutoridadeRegisto {
    private static final Logger logger = LogManager.getLogger(AutoridadeRegisto.class);
    private static final Logger securityLogger = LogManager.getLogger("SecurityLogger");

    private final PrivateKey chavePrivadaAR;
    private final PublicKey chavePublicaAR;
    private final List<CertificadoEleitor> eleitoresRegistados = new CopyOnWriteArrayList<>();

    public AutoridadeRegisto(PrivateKey chavePrivadaAR, PublicKey chavePublicaAR) {
        this.chavePrivadaAR = chavePrivadaAR;
        this.chavePublicaAR = chavePublicaAR;
        logger.info("AutoridadeRegisto inicializada com chaves públicas e privadas");
    }

    /**
     * Registra um novo eleitor
     */
    public synchronized void registarEleitor(CertificadoEleitor certificado) throws Exception {
        try {
            logger.debug("Iniciando registro para: {}", certificado.getIdentificacao());

            if (eleitoresRegistados.stream()
                    .anyMatch(c -> c.getIdentificacao().equals(certificado.getIdentificacao()))) {
                throw new Exception("Eleitor já registrado");
            }

            Signature sig = Signature.getInstance("SHA256withRSA");
            sig.initSign(chavePrivadaAR);
            sig.update(certificado.getDadosParaAssinatura());
            byte[] assinatura = sig.sign();
            certificado.setAssinatura(assinatura);

            eleitoresRegistados.add(certificado);

            securityLogger.info("REGISTRO_COMPLETO: {}", certificado.getIdentificacao());
            logger.info("Novo eleitor registrado: {}", certificado.getIdentificacao());
        } catch (Exception e) {
            logger.error("Falha no registro do eleitor {}: {}",
                    certificado.getIdentificacao(), e.getMessage(), e);
            throw e;
        }
    }

    /**
     * Valida um certificado de eleitor
     */
    public synchronized boolean validarCertificado(CertificadoEleitor certificado) {
        try {
            logger.debug("Validando certificado para: {}", certificado.getIdentificacao());

            if (!certificado.verificarAssinatura(chavePublicaAR)) {
                logger.warn("Assinatura inválida para: {}", certificado.getIdentificacao());
                return false;
            }

            boolean registrado = eleitoresRegistados.stream()
                    .anyMatch(c -> c.getIdentificacao().equals(certificado.getIdentificacao()));

            if (registrado) {
                securityLogger.info("VALIDACAO_CERTIFICADO_SUCESSO: {}",
                        certificado.getIdentificacao());
            } else {
                logger.warn("Certificado não registrado: {}", certificado.getIdentificacao());
            }

            return registrado;
        } catch (Exception e) {
            logger.error("Erro na validação do certificado: {}", e.getMessage(), e);
            return false;
        }
    }

    public List<CertificadoEleitor> getEleitoresRegistados() {
        return new CopyOnWriteArrayList<>(eleitoresRegistados);
    }
}