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
    short idCpt = 0;
    short ipPortLocal = 1236;
    final int tailleHeader = 16;
    final int tailleEcho = 15;

    public DiffusionServeur(short port) throws SocketException {
        this.port = port;
        pointAPointSocket = new DatagramSocket(port);
        Site tmpSite = new Site("127.0.1.1",1237);
        //voisins.add(tmpSite);
    }


    byte[] idMessage() throws UnknownHostException {
        byte[] id = new byte[8];
        int ip = Inet4Address.getLocalHost().hashCode();

        ByteBuffer bufferIPByte = ByteBuffer.allocate(4);
        ByteBuffer bufferPortByte = ByteBuffer.allocate(2);
        ByteBuffer bufferCptByte = ByteBuffer.allocate(2);

        byte[] IPByte = bufferIPByte.putInt(ip).array();
        byte[] portByte = bufferPortByte.putShort(port).array();

        byte[] cptByte = bufferCptByte.putShort(idCpt).array();

        //TODO mettre de constante
        System.out.print(port);
        System.arraycopy(IPByte, 0, id, 0, 4);
        System.arraycopy(portByte, 0, id, 4, 2);
        System.arraycopy(cptByte, 0, id, 6, 2);

        return id;
    }
    byte[] idSite() throws UnknownHostException {
        byte[] id = new byte[6];
        int ip = Inet4Address.getLocalHost().hashCode();

        ByteBuffer bufferIPByte = ByteBuffer.allocate(4);
        ByteBuffer bufferPortByte = ByteBuffer.allocate(2);

        byte[] IPByte = bufferIPByte.putInt(ip).array();
        byte[] portByte = bufferPortByte.putShort(port).array();

        //TODO mettre de constante
        System.out.print(port);
        System.arraycopy(IPByte, 0, id, 0, 4);
        System.arraycopy(portByte, 0, id, 4, 2);

        return id;
    }

    /*
    * sondeCreateur
     */
    byte[] sondeCreateur(byte[] dataRecu, byte tailleMessage) throws UnknownHostException {

        ByteBuffer idSiteByteBuffer = ByteBuffer.allocate(2);
        byte[] idSiteByte = new byte[6];
        byte[] SitePortByte = idSiteByteBuffer.putShort(port).array();


        System.arraycopy(Inet4Address.getLocalHost().getAddress(), 0, idSiteByte, 0, Inet4Address.getLocalHost().getAddress().length);
        System.arraycopy(SitePortByte, 0, idSiteByte, Inet4Address.getLocalHost().getAddress().length, SitePortByte.length);

        byte[] sonde = new byte[dataRecu[1] + tailleHeader];

        byte[] idMessage = idMessage();

        sonde[0] = 1 ;
        System.arraycopy(idSiteByte, 0, sonde, 1, idSiteByte.length);
        System.arraycopy(idMessage, 0, sonde, idSiteByte.length+1, idMessage.length);
        sonde[15] = tailleMessage ;
        System.arraycopy(dataRecu, 2, sonde, tailleHeader, dataRecu[1]);

        return sonde;
    }



    /*
    * createEcho
    * Permet de créer un message de type echo
    * un message echo est constitué comme cela : |Type de message(1)|id site(6)|id message(8)|
     */
    byte[] createEcho(byte[] dataRecu) throws UnknownHostException {

        byte[] echo = new byte[tailleEcho];

        ByteBuffer idSiteByteBuffer = ByteBuffer.allocate(2);
        byte[] SitePortByte = idSiteByteBuffer.putShort(port).array();

        byte[] idSiteByte = new byte[6];

        byte idMessage[] = Arrays.copyOfRange(dataRecu,7,15);

        System.arraycopy(Inet4Address.getLocalHost().getAddress(), 0, idSiteByte, 0, Inet4Address.getLocalHost().getAddress().length);
        System.arraycopy(SitePortByte, 0, idSiteByte, Inet4Address.getLocalHost().getAddress().length, SitePortByte.length);

        echo[0] = 2 ;
        System.arraycopy(idSiteByte, 0, echo, 1, idSiteByte.length);
        System.arraycopy(idMessage, 0, echo, 7, idMessage.length);

        return echo;
    }
    public int convertByteToInt(byte[] b)
    {
        int value= 0;
        for(int i=0; i<b.length; i++)
            value = (value << 8) | b[i];
        return value;
    }


    public void demarrer() throws IOException {
        HashMap<Integer, Integer> l = new HashMap<>();
        int IPExp;
        short portExp;
        short CptExp;
        String idRecuString;
        int idRecu;
        byte[] idSite = idSite();
        while (true) {
            byte[] bufferReception = new byte[250];
            DatagramPacket packet = new DatagramPacket(bufferReception, bufferReception.length);
            try {
                pointAPointSocket.receive(packet);
            } catch (IOException e) {
                e.printStackTrace();
            }
            byte[] ReceptionSonde = packet.getData();
            System.out.println(packet.getData()[0]);
            switch (packet.getData()[0]) {
                case 0:
                    System.out.println("Received message from client");
                    System.out.println(new String(packet.getData()));
                    idCpt++;
                    String ID = Integer.toString(Inet4Address.getLocalHost().hashCode()) + Integer.toString(port) + Integer.toString(idCpt);
                    //TODO c'est pas bien !!!
                    l.put((int) Long.parseLong(ID), voisins.size());
                    for (Site voisin : voisins) {
                        byte[] bufferSonde;
                        byte tailleMessage = packet.getData()[1];
                        bufferSonde = sondeCreateur(packet.getData(),tailleMessage);
                        DatagramPacket packetSonde = new DatagramPacket(bufferSonde, bufferSonde.length, InetAddress.getByName(voisin.getIp()), voisin.getPort());
                        pointAPointSocket.send(packetSonde);
                    }
                    break;
                case 1:
                    System.out.println("sonde reseau");
                    int taille = bufferReception[15];

                    //TODO Faire une fonction "reconstruction idMessage
                    IPExp = convertByteToInt(Arrays.copyOfRange(ReceptionSonde,7,11));
                    portExp = ByteBuffer.wrap(Arrays.copyOfRange(ReceptionSonde,11,13)).asShortBuffer().get();
                    CptExp = ByteBuffer.wrap(Arrays.copyOfRange(ReceptionSonde,13,15)).asShortBuffer().get();
                    idRecuString = Integer.toString(IPExp) + Integer.toString(portExp) + Integer.toString(CptExp);
                    idRecu = (int) Long.parseLong(idRecuString);

                    if (l.containsKey(idRecu)) {
                        int nbVoisins = l.get(idRecu);
                        l.remove(idRecu);

                        if (nbVoisins > 1)
                            l.put(idRecu, nbVoisins - 1);
                    } else {

                        //renvoi a l'expediteur
                        byte[] echoPacketBuffer = createEcho(packet.getData());
                        byte[] forwardSondeBuffer = packet.getData();
                        DatagramPacket packetEcho = new DatagramPacket(echoPacketBuffer, echoPacketBuffer.length, packet.getAddress(), packet.getPort());
                        pointAPointSocket.send(packetEcho);

                        //envoi vers l'app local
                        byte[] message = Arrays.copyOfRange(packet.getData(),tailleHeader-1,tailleHeader+taille);
                        DatagramPacket packetAppLocale = new DatagramPacket(message, message.length, Inet4Address.getLocalHost(), ipPortLocal);
                        System.out.print("send to local");
                        pointAPointSocket.send(packetAppLocale);

                        //envoi voisin - expediteur
                        byte[] bufferVoisin;
                        DatagramPacket packetVoisin;
                        for (Site site : voisins)
                            if (!(site.getIp().equals(packet.getAddress()) && site.getPort() == packet.getPort())) {
                                System.arraycopy(idSite, 0, forwardSondeBuffer, 1, idSite.length);
                                packetVoisin = new DatagramPacket(forwardSondeBuffer, forwardSondeBuffer.length, InetAddress.getByName(site.getIp()), site.getPort());
                                pointAPointSocket.send(packetVoisin);
                            }

                        l.put(idRecu, voisins.size() - 1);
                    }
                    break;

                case 2:

                    IPExp = convertByteToInt(Arrays.copyOfRange(ReceptionSonde,7,11));
                    portExp = ByteBuffer.wrap(Arrays.copyOfRange(ReceptionSonde,11,13)).asShortBuffer().get();
                    CptExp = ByteBuffer.wrap(Arrays.copyOfRange(ReceptionSonde,13,15)).asShortBuffer().get();

                    idRecuString = Integer.toString(IPExp) + Integer.toString(portExp) + Integer.toString(CptExp);
                    idRecu = (int) Long.parseLong(idRecuString);

                    System.out.println("id recu Echo : " + idRecu);
                    int nbVoisins = l.get(idRecu);
                    l.remove(idRecu);

                    if (nbVoisins > 1)
                        l.put(idRecu, nbVoisins - 1);
                    else
                        System.out.println("fin de transmission du message");
            }
        }
    }

}
