/**
 * Project: Labo04
 * Authors: Antoine Drabble & Simon Baehler & Frederic Fyfer
 * Date: 20.12.2016
 */

/**
 * Défini un site d'un parque de machine
 */
public class Site {
    private String ip;
    private int port;

    public Site(String ip, int port) {
        this.ip = ip;
        this.port = port;
    }

    public String getIp() {
        return ip;
    }

    public void setIp(String ip) {
        this.ip = ip;
    }

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }
}
