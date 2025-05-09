package aa;

import javax.crypto.Cipher;
import javax.crypto.NoSuchPaddingException;
import java.security.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AutoridadeApuramento {
    private final PrivateKey chavePrivadaAA;
    private final PublicKey chavePublicaAA;


    public AutoridadeApuramento(PrivateKey chavePrivadaAA, PublicKey chavePublicaAA) {
        this.chavePrivadaAA = chavePrivadaAA;
        this.chavePublicaAA = chavePublicaAA;
    }


    public List<String> desencriptarVotos(List<byte[]> votosEncriptados) throws Exception {
        Cipher cipher = Cipher.getInstance("RSA");
        cipher.init(Cipher.DECRYPT_MODE, chavePrivadaAA);

        List<String> votosDesencriptados = new ArrayList<>();
        for (byte[] votoEncriptado : votosEncriptados) {
            byte[] bytesDesencriptados = cipher.doFinal(votoEncriptado);
            votosDesencriptados.add(new String(bytesDesencriptados));
        }

        return votosDesencriptados;
    }


    public Map<String, Integer> apurarVotos(List<String> votos) {
        Map<String, Integer> resultados = new HashMap<>();

        for (String voto : votos) {
            resultados.put(voto, resultados.getOrDefault(voto, 0) + 1);
        }

        return resultados;
    }


    public PublicKey getChavePublicaAA() {
        return chavePublicaAA;
    }
}