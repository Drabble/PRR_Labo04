import java.io.IOException;
import java.net.*;
import java.util.Arrays;

public class TextServeur {
    DatagramSocket pointAPointSocket;
    short portLocal;
    short portDiffusion;

    public TextServeur(short portLocal, short portDiffusion) throws SocketException {
        this.portLocal = portLocal;
        this.portDiffusion = portDiffusion;
        pointAPointSocket = new DatagramSocket(portLocal);
        new Thread(() -> {
            DatagramSocket pointAPointSocket1 = null;
            try {
                pointAPointSocket1 = new DatagramSocket();
            } catch (SocketException e) {
                e.printStackTrace();
            }
            try {
                byte[] msg = new byte[232];
                DatagramPacket msgPacket = new DatagramPacket(msg, msg.length);
                pointAPointSocket1.receive(msgPacket);
                System.out.print("Nouveau message reçu : ");
                System.out.println(new String(msgPacket.getData()).substring(1));
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
        byte[] packetBuffer = new byte[1 + msg.getBytes().length+1];
        packetBuffer[0] = 0;
        packetBuffer[1] = (byte)msg.getBytes().length;
        System.arraycopy(msg.getBytes(), 0, packetBuffer, 2, msg.getBytes().length);

        System.out.println("taille : " + msg.getBytes().length);

        pointAPointSocket.send(new DatagramPacket(packetBuffer, packetBuffer.length, InetAddress.getByName("localhost"), portDiffusion));
    }
}