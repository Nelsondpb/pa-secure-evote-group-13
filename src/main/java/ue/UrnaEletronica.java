package ue;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import sv.ServidorVotacao;
import shared.*;
import shared.exceptions.*;

import java.io.IOException;
import java.util.*;
import java.nio.file.*;

public class UrnaEletronica {
    private static final Logger logger = LogManager.getLogger(UrnaEletronica.class);
    private static final String VOTOS_FILE = "votos_encrypted.bin";

    private final ServidorVotacao servidorVotacao;
    private final ConfigManager configManager;
    private final List<Voto> votos = new ArrayList<>();
    private boolean votacaoEncerrada = false;

    public UrnaEletronica(ServidorVotacao servidorVotacao) {
        this.servidorVotacao = servidorVotacao;
        this.configManager = new ConfigManager();
        logger.info("Urna eletrônica inicializada. Candidatos válidos: {}",
                configManager.getCandidatos());
    }

    public void receberVoto(byte[] votoEncriptado, UUID token)
            throws TokenInvalidoException, VotoInvalidoException, VotacaoEncerradaException {

        try {
            if (votacaoEncerrada) {
                throw new VotacaoEncerradaException("Período de votação encerrado");
            }

            if (token == null || !servidorVotacao.validarToken(token)) {
                throw new TokenInvalidoException("Token inválido ou já utilizado");
            }

            if (votoEncriptado == null || votoEncriptado.length == 0) {
                throw new VotoInvalidoException("Voto encriptado é inválido");
            }

            votos.add(new Voto(votoEncriptado, token));
            persistirVoto(votoEncriptado);

            logger.info("Voto recebido e persistido. Total: {}", votos.size());
        } catch (Exception e) {
            logger.error("Erro ao processar voto: {}", e.getMessage());
            throw e;
        }
    }

    private void persistirVoto(byte[] votoEncriptado) throws VotoInvalidoException {
        try {
            Files.write(
                    Paths.get(VOTOS_FILE),
                    votoEncriptado,
                    StandardOpenOption.CREATE,
                    StandardOpenOption.APPEND
            );
        } catch (IOException e) {
            throw new VotoInvalidoException("Falha ao persistir voto: " + e.getMessage());
        }
    }

    public List<byte[]> getVotosEncriptados() {
        return new ArrayList<>(votos.stream()
                .map(Voto::getVotoEncriptado)
                .toList());
    }

    public void encerrarVotacao() {
        this.votacaoEncerrada = true;
        logger.info("Votação encerrada. Total de votos: {}", votos.size());
    }

    public boolean isVotacaoEncerrada() {
        return votacaoEncerrada;
    }

    public int getTotalVotos() {
        return votos.size();
    }

    private static class Voto {
        private final byte[] votoEncriptado;
        private final UUID token;

        public Voto(byte[] votoEncriptado, UUID token) {
            this.votoEncriptado = votoEncriptado;
            this.token = token;
        }

        public byte[] getVotoEncriptado() {
            return votoEncriptado;
        }
    }
    public ConfigManager getConfigManager() {
        return this.configManager;
    }
}