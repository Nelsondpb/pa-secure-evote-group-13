package ar;

import shared.CertificadoEleitor;
import java.security.*;
import java.util.*;
import java.util.concurrent.CopyOnWriteArrayList;

public class AutoridadeRegisto {
    private final PrivateKey chavePrivadaAR;
    private final PublicKey chavePublicaAR;
    private final List<CertificadoEleitor> eleitoresRegistados = new CopyOnWriteArrayList<>();

    public AutoridadeRegisto(PrivateKey chavePrivadaAR, PublicKey chavePublicaAR) {
        this.chavePrivadaAR = chavePrivadaAR;
        this.chavePublicaAR = chavePublicaAR;
    }

    public synchronized void registarEleitor(CertificadoEleitor certificado) throws Exception {
        if (eleitoresRegistados.stream()
                .anyMatch(c -> c.getIdentificacao().equals(certificado.getIdentificacao()))) {
            throw new Exception("Eleitor já registrado");
        }

        Signature sig = Signature.getInstance("SHA256withRSA");
        sig.initSign(chavePrivadaAR);
        sig.update(certificado.getDadosParaAssinatura());
        certificado.setAssinatura(sig.sign());

        eleitoresRegistados.add(certificado);
        System.out.println("✅ Eleitor registrado: " + certificado.getIdentificacao());
    }

    public synchronized boolean validarCertificado(CertificadoEleitor certificado) {
        try {
            System.out.println("🔍 Validando certificado para: " + certificado.getIdentificacao());

            boolean assinaturaValida = certificado.verificarAssinatura(chavePublicaAR);
            if (!assinaturaValida) {
                System.out.println("❌ Assinatura inválida");
                return false;
            }

            boolean registrado = eleitoresRegistados.stream()
                    .anyMatch(c -> c.getIdentificacao().equals(certificado.getIdentificacao()));

            System.out.println("📋 Certificado " + (registrado ? "encontrado" : "não encontrado") + " nos registros");
            return registrado;
        } catch (Exception e) {
            System.err.println("⚠️ Erro na validação: " + e.getMessage());
            return false;
        }
    }

    public List<CertificadoEleitor> getEleitoresRegistados() {
        return new ArrayList<>(eleitoresRegistados);
    }
}