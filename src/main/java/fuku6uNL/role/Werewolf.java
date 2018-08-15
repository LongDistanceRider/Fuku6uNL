package fuku6uNL.role;

import fuku6uNL.board.BoardSurface;
import fuku6uNL.observer.DayStartObserver;
import fuku6uNL.util.Util;
import fuku6uNL.utterance.Utterance;
import org.aiwolf.common.data.Agent;
import org.aiwolf.common.data.Role;
import org.aiwolf.common.net.GameInfo;

public class Werewolf extends AbstractRole {

    // 占い師COするか
    private boolean isSeerCo = false;

    @Override
    public void dayStart() {
        BoardSurface boardSurface = BoardSurface.getInstance();
        GameInfo gameInfo = boardSurface.getGameInfo();

        // 占い師COするか決める（確率0.5）
        if (gameInfo.getDay() == 1) {
            isSeerCo = Util.cheatingCoin(0.5);
        }

        if (isSeerCo) {
            // 占い師CO
            boardSurface.setCoRole(Role.SEER);
            DayStartObserver.opposeCo(Role.SEER);
            Utterance.getInstance().offer("ボクが占い師！他はみんな偽物だよ！");
            // 占い結果作成
            lieDivined(boardSurface, gameInfo.getAliveAgentList());
        }

    }

    @Override
    public Agent vote() {
        BoardSurface boardSurface = BoardSurface.getInstance();
        GameInfo gameInfo = boardSurface.getGameInfo();
        Agent forceVoteTarget = boardSurface.getForceVoteTarget();
        if (forceVoteTarget != null) {
            return forceVoteTarget;
        }
        return null;
    }

    @Override
    public void talk(int turn) {

    }
}
