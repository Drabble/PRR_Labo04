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
import java.util.HashMap;

/**
 *
 */
public class DiffusionServeur {
    // Création du socket point à point pour l'envoi de packet UDP
    int port;
    DatagramSocket pointAPointSocket;
    ArrayList<Site> voisins = new ArrayList<>();
    int numberoSite = 1;
    short idCpt = 0;

    public DiffusionServeur(int port) throws SocketException {
        port = port;
        pointAPointSocket = new DatagramSocket(port);
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
                        // TODO cast id en byte array
                        // TODO ajouter le byte array packet.getData() enlever le premier byte
                        buffer = new byte[]{1, (byte)numberoSite, id, packet.getData()};
                        packet = new DatagramPacket(buffer, buffer.length, InetAddress.getByName(voisin.getIp()), voisin.getPort());
                        pointAPointSocket.send(packet);
                    }
                    break;
                case 1:
                    (ID,msg') ¬ msg -- Décomposition du contenu de msg
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
                    break;
                case 2:
                    byte[] idBuffer = {packet.getData()[1], packet.getData()[2], packet.getData()[3], packet.getData()[4], packet.getData()[5], packet.getData()[6]};
                    Retirer (msg,a) de L
                    if a > 1 then
                    L ¬ L È (msg,a-1)
                    end if
                    break;
            }
        }
    }

}
