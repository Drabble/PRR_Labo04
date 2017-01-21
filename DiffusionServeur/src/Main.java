import java.io.IOException;
import java.net.SocketException;

/**
 * Created by antoi on 12/20/2016.
 */
public class Main {
    public static void main(String[] args) throws IOException {
        DiffusionServeur ds = new DiffusionServeur((short)1235);
        ds.demarrer();
    }
}
