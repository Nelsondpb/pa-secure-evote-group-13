package sv;

import ar.AutoridadeRegisto;
import shared.CertificadoEleitor;
import shared.exceptions.AutenticacaoFalhouException;
import java.security.PublicKey;
import java.util.UUID;

public class ServidorVotacao {
    private final AutoridadeRegisto ar;
    private final PublicKey chavePublicaAA;
    private final TokenService tokenService;

    public ServidorVotacao(AutoridadeRegisto ar, PublicKey chavePublicaAA) {
        this.ar = ar;
        this.chavePublicaAA = chavePublicaAA;
        this.tokenService = new TokenService();
    }

    public UUID autenticarEleitor(CertificadoEleitor certificado) throws AutenticacaoFalhouException {
        try {
            System.out.println("\n🔐 Iniciando autenticação para: " + certificado.getIdentificacao());

            if (certificado == null) {
                throw new AutenticacaoFalhouException("Certificado nulo");
            }

            if (!ar.validarCertificado(certificado)) {
                throw new AutenticacaoFalhouException("Certificado inválido");
            }

            UUID token = tokenService.emitirToken();
            System.out.println("✅ Token emitido para: " + certificado.getIdentificacao());
            return token;
        } catch (Exception e) {
            throw new AutenticacaoFalhouException("Falha na autenticação: " + e.getMessage());
        }
    }

    public boolean validarToken(UUID token) {
        return tokenService.validarToken(token);
    }
}