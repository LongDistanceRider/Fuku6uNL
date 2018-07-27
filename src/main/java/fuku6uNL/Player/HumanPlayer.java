package fuku6uNL.Player;

import org.aiwolf.common.data.Agent;
import org.aiwolf.common.data.Player;
import org.aiwolf.common.net.GameInfo;
import org.aiwolf.common.net.GameSetting;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

public class HumanPlayer implements Player {
    private GameInfo gameInfo;
    @Override
    public String getName() {
        return "HumanPlayer";
    }

    @Override
    public void update(GameInfo gameInfo) {
        this.gameInfo = gameInfo;
    }

    @Override
    public void initialize(GameInfo gameInfo, GameSetting gameSetting) {
        this.gameInfo = gameInfo;
    }

    @Override
    public void dayStart() {
        if (gameInfo.getDay() == 1) {
            System.out.println("HumanPlayer_Role: " + gameInfo.getRole());
        }
    }

    @Override
    public String talk() {
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        String talk_String = null;
        BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
        try {
            System.out.print("TALK: ");
            talk_String = reader.readLine();
//            reader.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        if (talk_String.equals("s")) {
            talk_String = "Skip";
        }
        return talk_String;
    }

    @Override
    public String whisper() {
        return null;
    }

    @Override
    public Agent vote() {
        return null;
    }

    @Override
    public Agent attack() {
        return null;
    }

    @Override
    public Agent divine() {
        return null;
    }

    @Override
    public Agent guard() {
        return null;
    }

    @Override
    public void finish() {

    }
}
