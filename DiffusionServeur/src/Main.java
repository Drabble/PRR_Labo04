import java.net.SocketException;

/**
 * Created by antoi on 12/20/2016.
 */
public class Main {
    public static void main(String[] args) throws SocketException {
        DiffusionServeur ds = new DiffusionServeur(1234);
        ds.demarrer();
    }
}
