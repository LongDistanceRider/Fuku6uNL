import org.aiwolf.common.data.Role;

public class Main {
    private final static String HOST = "localhost"; // ホスト名 (kanolab 49.212.130.102)
//    private final static String HOST = "49.212.130.102"; // ホスト名 (kanolab 49.212.130.102)
    private final static int PORT = 10000;  // ポート番号
    private final static int NUMBER_OF_GAME = 1; // 試合回数
    private final static int NUMBER_OF_PLAYER = 5;  // 対戦人数

    public static void main(String[] args) throws InterruptedException {
        Starter starter = new Starter(HOST, PORT, NUMBER_OF_GAME, NUMBER_OF_PLAYER);  // ゲームスタートのインスタンス作成
        starter.serverStart();  // ローカルサーバの立ち上げ
        Thread.sleep(1000); // ここで1秒待たないと，サーバ立ち上がる前にクライアント接続してしまう
        starter.connectClient("fuku6uNL.Player.Fuku6uNL", "Fuku6u", Role.POSSESSED);
//        starter.connectClient("fuku6uNL.Player.Dammy", "Dammy", null);
//        starter.connectClient("fuku6uNL.Player.Dammy", "Dammy", null);
//        starter.connectClient("fuku6uNL.Player.Dammy", "Dammy", null);
        starter.connectClient("fuku6uNL.Player.HumanPlayer", "HumanPlayer", null);
        starter.connectClient("fuku6uNL.Player.HumanPlayer", "HumanPlayer", null);
        starter.connectClient("fuku6uNL.Player.HumanPlayer", "HumanPlayer", null);
        starter.connectClient("fuku6uNL.Player.HumanPlayer", "HumanPlayer", null);

    }
}
