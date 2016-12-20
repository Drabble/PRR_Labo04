import java.io.IOException;
import java.net.SocketException;
import java.util.Scanner;


public class Main {
    public static void main(String[] args) throws IOException {
        TextServeur ts = new TextServeur();
        while(true){
            Scanner reader = new Scanner(System.in);  // Reading from System.in
            System.out.println("Enter a message: ");
            String n = reader.nextLine();
            ts.envoyer(n);
        }
    }
}
