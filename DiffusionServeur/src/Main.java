/**
 * Project: Labo04
 * Authors: Antoine Drabble & Simon Baehler & Frederic Fyfer
 * Date: 20.12.2016
 * Object:
*       Réalisez un programme Java qui implémente l'algorithme de diffusion de messages par le
 *      paradigme sondes et échos.
 * Rapport:
 *      Chaque site se compose de 2 JVM. La première est une interface simple (textuelle)
 *      permettant d'émettre un message aux autres sites et aussi d'afficher les messages
 *      provenant des autres sites. La seconde JVM réalise le gestionnaire de diffusion, et c'est
 *      cette JVM qui connait la topologie du réseau.
 *
 *      Les messages diffusés sont des chaînes de caractères qui n'excèdent pas 230
 *      caractères.
 *      Ces sites et le réseau qui les interconnecte sont entièrement fiables.
 *      La communication entre les sites se fait uniquement par UDP point-à-point.
 *      Le réseau est général et ne support pas de mécanisme de diffusion omis celui réalisé.
 *
 *      Nous avons séparé les deux JVM dans deux projets distinct. Le premier est le gestionnaire de diffusion et le
 *      deuxième est l'interface textuelle. Nous avons défini une classe Protocole qui définit les différents
 *      types de messages envoyé (local, sonde, echo) et une classe Site qui définit un site.
 *
 *      Les mains des deux projets doivent être démarrer avec les bons paramêtres (voir javadoc).
 *      Le code du serveur de diffusion est basé sur le pseudocode donné dans la données du labo.
 *
 *      L'id d'un message suit le format suivant:
 *      ip (4 bytes) + port (2 bytes) + compteur (2 bytes)
 *
 *      L'id d'un site suit le même format que les messages mais sans compteur.
 *
 *      Exemple de paramêtres pour démarrer le parque des sites.
 *      Démarrage des interfaces textuelles :
 *      1234 1235
 *      1236 1237
 *      1238 1239
 *      1240 1241
 *
 *      Démarrage des gestionnaires de diffusion :
 *      1234 1235 127.0.0.1 1237 127.0.0.1 1239 127.0.0.1 1241
 *      1236 1237 127.0.0.1 1235 127.0.0.1 1239
 *      1238 1239 127.0.0.1 1235 127.0.0.1 1237 127.0.0.1 1241
 *      1240 1241 127.0.0.1 1235 127.0.0.1 1239
 *
 *
 */
import java.io.IOException;
import java.util.ArrayList;

/**
 * Démarre un nouveau gestionnaire de diffusion
 * Pour démarrer un nouveau serveur de diffusion. Les paramêtres suivant doivent être fourni :
 *      portLocal portDiffusion ipVoisin portVoisin [ipVoisin portVoisin]
 */
public class Main {
    public static void main(String[] args) throws IOException {
        // Récupération du port local et du port de diffusion
        if (args.length < 4 && args.length % 2 == 0) {
            System.out.println("Il faut fournir au moins le port local, le port de diffusion et un voisin. Il faut également fournir un port et une ip pour chaque voisin");
            return;
        }
        short portLocal = Short.valueOf(args[0]);
        short portDiffusion = Short.valueOf(args[1]);

        // Ajout des lieurs à la liste
        ArrayList<Site> voisins = new ArrayList<>();
        for (int i = 2; i < args.length - 1; i += 2) {
            voisins.add(new Site(args[i], Short.valueOf(args[i + 1])));
        }

        // Démarrer le serveur de diffusion
        DiffusionServeur ds = new DiffusionServeur(portDiffusion,portLocal,voisins);
        ds.demarrer();
    }
}
