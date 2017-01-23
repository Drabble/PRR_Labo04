/**
 * Project: Labo04
 * Authors: Antoine Drabble & Simon Baehler & Frederic Fyfer
 * Date: 20.12.2016
 */
import java.io.IOException;
import java.net.*;
import java.util.Arrays;

/**
 * Interface simple (textuelle)
 * permettant d'émettre un message aux autres sites et aussi d'afficher les messages
 * provenant des autres sites
 */
public class TextServeur {
    DatagramSocket pointAPointSocket;
    short portLocal;
    short portDiffusion;

    /**
     * Crée un nouveau serveur textuel. Démarre le thread de réception des messages
     *
     * @param portLocal
     * @param portDiffusion
     * @throws SocketException
     */
    public TextServeur(short portLocal, short portDiffusion) throws SocketException {
        this.portLocal = portLocal;
        this.portDiffusion = portDiffusion;
        pointAPointSocket = new DatagramSocket(portLocal);
        new Thread(() -> {
            DatagramSocket pointAPointSocket1 = null;
            while (true) {
                try {
                    byte[] msg = new byte[232];
                    DatagramPacket msgPacket = new DatagramPacket(msg, msg.length);
                    System.out.println("Réception d'un nouveau message!");
                    pointAPointSocket.receive(msgPacket);
                    System.out.print("Nouveau message reçu : ");
                    System.out.println(new String(msgPacket.getData()).substring(1, msgPacket.getData()[0] + 1));
                } catch (SocketException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }

    /**
     * Permet d'envoyer un message aux autres serveurs textuelles connectés.
     *
     * @param msg
     * @throws IOException
     */
    public void envoyer(String msg) throws IOException {
        if(msg.getBytes().length > 230){
            System.out.println("Le message doit faire moins de 230 caractères");
        }
        byte[] packetBuffer = new byte[1 + msg.getBytes().length+1];
        packetBuffer[0] = (byte)Protocole.LOCAL.ordinal();
        packetBuffer[1] = (byte)msg.getBytes().length;
        System.arraycopy(msg.getBytes(), 0, packetBuffer, 2, msg.getBytes().length);

        System.out.println("taille : " + msg.getBytes().length);

        pointAPointSocket.send(new DatagramPacket(packetBuffer, packetBuffer.length, InetAddress.getByName("localhost"), portDiffusion));
    }
}