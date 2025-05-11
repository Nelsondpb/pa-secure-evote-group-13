package ar;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import shared.CertificadoEleitor;
import java.security.*;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.List;
import java.util.Optional;

public class AutoridadeRegisto {
    private static final Logger logger = LogManager.getLogger(AutoridadeRegisto.class);
    private static final Logger securityLogger = LogManager.getLogger("SecurityLogger");

    private final PrivateKey chavePrivadaAR;
    private final PublicKey chavePublicaAR;
    private final List<CertificadoEleitor> eleitoresRegistados = new CopyOnWriteArrayList<>();
    private final CertificateRevocationList crl;

    public AutoridadeRegisto(PrivateKey chavePrivadaAR, PublicKey chavePublicaAR) {
        this.chavePrivadaAR = chavePrivadaAR;
        this.chavePublicaAR = chavePublicaAR;
        this.crl = new CertificateRevocationList();
        logger.info("AutoridadeRegisto inicializada com chaves públicas e privadas");
    }

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

    public synchronized boolean validarCertificado(CertificadoEleitor certificado) {
        try {
            logger.debug("Validando certificado para: {}", certificado.getIdentificacao());

            if (!certificado.isValido() || crl.isRevogado(certificado.getIdentificacao())) {
                logger.warn("Certificado inválido ou revogado: {}", certificado.getIdentificacao());
                return false;
            }

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

    public synchronized void revogarCertificado(String identificacaoEleitor) {
        Optional<CertificadoEleitor> certificado = eleitoresRegistados.stream()
                .filter(c -> c.getIdentificacao().equals(identificacaoEleitor))
                .findFirst();

        if (certificado.isPresent()) {
            certificado.get().revogar();
            crl.adicionarRevogacao(certificado.get());
            logger.info("Certificado revogado: {}", identificacaoEleitor);
            securityLogger.warn("CERTIFICADO_REVOGADO: {}", identificacaoEleitor);

            new NotificadorService().notificarRevogacao(identificacaoEleitor);
        }
    }

    public List<CertificadoEleitor> getEleitoresRegistados() {
        return new CopyOnWriteArrayList<>(eleitoresRegistados);
    }

    public List<CertificadoEleitor> getCertificadosRevogados() {
        return crl.getCRL();
    }
}