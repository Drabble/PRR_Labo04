/**
 * Project: Labo04
 * Authors: Antoine Drabble & Simon Baehler & Frederic Fyfer
 * Date: 20.12.2016
 */
import java.io.IOException;
import java.net.*;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;

/**
 * Gestionnaire de diffusion de messages pour le paradigme sondes et échos
 */
public class DiffusionServeur {
    // Création du socket point à point pour l'envoi de packet UDP
    short port;
    DatagramSocket pointAPointSocket;
    ArrayList<Site> voisins = new ArrayList<>();
    short idCpt = 0;
    short portLocal;
    final int tailleHeader = 16;
    final int tailleEcho = 15;
    byte[] idSite;
    final int DEBUT_IDSITE = 1;
    final int FIN_IDSITE = 6;
    final int DEBUT_IDMESSAGE = 7;
    final int FIN_IDMESSAGE = 14;
    final int TAILLE_POSITION = 15;

    /**
     * Création d'un nouveau gestionnaire de diffusion
     *
     * @param port
     * @param portLocal
     * @param voisins
     * @throws SocketException
     */
    public DiffusionServeur(short port,short portLocal, ArrayList<Site> voisins) throws SocketException {
        this.port = port;
        pointAPointSocket = new DatagramSocket(port);
        this.portLocal = portLocal;
        this.voisins = voisins;
        System.out.println("Démarrage du site " + port + "...");
    }

    /**
     * Fonction de création d'id de message.
     * Un id de message est construit de la façon suivante :
     * taille de 8 bytes
     * 4 pour l'ip du serveur expéditeur
     * 2 pour le port d'expédition
     * 2 pour compter
     *
     * @param -
     * @throws UnknownHostException
     */
    byte[] idMessage() throws UnknownHostException {
        byte[] id = new byte[8];
        int ip = Inet4Address.getLocalHost().hashCode();

        // Allocation de l'espace nécessaire pour les trois bufers.
        ByteBuffer bufferIPByte = ByteBuffer.allocate(4);
        ByteBuffer bufferPortByte = ByteBuffer.allocate(2);
        ByteBuffer bufferCptByte = ByteBuffer.allocate(2);

        // Transformation en tableau de bytes.
        byte[] IPByte = bufferIPByte.putInt(ip).array();
        byte[] portByte = bufferPortByte.putShort(port).array();
        byte[] cptByte = bufferCptByte.putShort(idCpt).array();

        // Remplissage des tableaux de bytes avec les données.
        System.arraycopy(IPByte, 0, id, 0, IPByte.length);
        System.arraycopy(portByte, 0, id, IPByte.length, portByte.length);
        System.arraycopy(cptByte, 0, id, IPByte.length+portByte.length, cptByte.length);

        return id;
    }

    /**
     * Fonction de création d'id de site.
     * Un id de message est construit de la façon suivante :
     * taille de 6 bytes
     * 4 pour l'ip du serveur expéditeur
     * 2 pour le port d'expédition
     *
     * @param -
     * @throws UnknownHostException
     */
    byte[] idSite() throws UnknownHostException {
        byte[] id = new byte[6];
        int ip = Inet4Address.getLocalHost().hashCode();

        // Allocation de l'espace nécessaire pour les deux bufers.
        ByteBuffer bufferIPByte = ByteBuffer.allocate(4);
        ByteBuffer bufferPortByte = ByteBuffer.allocate(2);

        // Transformation en tableau de bytes.
        byte[] IPByte = bufferIPByte.putInt(ip).array();
        byte[] portByte = bufferPortByte.putShort(port).array();

        // Remplissage des tableaux de bytes avec les données.
        System.arraycopy(IPByte, 0, id, 0, IPByte.length);
        System.arraycopy(portByte, 0, id, IPByte.length, portByte.length);

        return id;
    }

    /**
     * Fonction de création d'id de sonde.
     * une sonde et fait de la manière suivante :
     * 1er byte : indique que le packet sera de type sonde (pur différentier du type echo et local)
     * 6 bytes pour l'id du site
     * 8 bytes pour l'id du message
     * 1 byte pour la taille du message
     *
     * @param dataRecu dataRecu, byte tailleMessage
     * @throws UnknownHostException
     */
    byte[] sondeCreateur(byte[] dataRecu, byte tailleMessage) throws UnknownHostException {
        // Création des buffers et remplissage avec les données.
        ByteBuffer idSiteByteBuffer = ByteBuffer.allocate(2);
        byte[] idSiteByte = new byte[6];
        byte[] SitePortByte = idSiteByteBuffer.putShort(port).array();
        System.arraycopy(Inet4Address.getLocalHost().getAddress(), 0, idSiteByte, 0, Inet4Address.getLocalHost().getAddress().length);
        System.arraycopy(SitePortByte, 0, idSiteByte, Inet4Address.getLocalHost().getAddress().length, SitePortByte.length);

        byte[] sonde = new byte[dataRecu[1] + tailleHeader];
        byte[] idMessage = idMessage();

        sonde[0] = (byte)Protocole.SONDE.ordinal();
        System.arraycopy(idSiteByte, 0, sonde, DEBUT_IDSITE, idSiteByte.length);
        System.arraycopy(idMessage, 0, sonde, idSiteByte.length+1, idMessage.length);

        // Remplissage du bit dédié à la taille du message.
        sonde[TAILLE_POSITION] = tailleMessage;
        System.arraycopy(dataRecu, 2, sonde, tailleHeader, dataRecu[1]);

        return sonde;
    }

    /**
     * Fonction de création d'id d'echo.
     * un echo et fait de la manière suivante :
     * 1er byte : indique que le packet sera de type echo (pur différentier du type sonde et local)
     * 6 bytes pour l'id du site
     * 8 bytes pour l'id du message
     *
     * @param dataRecu dataRecu
     * @throws UnknownHostException
     */
    byte[] creationEcho(byte[] dataRecu) throws UnknownHostException {
        // Création des buffers.
        byte[] echo = new byte[tailleEcho];
        ByteBuffer idSiteByteBuffer = ByteBuffer.allocate(2);
        byte[] SitePortByte = idSiteByteBuffer.putShort(port).array();
        byte[] idSiteByte = new byte[6];
        byte idMessage[] = Arrays.copyOfRange(dataRecu,DEBUT_IDMESSAGE,FIN_IDMESSAGE+1);

        // Remplissage avec les données.
        System.arraycopy(Inet4Address.getLocalHost().getAddress(), 0, idSiteByte, 0, Inet4Address.getLocalHost().getAddress().length);
        System.arraycopy(SitePortByte, 0, idSiteByte, Inet4Address.getLocalHost().getAddress().length, SitePortByte.length);

        echo[0] = (byte)Protocole.ECHO.ordinal();
        System.arraycopy(idSiteByte, 0, echo, 1, idSiteByte.length);
        System.arraycopy(idMessage, 0, echo, FIN_IDSITE+1, idMessage.length);

        return echo;
    }

    /**
     * Transforme un byte en int
     *
     * @param b
     * @return
     */
    public int byteToInt(byte[] b)
    {
        int value= 0;
        for(int i=0; i<b.length; i++)
            value = (value << 8) | b[i];
        return value;
    }

    /**
     * Fonction de decortication de message
     * elle recupère l'id du message reçu
     *
     * @param ReceptionSonde ReceptionSonde
     * @throws
     */
    public Long getIDMessage(byte[] ReceptionSonde)
    {
        int IPExp = byteToInt(Arrays.copyOfRange(ReceptionSonde,0,4));
        short portExp = ByteBuffer.wrap(Arrays.copyOfRange(ReceptionSonde,4,6)).asShortBuffer().get();
        short CptExp = ByteBuffer.wrap(Arrays.copyOfRange(ReceptionSonde,6,8)).asShortBuffer().get();
        String idRecuString = Integer.toString(IPExp) + Integer.toString(portExp) + Integer.toString(CptExp);
        return Long.parseLong(idRecuString);
    }

    /**
     * Démarre le serveur de diffusion
     *
     * @throws IOException
     */
    public void demarrer() throws IOException {
        HashMap<Long, Integer> l = new HashMap<>();
        Long idRecu;
        idSite = idSite();
        System.out.println("Site " + port + " démarré!");

        while (true) {
            // Préparation du buffer et packet permettant la réception du message.
            byte[] bufferReception = new byte[250];
            DatagramPacket packet = new DatagramPacket(bufferReception, bufferReception.length);

            // Réception du packet.
            try {
                pointAPointSocket.receive(packet);
            } catch (IOException e) {
                e.printStackTrace();
            }

            // Récupération des données contenues dans le packet.
            byte[] ReceptionSonde = packet.getData();
            System.out.println("--------------------------------------------");
            System.out.println("Réception d'un message de type " + packet.getData()[0]);

            // Si il s'agit d'un message de type LOCAL.
            if(packet.getData()[0] == Protocole.LOCAL.ordinal()) {
                System.out.print("Recu un message d'un client : ");
                System.out.println(new String(packet.getData()).substring(2, packet.getData()[1] + 2));
                idCpt++;
                String ID = Integer.toString(Inet4Address.getLocalHost().hashCode()) + Integer.toString(port) + Integer.toString(idCpt);

                System.out.println("Id expedié : " + ID);
                l.put(Long.parseLong(ID), voisins.size());

                // Envoie d'une sonde à tous les voisins.
                for (Site voisin : voisins) {
                    byte[] bufferSonde;
                    byte tailleMessage = packet.getData()[1];
                    bufferSonde = sondeCreateur(packet.getData(), tailleMessage);
                    System.out.println("Envoi du paquet au voisin " + voisin.getIp() + ":" + voisin.getPort());
                    DatagramPacket packetSonde = new DatagramPacket(bufferSonde, bufferSonde.length, InetAddress.getByName(voisin.getIp()), voisin.getPort());
                    pointAPointSocket.send(packetSonde);
                }
            // Si il s'agit dun message de type SONDE.
            } else if(packet.getData()[0] == Protocole.SONDE.ordinal()) {
                int taille = bufferReception[TAILLE_POSITION];

                idRecu = getIDMessage(Arrays.copyOfRange(ReceptionSonde, DEBUT_IDMESSAGE, FIN_IDMESSAGE + 1));
                System.out.println("Id expedié : " + idRecu);

                // Suppression de l'ID reçu si celui ci est déjà présent.
                if (l.containsKey(idRecu)) {
                    int nbVoisins = l.get(idRecu);
                    l.remove(idRecu);

                    if (nbVoisins > 1) {
                        System.out.println("Voisin -1 sur " + idRecu + " " + (nbVoisins - 1));
                        l.put(idRecu, nbVoisins - 1);
                    }
                } else {
                    // Renvoi a l'expediteur
                    byte[] echoPacketBuffer = creationEcho(packet.getData());
                    byte[] forwardSondeBuffer = packet.getData();
                    DatagramPacket packetEcho = new DatagramPacket(echoPacketBuffer, echoPacketBuffer.length, packet.getAddress(), packet.getPort());
                    pointAPointSocket.send(packetEcho);

                    // Envoi vers l'app local
                    byte[] message = Arrays.copyOfRange(packet.getData(), tailleHeader - 1, tailleHeader + taille);
                    System.out.print(packet.getData()[tailleHeader - 1]);
                    DatagramPacket packetAppLocale = new DatagramPacket(message, message.length, Inet4Address.getLocalHost(), portLocal);
                    System.out.println("Envoi à l'application local sur le port " + portLocal);
                    pointAPointSocket.send(packetAppLocale);

                    // Envoi voisin sauf l'expediteur
                    DatagramPacket packetVoisin;
                    System.out.println(packet.getAddress().getHostAddress());
                    for (Site site : voisins)
                        if (!(site.getIp().equals(packet.getAddress().getHostAddress()) && site.getPort() == packet.getPort())) {
                            System.arraycopy(idSite, 0, forwardSondeBuffer, DEBUT_IDSITE, idSite.length);
                            packetVoisin = new DatagramPacket(forwardSondeBuffer, forwardSondeBuffer.length, InetAddress.getByName(site.getIp()), site.getPort());
                            pointAPointSocket.send(packetVoisin);
                            System.out.println("Forward du message reçu au voisin " + site.getIp() + " " + site.getPort());
                        }
                }
                l.put(idRecu, voisins.size() - 1);
            // Si il s'agit d'un message de type ECHO.
            } else if(packet.getData()[0] == Protocole.ECHO.ordinal()){
                    idRecu = getIDMessage(Arrays.copyOfRange(ReceptionSonde,DEBUT_IDMESSAGE,FIN_IDMESSAGE+1));
                    System.out.println("Id reçu dans l'echo : " + idRecu);
                    int nbVoisins = l.get(idRecu);
                    l.remove(idRecu);

                    if (nbVoisins > 1)
                        l.put(idRecu, nbVoisins - 1);
                    else
                        System.out.println("Fin de transmission du message");
            }
        }
    }

}
