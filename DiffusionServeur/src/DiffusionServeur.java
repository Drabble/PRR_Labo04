/**
 * Project: Labo04
 * Authors: Antoine Drabble & Simon Baehler
 * Date: 20.12.2016
 */

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
    short ipPortLocal = 1236;
    final int tailleHeader = 16;

    public DiffusionServeur(short port) throws SocketException {
        this.port = port;
        pointAPointSocket = new DatagramSocket(port);
        Site tmpSite = new Site("10.192.91.15",1237);
        //voisins.add(tmpSite);
    }


    byte[] id(short port, short cpt) throws UnknownHostException {
        byte[] id = new byte[8];
        byte[] ip = new byte[4];
        ip = Inet4Address.getLocalHost().getAddress();

        ByteBuffer bufferportByte = ByteBuffer.allocate(2);
        ByteBuffer bufferportcptByte = ByteBuffer.allocate(2);

        byte[] portByte = bufferportByte.putShort(port).array();
        byte[] cptByte = bufferportcptByte.putShort(cpt).array();

        //TODO mettre de constante
        System.arraycopy(cptByte, 0, id, 6, 2);
        System.arraycopy(portByte, 0, id, 4, 2);
        System.arraycopy(ip, 0, id, 0, 4);
        System.out.println("ip lenght : " + ip.length);
        System.out.println(Inet4Address.getLocalHost().getHostAddress());
        return id;
    }

    byte[] sondeCreateur(byte[] dataRecu) throws UnknownHostException {
        ByteBuffer bufferSizeByte = ByteBuffer.allocate(2);
        byte[] sizeByte = bufferSizeByte.putShort(port).array();
        Inet4Address.getLocalHost().getAddress();

        ByteBuffer idSiteByteBuffer = ByteBuffer.allocate(2);
        byte[] idSiteByte = new byte[6];
        byte[] SitePortByte = idSiteByteBuffer.putShort(port).array();


        System.arraycopy(Inet4Address.getLocalHost().getAddress(), 0, idSiteByte, 0, Inet4Address.getLocalHost().getAddress().length);
        System.arraycopy(SitePortByte, 0, idSiteByte, Inet4Address.getLocalHost().getAddress().length, SitePortByte.length);


        byte tailleMessage = dataRecu[1];
        byte[] sonde = new byte[dataRecu[1] + tailleHeader];
        /*System.out.println("lenght : " + dataRecu.length);
        System.out.println("sonde lenght : " + sonde.length);

        System.out.println("taille message " + dataRecu[1]);
        System.out.println("lenght idSiteByte : " + idSiteByte.length);*/


        byte[] idMessage = id(port, idCpt);

        sonde[0] = 1 ;
        System.arraycopy(idSiteByte, 0, sonde, 1, idSiteByte.length);
        System.arraycopy(idMessage, 0, sonde, idSiteByte.length+1, idMessage.length);
        sonde[15] = tailleMessage ;
        System.arraycopy(dataRecu, 2, sonde, tailleHeader, dataRecu[1]);


        return sonde;
    }

    /**
     * @param b, contains the value to convert
     * @return long, the value converted
     * <p>
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
        ip = Inet4Address.getLocalHost().getAddress();

        ByteBuffer bufferportByte = ByteBuffer.allocate(2);
        byte[] portByte = bufferportByte.putShort(port).array();

        byte[] echo = new byte[ip.length + portByte.length + id.length + 1];
        echo[0] = 2;

        System.arraycopy(id, 0, echo, 1, id.length);
        System.arraycopy(ip, 0, echo, id.length + 1, ip.length);
        System.arraycopy(portByte, 0, echo, id.length + ip.length + 1, portByte.length);

        return echo;
    }

    public void demarrer() throws IOException {
        HashMap<Integer, Integer> l = new HashMap<>();
        while (true) {
            byte[] bufferReception = new byte[250];
            DatagramPacket packet = new DatagramPacket(bufferReception, bufferReception.length);
            try {
                pointAPointSocket.receive(packet);
            } catch (IOException e) {
                e.printStackTrace();
            }
            System.out.println(packet.getData()[0]);
            switch (packet.getData()[0]) {
                case 0:
                    System.out.println("Received message from client");
                    System.out.println(new String(packet.getData()));
                    // TODO : Transformer l'id en tableau de byte de longueur 8 bytes : ip - port - cpt
                    int id = idCpt++ * 10 + numberoSite;
                    l.put(id, voisins.size());
                    for (Site voisin : voisins) {
                        byte[] bufferSonde;
                        bufferSonde = sondeCreateur(packet.getData());
                        DatagramPacket packetSonde = new DatagramPacket(bufferSonde, bufferSonde.length, InetAddress.getByName(voisin.getIp()), voisin.getPort());
                        pointAPointSocket.send(packetSonde);
                        l.put(bytesToLong(id(port, idCpt)), voisins.size());
                    }
                    break;
                case 1:
                    System.out.println("sonde reseau");
                    DatagramPacket receptionSonde = packet;

                    byte[] buffer2 = receptionSonde.getData();
                    byte[] messageContenu = Arrays.copyOfRange(buffer2, 9, buffer2.length - 1);

                    for(int i = 0 ; i< 25; i++)
                        System.out.println(i + " " + bufferReception[i]);
                    int taille = bufferReception[15];
                    byte[] messagePourAppLocal = new byte[taille+1];
                    ;



                    int idRecu = bytesToLong(Arrays.copyOfRange(messageContenu, 5, 13));

                    if (l.containsKey(idRecu)) {
                        int nbVoisins = l.get(idRecu);
                        l.remove(idRecu);

                        if (nbVoisins > 1)
                            l.put(idRecu, nbVoisins - 1);
                    } else {
                        byte[] buffer3 = new byte[250];
                        byte[] buffer4 = createEcho(Arrays.copyOfRange(buffer3, 1, buffer3.length - 1));
                        DatagramPacket packetEcho = new DatagramPacket(buffer4, buffer4.length, receptionSonde.getAddress(), receptionSonde.getPort());
                        pointAPointSocket.send(packetEcho);

                        byte[] bufferAppLocale = new byte[230];
                        bufferAppLocale[0] = (byte)taille;
                        byte[] message = Arrays.copyOfRange(bufferReception,tailleHeader+1,tailleHeader+1+taille);
                        System.arraycopy(message, 0, bufferAppLocale, 1, message.length);
                        DatagramPacket packetAppLocale = new DatagramPacket(bufferAppLocale, bufferAppLocale.length, Inet4Address.getLocalHost(), ipPortLocal);
                        System.out.print("send to local");
                        pointAPointSocket.send(packetAppLocale);

                        byte[] bufferVoisin = new byte[239];


                        DatagramPacket packetVoisin;

                        for (Site site : voisins)
                            if (!site.getIp().equals(receptionSonde.getAddress()) && site.getPort() != receptionSonde.getPort()) {
                                bufferVoisin = sondeCreateur(bufferReception);
                                packetVoisin = new DatagramPacket(bufferVoisin, bufferVoisin.length, InetAddress.getByName(site.getIp()), site.getPort());
                                pointAPointSocket.send(packetVoisin);
                            }

                        l.put(bytesToLong(Arrays.copyOfRange(messageContenu, 5, 13)), voisins.size() - 1);
                    }
                    break;

                case 2:
                    receptionSonde = packet;
                    byte[] bufferSonde = receptionSonde.getData();
                    idRecu = bytesToLong(Arrays.copyOfRange(bufferSonde, 1, 5));
                    System.out.println(idRecu);
                    int nbVoisins = l.get(idRecu);
                    l.remove(idRecu);

                    if (nbVoisins > 1)
                        l.put(idRecu, nbVoisins - 1);
            }
        }
    }

}
