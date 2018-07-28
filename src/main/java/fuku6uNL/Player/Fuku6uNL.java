package fuku6uNL.Player;

import fuku6uNL.board.BoardSurface;
import fuku6uNL.listen.Listen;
import fuku6uNL.log.Log;
import org.aiwolf.common.data.Agent;
import org.aiwolf.common.data.Player;
import org.aiwolf.common.net.GameInfo;
import org.aiwolf.common.net.GameSetting;

public class Fuku6uNL implements Player {

    // BoardSurface
    private BoardSurface boardSurface;
    // Listen
    private Listen listen = new Listen();

    @Override
    public void initialize(GameInfo gameInfo, GameSetting gameSetting) {
        Log.startLog();
        Log.trace("initialize()");
        this.boardSurface = new BoardSurface(gameInfo);
    }

    @Override
    public void update(GameInfo gameInfo) {
        Log.trace("update()");
        this.boardSurface.update(gameInfo);
        listen.update(boardSurface);
    }

    @Override
    public void dayStart() {
        Log.trace("dayStart()");
    }

    @Override
    public String talk() {
        Log.trace("talk()");
        // TODO COした役職をBoardSurfaceにセットすること（狂人役職の占い師CO役職）
        // TODO COする時にObserver.opposeCOをすること
        return null;
    }

    @Override
    public Agent vote() {
        Log.trace("vote()");
        return null;
    }

    @Override
    public Agent attack() {
        Log.trace("attack()");
        return null;
    }

    @Override
    public Agent divine() {
        Log.trace("divine");
        return null;
    }

    @Override
    public Agent guard() {
        Log.trace("guard()");
        return null;
    }

    @Override
    public void finish() {
        Log.trace("finish()");
        Log.endLog();
    }

    @Override
    public String getName() {
        return "Fuku6uNL";
    }

    @Override
    public String whisper() {
        return null;
    }
}
