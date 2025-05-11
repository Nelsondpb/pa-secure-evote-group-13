    import aa.AutoridadeApuramento;
    import ar.AutoridadeRegisto;
    import ar.ARServer;
    import eleitor.Eleitor;
    import shared.CertificadoEleitor;
    import sv.ServidorVotacao;
    import ue.UrnaEletronica;

    import java.security.*;
    import java.util.ArrayList;
    import java.util.List;
    import java.util.Scanner;

    public class Main {
        private static final int SSL_PORT = 9090;
        private static final int SERVER_START_DELAY_MS = 1500;
        private static final Scanner scanner = new Scanner(System.in);

        public static final String RED = "\033[0;31m";
        public static final String GREEN = "\033[0;32m";
        public static final String YELLOW = "\033[0;33m";
        public static final String BLUE = "\033[0;34m";
        public static final String PURPLE = "\033[0;35m";
        public static final String CYAN = "\033[0;36m";
        public static final String RESET = "\033[0m";

        public static void main(String[] args) {
            try {
                configurarSSL();

                KeyPair parChavesAR = gerarParChavesRSA();
                KeyPair parChavesAA = gerarParChavesRSA();

                AutoridadeRegisto ar = new AutoridadeRegisto(parChavesAR.getPrivate(), parChavesAR.getPublic());
                ARServer arServer = new ARServer(ar);
                AutoridadeApuramento aa = new AutoridadeApuramento(parChavesAA.getPrivate(), parChavesAA.getPublic());
                ServidorVotacao sv = new ServidorVotacao(ar, aa.getChavePublicaAA());
                UrnaEletronica ue = new UrnaEletronica(sv);

                iniciarServidorAR(arServer);
                Thread.sleep(SERVER_START_DELAY_MS);

                List<Eleitor> eleitores = new ArrayList<>();
                Eleitor eleitorAtual = null;

                while (true) {
                    exibirMenu();
                    int opcao = scanner.nextInt();
                    scanner.nextLine();

                    switch (opcao) {
                        case 1:
                            eleitorAtual = registrarEleitor(ar);
                            eleitores.add(eleitorAtual);
                            break;
                        case 2:
                            if (eleitorAtual == null) {
                                System.out.println(RED + "\n⚠️ Nenhum eleitor registrado. Registre um eleitor primeiro." + RESET);
                            } else {
                                autenticarEleitor(eleitorAtual, sv);
                            }
                            break;
                        case 3:
                            if (eleitorAtual == null || eleitorAtual.getTokenVoto() == null) {
                                System.out.println(RED + "\n⚠️ Eleitor não autenticado. Autentique primeiro." + RESET);
                            } else {
                                votar(eleitorAtual, ue, aa);
                            }
                            break;
                        case 4:
                            encerrarEApurar(ue, aa);
                            break;
                        case 5:
                            System.out.println(GREEN + "\n✅ Sistema encerrado com sucesso!" + RESET);
                            System.exit(0);
                        default:
                            System.out.println(RED + "\n⚠️ Opção inválida! Tente novamente." + RESET);
                    }
                }

            } catch (NoSuchAlgorithmException e) {
                System.err.println(RED + "❌ Erro de criptografia: " + e.getMessage() + RESET);
            } catch (Exception e) {
                System.err.println(RED + "❌ Erro crítico: " + e.getMessage() + RESET);
                e.printStackTrace();
            } finally {
                scanner.close();
            }
        }

        private static void exibirMenu() {
            System.out.println(PURPLE + "\n===  SISTEMA DE VOTAÇÃO ELETRÔNICA ===" + RESET);
            System.out.println(CYAN + "1. Registar-se como eleitor");
            System.out.println("2. Autenticar-se como eleitor");
            System.out.println("3. Votar nos candidatos");
            System.out.println("4. Encerrar votação e mostrar resultados");
            System.out.println("5. Sair" + RESET);
            System.out.print(YELLOW + "Escolha uma opção: " + RESET);
        }

        private static void configurarSSL() {
            System.setProperty("javax.net.ssl.keyStore", "certificates/keystore.p12");
            System.setProperty("javax.net.ssl.keyStorePassword", "password");
            System.setProperty("javax.net.ssl.trustStore", "certificates/keystore.p12");
            System.setProperty("javax.net.ssl.trustStorePassword", "password");
        }

        private static KeyPair gerarParChavesRSA() throws NoSuchAlgorithmException {
            KeyPairGenerator keyGen = KeyPairGenerator.getInstance("RSA");
            keyGen.initialize(2048);
            return keyGen.generateKeyPair();
        }

        private static void iniciarServidorAR(ARServer server) {
            new Thread(() -> {
                try {
                    System.out.println(BLUE + "🔒 Servidor AR iniciando na porta " + SSL_PORT + "..." + RESET);
                    server.start();
                } catch (Exception e) {
                    System.err.println(RED + "❌ Falha no servidor AR: " + e.getMessage() + RESET);
                    System.exit(1);
                }
            }).start();
        }

        private static Eleitor registrarEleitor(AutoridadeRegisto ar) throws Exception {
            System.out.print(CYAN + "\nDigite a identificação do eleitor: " + RESET);
            String identificacao = scanner.nextLine();

            Eleitor eleitor = new Eleitor(identificacao);
            CertificadoEleitor certificado = eleitor.registarNaAR();

            System.out.println(GREEN + "\n✅ Eleitor registrado com sucesso!" + RESET);
            System.out.println(BLUE + "📄 Certificado:\n" + certificado.toPemFormat() + RESET);
            return eleitor;
        }

        private static void autenticarEleitor(Eleitor eleitor, ServidorVotacao sv) throws Exception {
            eleitor.autenticarNoSV(sv);
            System.out.println(GREEN + "\n✅ Autenticação realizada com sucesso!" + RESET);
            System.out.println(BLUE + "🔑 Token de voto: " + eleitor.getTokenVoto() + RESET);
        }

        private static void votar(Eleitor eleitor, UrnaEletronica ue, AutoridadeApuramento aa) throws Exception {
            System.out.println(PURPLE + "\nCandidatos disponíveis:" + RESET);
            List<String> candidatos = ue.getConfigManager().getCandidatos();

            for (int i = 0; i < candidatos.size(); i++) {
                String nomeCandidato = candidatos.get(i);
                String info = ue.getConfigManager().getInfoCandidato(nomeCandidato);
                System.out.println(YELLOW + (i+1) + ". " + nomeCandidato + " - \"" + info + "\"" + RESET);
            }

            System.out.print(CYAN + "Escolha o número do candidato: " + RESET);
            int opcao = scanner.nextInt();
            scanner.nextLine();

            if (opcao < 1 || opcao > candidatos.size()) {
                System.out.println(RED + "\n⚠️ Opção inválida!" + RESET);
                return;
            }

            String candidato = candidatos.get(opcao-1);
            eleitor.votar(candidato, ue, aa.getChavePublicaAA());
            System.out.println(GREEN + "\n✅ Voto registrado para " + candidato + RESET);
        }

        private static void encerrarEApurar(UrnaEletronica ue, AutoridadeApuramento aa) {
            ue.encerrarVotacao();
            System.out.println(BLUE + "\n⏳ Encerrando votação..." + RESET);

            try {
                List<byte[]> votosEncriptados = ue.getVotosEncriptados();
                List<String> votosDesencriptados = aa.desencriptarVotos(votosEncriptados);
                aa.apurarVotos(votosDesencriptados);

                System.out.println(PURPLE + "\n=== RESULTADOS FINAIS ===" + RESET);
                System.out.println(CYAN + aa.gerarRelatorio() + RESET);
            } catch (Exception e) {
                System.err.println(RED + "\n❌ Erro no apuramento: " + e.getMessage() + RESET);
                e.printStackTrace();
            }
        }
    }