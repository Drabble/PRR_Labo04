import java.io.IOException;
import java.net.SocketException;
import java.util.ArrayList;

/**
 * Created by antoi on 12/20/2016.
 */
public class Main {
    public static void main(String[] args) throws IOException {

        int startSite = 4;
        ArrayList<Site> listeVoisinsSite1 = new ArrayList<>();
        ArrayList<Site> listeVoisinsSite2 = new ArrayList<>();
        ArrayList<Site> listeVoisinsSite3 = new ArrayList<>();
        ArrayList<Site> listeVoisinsSite4 = new ArrayList<>();

        short portAplLocalle1 = 1234;
        short portAplLocalle2 = 1236;
        short portAplLocalle3 = 1238;
        short portAplLocalle4 = 1240;

        short portDiffusion1 = 1235;
        short portDiffusion2 = 1237;
        short portDiffusion3 = 1239;
        short portDiffusion4 = 1241;

        Site site1 = new Site("127.0.0.1",portDiffusion1 );
        Site site2 = new Site("127.0.0.1",portDiffusion2 );
        Site site3 = new Site("127.0.0.1",portDiffusion3 );
        Site site4 = new Site("127.0.0.1",portDiffusion4 );

        listeVoisinsSite1.add(site2);
        listeVoisinsSite1.add(site3);
        listeVoisinsSite1.add(site4);

        listeVoisinsSite2.add(site1);
        listeVoisinsSite2.add(site3);

        listeVoisinsSite3.add(site1);
        listeVoisinsSite3.add(site2);
        listeVoisinsSite3.add(site4);

        listeVoisinsSite4.add(site1);
        listeVoisinsSite4.add(site3);


        if(startSite == 1)
        {
            DiffusionServeur ds1 = new DiffusionServeur(portDiffusion1,portAplLocalle1,listeVoisinsSite1);
            ds1.demarrer();
        }else if (startSite == 2)
        {
            DiffusionServeur ds2 = new DiffusionServeur(portDiffusion2,portAplLocalle2,listeVoisinsSite2);
            ds2.demarrer();
        }else if(startSite == 3)
        {
            DiffusionServeur ds3 = new DiffusionServeur(portDiffusion3,portAplLocalle3,listeVoisinsSite3);
            ds3.demarrer();
        }
        else if(startSite == 4)
        {
            DiffusionServeur ds4 = new DiffusionServeur(portDiffusion4,portAplLocalle4,listeVoisinsSite4);
            ds4.demarrer();
        }
    }
}
