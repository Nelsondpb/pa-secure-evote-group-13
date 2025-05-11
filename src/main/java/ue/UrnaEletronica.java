package ue;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import sv.ServidorVotacao;
import shared.*;
import shared.exceptions.*;

import java.io.IOException;
import java.util.*;
import java.nio.file.*;

/**
 * Representa uma urna eletrônica para recepção e persistência de votos encriptados.
 * Controla o estado da votação, valida tokens, armazena votos e gerencia os candidatos através do {@link ConfigManager}.
 */
public class UrnaEletronica {
    private static final Logger logger = LogManager.getLogger(UrnaEletronica.class);
    private static final String VOTOS_FILE = "votos_encrypted.bin";

    private final ServidorVotacao servidorVotacao;
    private final ConfigManager configManager;
    private final List<Voto> votos = new ArrayList<>();
    private boolean votacaoEncerrada = false;

    /**
     * Construtor da urna eletrônica.
     *
     * @param servidorVotacao servidor responsável pela validação de tokens
     */
    public UrnaEletronica(ServidorVotacao servidorVotacao) {
        this.servidorVotacao = servidorVotacao;
        this.configManager = new ConfigManager();
        logger.info("Urna eletrônica inicializada. Candidatos válidos: {}",
                configManager.getCandidatos());
    }

    /**
     * Recebe um voto encriptado e o armazena caso o token seja válido e a votação esteja ativa.
     *
     * @param votoEncriptado dados do voto cifrado
     * @param token token de autenticação do eleitor
     * @throws TokenInvalidoException se o token for inválido
     * @throws VotoInvalidoException se os dados do voto forem nulos ou houver falha na persistência
     * @throws VotacaoEncerradaException se a votação já estiver encerrada
     */
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

    /**
     * Persiste o voto encriptado em disco no arquivo definido.
     *
     * @param votoEncriptado voto a ser salvo
     * @throws VotoInvalidoException se houver erro de I/O
     */
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

    /**
     * Retorna a lista de votos encriptados.
     *
     * @return lista de votos
     */
    public List<byte[]> getVotosEncriptados() {
        return new ArrayList<>(votos.stream()
                .map(Voto::getVotoEncriptado)
                .toList());
    }

    /**
     * Encerra a votação, impedindo o recebimento de novos votos.
     */
    public void encerrarVotacao() {
        this.votacaoEncerrada = true;
        logger.info("Votação encerrada. Total de votos: {}", votos.size());
    }

    /**
     * Indica se a votação foi encerrada.
     *
     * @return true se encerrada, false caso contrário
     */
    public boolean isVotacaoEncerrada() {
        return votacaoEncerrada;
    }

    /**
     * Retorna o total de votos recebidos.
     *
     * @return total de votos
     */
    public int getTotalVotos() {
        return votos.size();
    }

    /**
     * Classe interna que representa um voto unitário.
     */
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

    /**
     * Retorna o gerenciador de configurações, incluindo os candidatos.
     *
     * @return {@link ConfigManager} associado
     */
    public ConfigManager getConfigManager() {
        return this.configManager;
    }
}