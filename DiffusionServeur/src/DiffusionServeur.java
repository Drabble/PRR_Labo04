/**
 * Project: Labo04
 * Authors: Antoine Drabble & Simon Baehler
 * Date: 20.12.2016
 */
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.util.Arrays;

/**
 *
 */
public class DiffusionServeur {
    // Création du socket point à point pour l'envoi de packet UDP
    int port;
    DatagramSocket pointAPointSocket;

    public DiffusionServeur(int port) throws SocketException {
        port = port;
        pointAPointSocket = new DatagramSocket(port);
    }

    public void demarrer(){
        while(true){
            byte[] buffer = new byte[250];
            DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
            try {
                pointAPointSocket.receive(packet);
            } catch (IOException e) {
                e.printStackTrace();
            }
            switch(packet.getData()[0]){
                case 0:
                    System.out.println("Received message from client");
                    System.out.println(new String(packet.getData()));
                    break;
                case 1:
                    break;
                case 2:
                    break;
            }
/*

            // Envoi du paquet de souscription
            DatagramPacket linkerSubscribePacket = new DatagramPacket(souscriptionBuffer, souscriptionBuffer.length, InetAddress.getByName(lieurs[linkerNumber].getIp()), lieurs[linkerNumber].getPort());
            pointAPointSocket.send(linkerSubscribePacket);

            // Attente de la confirmation du lieur
            byte[] buffer = new byte[1];
            DatagramPacket linkerConfirmationPacket = new DatagramPacket(buffer, buffer.length);
            try {
                pointAPointSocket.setSoTimeout(tempsMaxAttenteReponse);
                pointAPointSocket.receive(linkerConfirmationPacket);
            } catch (SocketException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
*/
        }
    }

}
