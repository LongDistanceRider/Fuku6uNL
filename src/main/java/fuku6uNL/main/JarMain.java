package fuku6uNL.main;

import org.aiwolf.common.data.Player;
import org.aiwolf.common.data.Role;
import org.aiwolf.common.net.TcpipClient;

public class JarMain {
    private final static String HOST = "49.212.130.102"; // ホスト名 (kanolab 49.212.130.102)
    private final static int PORT = 10000;  // ポート番号

    public static void main(String[] args) throws InterruptedException {
        connectClient("fuku6uNL.Player.Fuku6uNL", "Fuku6u", null);
    }

    private static void connectClient(String classPass, String playerName, Role role) {
        TcpipClient client = new TcpipClient(HOST, PORT, role);
        Class<?> class_;
        Player player = null;
        try {
            class_ = Class.forName(classPass);
            player = (Player) class_.newInstance();
        } catch (InstantiationException | IllegalAccessException | ClassNotFoundException e) {
            e.printStackTrace();
        }
        client.connect(player);
        client.setName(playerName);
        System.out.println(playerName + " is connected");
    }
}
