package shared;

import javax.net.ssl.SSLSocket;
import java.io.*;

public class NetworkUtils {
    public static void sendObject(SSLSocket socket, Object object) throws IOException {
        try {
            ObjectOutputStream oos = new ObjectOutputStream(socket.getOutputStream());
            oos.writeObject(object);
            oos.flush();
        } catch (IOException e) {
            throw new IOException("Falha ao enviar objeto: " + e.getMessage(), e);
        }
    }

    public static Object receiveObject(SSLSocket socket) throws IOException, ClassNotFoundException {
        try {
            ObjectInputStream ois = new ObjectInputStream(socket.getInputStream());
            return ois.readObject();
        } catch (IOException e) {
            throw new IOException("Falha ao receber objeto: " + e.getMessage(), e);
        }
    }
}