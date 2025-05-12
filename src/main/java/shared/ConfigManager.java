package shared;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Classe responsável por carregar e fornecer informações sobre os candidatos
 * a partir de um ficheiro de configuração.
 */
public class ConfigManager {
    private static final Logger logger = LogManager.getLogger(ConfigManager.class);
    private static final String CANDIDATOS_FILE = "src/main/resources/candidatos.txt";
    private List<String> candidatos;
    private Map<String, String> candidatosInfo;

    /**
     * Construtor que inicializa o carregamento dos candidatos e suas descrições.
     */
    public ConfigManager() {
        carregarCandidatos();
        carregarInfoCandidatos();
    }
    /**
     * Lê o ficheiro candidatos.txt e armazena os nomes dos candidatos.
     * Lança uma RuntimeException em caso de erro.
     */
    private void carregarCandidatos() {
        try {
            var inputStream = getClass().getClassLoader().getResourceAsStream("candidatos.txt");
            if (inputStream == null) {
                throw new IOException("Ficheiro candidatos.txt não encontrado no classpath.");
            }

            try (var reader = new java.io.BufferedReader(new java.io.InputStreamReader(inputStream))) {
                candidatos = reader.lines().toList();
            }

            logger.info("{} candidatos carregados: {}", candidatos.size(), candidatos);
        } catch (IOException e) {
            logger.error("Falha ao carregar candidatos: {}", e.getMessage());
            throw new RuntimeException("Erro crítico: não foi possível carregar candidatos", e);
        }
    }

    /**
     * Inicializa o mapa com informações descritivas sobre cada candidato.
     */
    private void carregarInfoCandidatos() {
        candidatosInfo = new HashMap<>();
        candidatosInfo.put("Donald Trump", "Make Voting Great Again.");
        candidatosInfo.put("Elon Musk", "Promete levar teu voto para Marte.");
        candidatosInfo.put("Papa Leão XIV", "Unção divina com estratégia eleitoral.");
        candidatosInfo.put("Galileu Galilei", "eppur si muove… nas pesquisas.");
        candidatosInfo.put("Albert Einstein", "Relatividade aplicada às urnas.");
        candidatosInfo.put("Arnold Schwarzenegger", "Já fui governador, por que não presidente?");
        candidatosInfo.put("Napoleão Bonaparte", "Pequeno no tamanho, gigante nas ambições.");
        candidatosInfo.put("Alexandre, o Grande", "Sempre um passo à frente");
        candidatosInfo.put("Leonardo da Vinci", "Arte, ciencia e governo.");
        candidatosInfo.put("William Shakespeare", "Caiu uma maçã, mas ele se levantou!");
        candidatosInfo.put("Isaac Newton", "Ser ou não ser eleito?");

    }
    /**
     * Retorna a lista de todos os candidatos carregados.
     *
     * @return lista de nomes de candidatos
     */
    public List<String> getCandidatos() {
        return candidatos;
    }
    /**
     * Retorna a descrição do candidato fornecido.
     *
     * @param nome nome do candidato
     * @return descrição ou \"Sem informação\" se não encontrado
     */
    public String getInfoCandidato(String nome) {
        return candidatosInfo.getOrDefault(nome, "Sem informação");
    }
    /**
     * Verifica se o nome passado pertence à lista de candidatos válidos.
     *
     * @param nome nome do candidato
     * @return true se estiver na lista, false caso contrário
     */
    public boolean isCandidatoValido(String nome) {
        return candidatos.contains(nome);
    }
}