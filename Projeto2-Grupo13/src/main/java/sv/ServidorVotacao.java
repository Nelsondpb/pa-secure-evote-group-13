package sv;

import ar.AutoridadeRegisto;
import shared.CertificadoEleitor;
import shared.exceptions.AutenticacaoFalhouException;
import shared.exceptions.CertificadoInvalidoException;

import java.security.PublicKey;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class ServidorVotacao {
    private final AutoridadeRegisto autoridadeRegisto;
    private final PublicKey chavePublicaAA;
    private final Map<String, UUID> tokensEmitidos;

    public ServidorVotacao(AutoridadeRegisto autoridadeRegisto, PublicKey chavePublicaAA) {
        this.autoridadeRegisto = autoridadeRegisto;
        this.chavePublicaAA = chavePublicaAA;
        this.tokensEmitidos = new HashMap<>();
    }

    public UUID autenticarEleitor(CertificadoEleitor certificado) throws AutenticacaoFalhouException {
        try {
            if (!autoridadeRegisto.validarCertificado(certificado)) {
                throw new AutenticacaoFalhouException("Certificado inválido");
            }

            if (tokensEmitidos.containsKey(certificado.getNumeroSerie())) {
                throw new AutenticacaoFalhouException("Eleitor já emitiu um voto");
            }

            UUID token = UUID.randomUUID();
            tokensEmitidos.put(certificado.getNumeroSerie(), token);
            return token;
        } catch (CertificadoInvalidoException e) {
            throw new AutenticacaoFalhouException("Falha na autenticação: " + e.getMessage());
        }
    }

    public boolean validarToken(UUID token) {
        return tokensEmitidos.containsValue(token);
    }

    public PublicKey getChavePublicaAA() {
        return chavePublicaAA;
    }
}