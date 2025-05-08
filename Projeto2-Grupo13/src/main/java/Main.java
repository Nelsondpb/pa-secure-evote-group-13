import aa.AutoridadeApuramento;
import ar.AutoridadeRegisto;
import eleitor.Eleitor;
import sv.ServidorVotacao;
import ue.UrnaEletronica;

import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;


public class Main {
    public static void main(String[] args) {
        try {
            KeyPairGenerator keyGen = KeyPairGenerator.getInstance("RSA");
            keyGen.initialize(2048);

            KeyPair parChavesAR = keyGen.generateKeyPair();

            KeyPair parChavesAA = keyGen.generateKeyPair();

            AutoridadeRegisto ar = new AutoridadeRegisto(parChavesAR.getPrivate(), parChavesAR.getPublic());
            AutoridadeApuramento aa = new AutoridadeApuramento(parChavesAA.getPrivate(), parChavesAA.getPublic());
            ServidorVotacao sv = new ServidorVotacao(ar, aa.getChavePublicaAA());
            UrnaEletronica ue = new UrnaEletronica(sv, aa.getChavePublicaAA());

            Eleitor eleitor1 = new Eleitor("Eleitor1");

            eleitor1.registarNaAR(ar);

            eleitor1.autenticarNoSV(sv);

            eleitor1.votar("CandidatoA", ue, aa.getChavePublicaAA());

            System.out.println("Voto registado com sucesso!");

        } catch (NoSuchAlgorithmException e) {
            System.err.println("Algoritmo de criptografia não disponível: " + e.getMessage());
        } catch (Exception e) {
            System.err.println("Erro durante a execução: " + e.getMessage());
            e.printStackTrace();
        }
    }
}