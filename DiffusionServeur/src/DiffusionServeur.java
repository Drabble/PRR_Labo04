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
    short ipPortLocal = 123;

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

    byte[] sondeCreateur(int taille, byte[] messageTextuel) throws UnknownHostException {
        ByteBuffer bufferSizeByte = ByteBuffer.allocate(2);
        byte[] sizeByte = bufferSizeByte.putShort(port).array();

        ByteBuffer idSiteByteBuffer = ByteBuffer.allocate(2);
        byte[] idSiteByte = idSiteByteBuffer.putShort(port).array();

        byte[] sonde = new byte[255];
        sonde[0] = 1;
        byte[] idMessage = id(port,idCpt);
        System.arraycopy(messageTextuel,1,sonde,9,messageTextuel.length-2);
        System.arraycopy(idMessage,0,sonde,1,idMessage.length-1);
        System.arraycopy(sizeByte,0, sonde,13, 2);
        System.arraycopy(idSiteByte,0, sonde,1, 4);

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

    byte[] createEcho(byte[] id) throws UnknownHostException {
        byte[] ip = new byte[4];
        ip =  Inet4Address.getLocalHost().getAddress();

        ByteBuffer bufferportByte = ByteBuffer.allocate(2);
        byte[] portByte = bufferportByte.putShort(port).array();

        byte[] echo = new byte[ip.length + portByte.length + id.length + 1];
        echo[0] = 2;

        System.arraycopy(id,0, echo,1, id.length);
        System.arraycopy(ip,0, echo,id.length + 1, ip.length);
        System.arraycopy(portByte,0, echo,id.length + ip.length + 1, portByte.length);

        return echo;
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
                        bufferSonde = sondeCreateur(packet.getData().length - 1, buffer);
                        DatagramPacket packetSonde = new DatagramPacket(bufferSonde, bufferSonde.length, InetAddress.getByName(voisin.getIp()), voisin.getPort());
                        pointAPointSocket.send(packetSonde);
                        l.put(bytesToLong(id(port,idCpt)),voisins.size());
                    }
                    break;
                case 1:
                    DatagramPacket receptionSonde = packet;
                    pointAPointSocket.receive(receptionSonde);
                    byte[] buffer2 = receptionSonde.getData();
                    byte[] messageContenu = Arrays.copyOfRange(buffer2, 9, buffer2.length-1);

                    int idRecu = bytesToLong(Arrays.copyOfRange(messageContenu, 5, 13));

                    if (l.containsKey(idRecu)) {
                        int nbVoisins = l.get(idRecu);
                        l.remove(idRecu);

                        if (nbVoisins > 1)
                            l.put(idRecu, nbVoisins - 1);
                    }
                    else {
                        byte[] buffer3 = new byte[250];
                        byte[] buffer4 = createEcho(Arrays.copyOfRange(buffer3, 1, buffer3.length - 1));
                        DatagramPacket packetEcho = new DatagramPacket(buffer4, buffer4.length, receptionSonde.getAddress(), receptionSonde.getPort());
                        pointAPointSocket.send(packetEcho);

                        byte[] bufferAppLocale = new byte[230];
                        DatagramPacket packetAppLocale = new DatagramPacket(bufferAppLocale, bufferAppLocale.length, Inet4Address.getLocalHost(), ipPortLocal);
                        pointAPointSocket.send(packetAppLocale);

                        byte[] bufferVoisin = new byte[239];
                        DatagramPacket packetVoisin;

                        for (Site site : voisins)
                            if (!site.getIp().equals(receptionSonde.getAddress()) && site.getPort() != receptionSonde.getPort()) {
                                bufferVoisin = sondeCreateur(packet.getData().length - 1, buffer);
                                packetVoisin = new DatagramPacket(bufferVoisin, bufferVoisin.length, InetAddress.getByName(site.getIp()), site.getPort());
                                pointAPointSocket.send(packetVoisin);
                            }

                        l.put(bytesToLong(Arrays.copyOfRange(messageContenu, 5, 13)), voisins.size() - 1);
                    }

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
