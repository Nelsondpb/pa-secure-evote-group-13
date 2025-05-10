package aa;

import shared.PacoteVoto;
import shared.CryptoUtils;
import shared.exceptions.DescriptografiaFalhouException;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.security.*;
import java.util.*;

public class AutoridadeApuramento {
    private final PrivateKey chavePrivadaAA;
    private final PublicKey chavePublicaAA;
    private Map<String, Integer> resultados;

    public AutoridadeApuramento(PrivateKey chavePrivadaAA, PublicKey chavePublicaAA) {
        this.chavePrivadaAA = chavePrivadaAA;
        this.chavePublicaAA = chavePublicaAA;
        this.resultados = new HashMap<>();
    }

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

    public Map<String, Integer> apurarVotos(List<String> votos) {
        resultados = new HashMap<>();
        for (String voto : votos) {
            resultados.put(voto, resultados.getOrDefault(voto, 0) + 1);
        }
        return new HashMap<>(resultados);
    }

    public String gerarRelatorio() {
        StringBuilder relatorio = new StringBuilder();
        relatorio.append("=== RELATÓRIO DE APURAMENTO ===\n");
        relatorio.append("Total de votos apurados: ").append(resultados.values().stream().mapToInt(Integer::intValue).sum()).append("\n\n");
        relatorio.append("RESULTADOS:\n");

        resultados.entrySet().stream()
                .sorted(Map.Entry.<String, Integer>comparingByValue().reversed())
                .forEach(entry -> relatorio.append("- ").append(entry.getKey()).append(": ").append(entry.getValue()).append(" votos\n"));

        relatorio.append("\n=== FIM DO RELATÓRIO ===");
        return relatorio.toString();
    }

    public PublicKey getChavePublicaAA() {
        return chavePublicaAA;
    }
}