/**
 * Project: Labo04
 * Authors: Antoine Drabble & Simon Baehler
 * Date: 20.12.2016
 */
import javafx.util.Pair;

import java.io.IOException;
import java.net.*;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;

/**
 *
 */
public class DiffusionServeur {
    // Création du socket point à point pour l'envoi de packet UDP
    short port;
    DatagramSocket pointAPointSocket;
    ArrayList<Site> voisins = new ArrayList<>();
    int numberoSite = 1;
    short idCpt = 0;

    public DiffusionServeur(int port) throws SocketException {
        port = port;
        pointAPointSocket = new DatagramSocket(port);
    }


    byte[] id (short port, short cpt) throws UnknownHostException {
        byte[] id = new byte[8];
        byte[] ip = new byte[4];
        ip =  Inet4Address.getLocalHost().getAddress();

        ByteBuffer bufferportByte = ByteBuffer.allocate(2);
        ByteBuffer bufferportcptByte = ByteBuffer.allocate(2);

        byte[] portByte = bufferportByte.putShort(port).array();
        byte[] cptByte = bufferportcptByte.putShort(cpt).array();

        //TODO mettre de constante
        System.arraycopy(cptByte,0,id,6,2);
        System.arraycopy(portByte,0,id,4,2);
        System.arraycopy(ip,0,id,0,4);
        System.out.println(ip.length);
        System.out.println(Inet4Address.getLocalHost().getHostAddress());
        return id;
    }

    byte[] sondeCreateur(byte[] message) throws UnknownHostException {
        byte[] sonde = new byte[9+message.length-2];
        sonde[0] = 1;
        byte[] idMessage = new byte[8];
        idMessage = id(port,idCpt);
        System.arraycopy(idMessage,0,sonde,1,idMessage.length-1);
        System.arraycopy(message,1,sonde,9,message.length-2);

        return sonde;
    }

    /**
     *
     * @param b, contains the value to convert
     * @return long, the value converted
     *
     * This method converts bytes to a long.
     */
    static int bytesToLong(byte[] b) {
        int result = 0;
        for (int i = 0; i < 8; i++) {
            result <<= 8;
            result |= (b[i] & 0xFF);
        }
        return result;
    }

    public void demarrer() throws IOException {
        HashMap<Integer,Integer> l = new HashMap<>();
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
                    // TODO : Transformer l'id en tableau de byte de longueur 8 bytes : ip - port - cpt
                    int id = idCpt++ * 10 + numberoSite;
                    l.put(id, voisins.size());
                    for(Site voisin : voisins){
                        byte[] bufferSonde ;
                        bufferSonde = sondeCreateur(buffer);
                        packet = new DatagramPacket(bufferSonde, bufferSonde.length, InetAddress.getByName(voisin.getIp()), voisin.getPort());
                        pointAPointSocket.send(packet);
                        l.put(bytesToLong(id(port,idCpt)),voisins.size());
                    }
                    break;
                case 1:
                    pointAPointSocket.receive(packet);
                    byte[] buffer2 = packet.getData();
                    Arrays.copyOfRange(packet.getData(), 9, buffer2.length-1);
                    /*(ID,msg') ¬ msg -- Décomposition du contenu de msg
                    if ID Î L then
                        Retirer (ID,a) de L
                        if a > 1 then
                            L ¬ L È (ID,a-1)
                        end if
                    else
                        "j Î Voisini - {émetteur}, envoyer(sonde,i,msg) à j
                        envoyer(écho,i,ID) à émetteur
                        envoyer(msg') à l'application locale
                        L ¬ L È (ID,| Voisini | - 1)
                    end if
                    break;*/
                case 2:
                    byte[] idBuffer = {packet.getData()[1], packet.getData()[2], packet.getData()[3], packet.getData()[4], packet.getData()[5], packet.getData()[6]};
                    /*Retirer (msg,a) de L
                    if a > 1 then
                    L ¬ L È (msg,a-1)
                    end if
                    break;*/
            }
        }
    }

}
