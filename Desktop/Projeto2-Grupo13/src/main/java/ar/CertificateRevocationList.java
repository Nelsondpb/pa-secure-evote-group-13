package ar;

import shared.CertificadoEleitor;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class CertificateRevocationList {
    private final Map<String, CertificadoEleitor> certificadosRevogados = new ConcurrentHashMap<>();

    public synchronized void adicionarRevogacao(CertificadoEleitor certificado) {
        certificadosRevogados.put(certificado.getIdentificacao(), certificado);
    }

    public synchronized boolean isRevogado(String identificacaoEleitor) {
        return certificadosRevogados.containsKey(identificacaoEleitor);
    }

    public synchronized List<CertificadoEleitor> getCRL() {
        return new ArrayList<>(certificadosRevogados.values());
    }
}