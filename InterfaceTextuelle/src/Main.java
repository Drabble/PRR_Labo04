/**
 * Project: Labo04
 * Authors: Antoine Drabble & Simon Baehler & Frederic Fyfer
 * Date: 20.12.2016
 */
import java.io.IOException;
import java.util.Scanner;

/**
 * Démarre un serveur textuelle. Les paramêtres suivant doivent être fourni:
 *      portLocal portDiffusion
 */
public class Main {
    public static void main(String[] args) throws IOException {
        if(args.length != 2){
            System.out.println("Erreur! Il faut préciser le port suivi du port du serveur de diffusion en paramètres!");
        }
        short portLocal = Short.valueOf(args[0]);
        short portDiffusion = Short.valueOf(args[1]);

        TextServeur ts = new TextServeur(portLocal, portDiffusion);
        while(true){
            Scanner reader = new Scanner(System.in);  // Reading from System.in
            System.out.println("Enter a message: ");
            String n = reader.nextLine();
            ts.envoyer(n);
        }
    }
}
