package sv;

import ar.AutoridadeRegisto;
import shared.CertificadoEleitor;
import shared.exceptions.AutenticacaoFalhouException;
import java.security.PublicKey;
import java.util.UUID;

/**
 * Representa o servidor de votação que autentica eleitores com base
 * nos seus certificados e gera tokens únicos para autorização de votos.
 */
public class ServidorVotacao {
    private final AutoridadeRegisto ar;
    private final PublicKey chavePublicaAA;
    private final TokenService tokenService;

    /**
     * Constrói o servidor de votação.
     *
     * @param ar instância da autoridade de registo
     * @param chavePublicaAA chave pública da autoridade de apuramento
     */
    public ServidorVotacao(AutoridadeRegisto ar, PublicKey chavePublicaAA) {
        this.ar = ar;
        this.chavePublicaAA = chavePublicaAA;
        this.tokenService = new TokenService();
    }

    /**
     * Autentica um eleitor através do seu certificado e emite um token único.
     *
     * @param certificado certificado do eleitor
     * @return token de autenticação
     * @throws AutenticacaoFalhouException se a autenticação falhar
     */
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

    /**
     * Valida se o token fornecido é reconhecido pelo sistema.
     *
     * @param token token a ser verificado
     * @return true se o token for válido, false caso contrário
     */
    public boolean validarToken(UUID token) {
        return tokenService.validarToken(token);
    }
}