package ue;

import sv.ServidorVotacao;
import shared.exceptions.VotoInvalidoException;

import java.security.PublicKey;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class UrnaEletronica {
    private final ServidorVotacao servidorVotacao;
    private final PublicKey chavePublicaAA;
    private final List<byte[]> votosEncriptados;


    public UrnaEletronica(ServidorVotacao servidorVotacao, PublicKey chavePublicaAA) {
        this.servidorVotacao = servidorVotacao;
        this.chavePublicaAA = chavePublicaAA;
        this.votosEncriptados = new ArrayList<>();
    }


    public void receberVoto(byte[] votoEncriptado, UUID token) throws VotoInvalidoException {
        if (!servidorVotacao.validarToken(token)) {
            throw new VotoInvalidoException("Token de voto inválido");
        }

        if (votoEncriptado == null || votoEncriptado.length == 0) {
            throw new VotoInvalidoException("Voto encriptado inválido");
        }

        votosEncriptados.add(votoEncriptado);
    }


    public List<byte[]> getVotosEncriptados() {
        return new ArrayList<>(votosEncriptados);
    }

}