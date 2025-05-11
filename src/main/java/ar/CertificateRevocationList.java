package ar;

import shared.CertificadoEleitor;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
/**
 * Representa uma lista de revogação de certificados (CRL - Certificate Revocation List).
 * Armazena e permite consultar certificados de eleitores revogados.
 */
public class CertificateRevocationList {
    private final Map<String, CertificadoEleitor> certificadosRevogados = new ConcurrentHashMap<>();
    /**
     * Adiciona um certificado à lista de revogados.
     *
     * @param certificado o certificado de eleitor a ser revogado
     */
    public synchronized void adicionarRevogacao(CertificadoEleitor certificado) {
        certificadosRevogados.put(certificado.getIdentificacao(), certificado);
    }
    /**
     * Verifica se o certificado associado ao ID fornecido está revogado.
     *
     * @param identificacaoEleitor ID do eleitor
     * @return true se o certificado estiver revogado; false caso contrário
     */
    public synchronized boolean isRevogado(String identificacaoEleitor) {
        return certificadosRevogados.containsKey(identificacaoEleitor);
    }
    /**
     * Retorna a lista completa de certificados revogados.
     *
     * @return lista de {@link CertificadoEleitor} revogados
     */
    public synchronized List<CertificadoEleitor> getCRL() {
        return new ArrayList<>(certificadosRevogados.values());
    }
}