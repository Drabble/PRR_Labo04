import java.io.IOException;
import java.net.*;
import java.util.Arrays;

public class TextServeur {
    // 2 datagram socket ?? synchronized ?
    // Configurer adresse et port du serveur de diffusion
    DatagramSocket pointAPointSocket;

    public TextServeur() throws SocketException {
        pointAPointSocket = new DatagramSocket(1235);
        new Thread(() -> {
            DatagramSocket pointAPointSocket1 = null;
            try {
                pointAPointSocket1 = new DatagramSocket();
            } catch (SocketException e) {
                e.printStackTrace();
            }
            try {
                byte[] msg = new byte[230];
                DatagramPacket msgPacket = new DatagramPacket(msg, msg.length);
                pointAPointSocket1.receive(msgPacket);
                System.out.println("New message");
                System.out.println(new String(msgPacket.getData()));
            } catch (SocketException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }).start();
    }

    public void envoyer(String msg) throws IOException {
        if(msg.getBytes().length > 230){
            System.out.println("Le message doit faire moins de 230 caractères");
        }
        byte[] packetBuffer = new byte[1 + msg.getBytes().length];
        packetBuffer[0] = 0;
        System.arraycopy(msg.getBytes(), 0, packetBuffer, 1, msg.getBytes().length);

        pointAPointSocket.send(new DatagramPacket(packetBuffer, packetBuffer.length, InetAddress.getByName("localhost"), 1234));
    }
}