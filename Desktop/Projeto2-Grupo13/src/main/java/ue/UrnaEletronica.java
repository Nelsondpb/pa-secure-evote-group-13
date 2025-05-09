package ue;

import sv.ServidorVotacao;
import shared.exceptions.TokenInvalidoException;
import shared.exceptions.VotoInvalidoException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class UrnaEletronica {
    private final ServidorVotacao servidorVotacao;
    private final List<Voto> votos = new ArrayList<>();

    public UrnaEletronica(ServidorVotacao servidorVotacao) {
        this.servidorVotacao = servidorVotacao;
    }

    public void receberVoto(byte[] votoEncriptado, UUID token)
            throws TokenInvalidoException, VotoInvalidoException {
        if (!servidorVotacao.validarToken(token)) {
            throw new TokenInvalidoException("Token inválido ou já utilizado");
        }
        if (votoEncriptado == null || votoEncriptado.length == 0) {
            throw new VotoInvalidoException("Voto encriptado é inválido");
        }
        votos.add(new Voto(votoEncriptado, token));
    }

    public List<byte[]> getVotosEncriptados() {
        List<byte[]> votosEncriptados = new ArrayList<>();
        for (Voto voto : votos) {
            votosEncriptados.add(voto.getVotoEncriptado());
        }
        return votosEncriptados;
    }
}