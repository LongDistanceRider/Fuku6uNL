package fuku6uNL.Player;

import fuku6uNL.board.BoardSurface;
import fuku6uNL.log.Log;
import org.aiwolf.common.data.Agent;
import org.aiwolf.common.data.Player;
import org.aiwolf.common.net.GameInfo;
import org.aiwolf.common.net.GameSetting;

public class Fuku6uNL implements Player {

    // BoardSurface
    private BoardSurface boardSurface;

    @Override
    public void initialize(GameInfo gameInfo, GameSetting gameSetting) {
        Log.startLog();
        Log.trace("initialize()");
        this.boardSurface = new BoardSurface(gameInfo);
    }

    @Override
    public void update(GameInfo gameInfo) {

    }

    @Override
    public void dayStart() {

    }

    @Override
    public String talk() {
        return null;
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

    @Override
    public String getName() {
        return "Fuku6uNL";
    }
}
