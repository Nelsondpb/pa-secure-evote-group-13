package shared;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import javax.net.ssl.SSLSocket;
import java.io.*;

/**
 * Classe  para envio e recebimento de objetos serializados
 * através de sockets SSL.
 */
public class NetworkUtils {
    private static final Logger logger = LogManager.getLogger(NetworkUtils.class);

    /**
     * Envia um objeto serializável através de um SSLSocket.
     *
     * @param socket socket seguro para envio
     * @param object objeto a ser enviado
     * @throws IOException se ocorrer erro de I/O
     */
    public static void sendObject(SSLSocket socket, Object object) throws IOException {
        try {
            ObjectOutputStream oos = new ObjectOutputStream(socket.getOutputStream());
            oos.writeObject(object);
            oos.flush();
            logger.debug("Objeto enviado para {}", socket.getInetAddress());
        } catch (IOException e) {
            logger.error("Erro ao enviar objeto: {}", e.getMessage(), e);
            throw e;
        }
    }

    /**
     * Recebe um objeto serializado de um SSLSocket.
     *
     * @param socket socket seguro para leitura
     * @return objeto recebido
     * @throws IOException se ocorrer erro de I/O
     * @throws ClassNotFoundException se a classe do objeto recebido não for encontrada
     */
    public static Object receiveObject(SSLSocket socket) throws IOException, ClassNotFoundException {
        try {
            ObjectInputStream ois = new ObjectInputStream(socket.getInputStream());
            Object obj = ois.readObject();
            logger.debug("Objeto recebido de {}", socket.getInetAddress());
            return obj;
        } catch (Exception e) {
            logger.error("Erro ao receber objeto: {}", e.getMessage(), e);
            throw e;
        }
    }
}