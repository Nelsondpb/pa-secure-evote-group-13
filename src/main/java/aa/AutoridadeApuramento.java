package aa;

import shared.PacoteVoto;
import shared.CryptoUtils;
import shared.exceptions.DescriptografiaFalhouException;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.security.*;
import java.util.*;

/**
 * Classe responsável por apurar votos eletrônicos.
 * Desencripta os votos recebidos, verifica sua integridade e contabiliza os resultados.
 */
public class AutoridadeApuramento {
    private final PrivateKey chavePrivadaAA;
    private final PublicKey chavePublicaAA;
    private Map<String, Integer> resultados;

    /**
     * Constrói a instância da autoridade de apuramento.
     *
     * @param chavePrivadaAA chave privada usada para desencriptação
     * @param chavePublicaAA chave pública correspondente
     */
    public AutoridadeApuramento(PrivateKey chavePrivadaAA, PublicKey chavePublicaAA) {
        this.chavePrivadaAA = chavePrivadaAA;
        this.chavePublicaAA = chavePublicaAA;
        this.resultados = new HashMap<>();
    }

    /**
     * Desencripta uma lista de votos encriptados.
     *
     * @param votosEncriptados lista de votos em formato byte[]
     * @return lista de votos em texto plano
     * @throws Exception caso a integridade ou a desencriptação falhem
     */
    public List<String> desencriptarVotos(List<byte[]> votosEncriptados) throws Exception {
        List<String> votosDesencriptados = new ArrayList<>();

        for (byte[] votoBytes : votosEncriptados) {
            try {
                PacoteVoto pacote = PacoteVoto.fromByteArray(votoBytes);

                if (!pacote.verificarIntegridade()) {
                    throw new DescriptografiaFalhouException("Integridade do voto comprometida");
                }

                byte[] chaveAESBytes = CryptoUtils.decryptRSA(pacote.getChaveEncriptada(), chavePrivadaAA);
                SecretKey chaveAES = new SecretKeySpec(chaveAESBytes, "AES");

                String voto = new String(CryptoUtils.decryptAES(pacote.getVotoEncriptado(), chaveAES));
                votosDesencriptados.add(voto);
            } catch (Exception e) {
                throw new DescriptografiaFalhouException("Falha ao processar voto: " + e.getMessage());
            }
        }

        return votosDesencriptados;
    }

    /**
     * Realiza a contagem dos votos.
     *
     * @param votos lista de votos em texto plano
     * @return mapa com o total de votos por opção
     */
    public Map<String, Integer> apurarVotos(List<String> votos) {
        resultados = new HashMap<>();
        for (String voto : votos) {
            resultados.put(voto, resultados.getOrDefault(voto, 0) + 1);
        }
        return new HashMap<>(resultados);
    }

    /**
     * Gera um relatório textual da apuração com total de votos por candidato.
     *
     * @return string contendo o relatório formatado
     */
    public String gerarRelatorio() {
        StringBuilder relatorio = new StringBuilder();
        resultados.entrySet().stream()
                .sorted(Map.Entry.<String, Integer>comparingByValue().reversed())
                .forEach(entry -> relatorio.append("- ").append(entry.getKey()).append(": ").append(entry.getValue()).append(" votos\n"));

        relatorio.append("\n=== RELATÓRIO DE APURAMENTO ===\n");
        relatorio.append("Total de votos apurados: ").append(resultados.values().stream().mapToInt(Integer::intValue).sum()).append("\n\n");

        relatorio.append("\n=== FIM DO RELATÓRIO ===");
        return relatorio.toString();
    }

    /**
     * Retorna a chave pública da autoridade de apuramento.
     *
     * @return chave pública
     */
    public PublicKey getChavePublicaAA() {
        return chavePublicaAA;
    }
}