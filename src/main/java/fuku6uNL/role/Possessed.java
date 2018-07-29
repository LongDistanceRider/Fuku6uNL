package fuku6uNL.role;

import fuku6uNL.board.BoardSurface;
import fuku6uNL.observer.Observer;
import fuku6uNL.util.Util;
import fuku6uNL.utterance.Utterance;
import org.aiwolf.common.data.Agent;
import org.aiwolf.common.data.Role;
import org.aiwolf.common.net.GameInfo;

import java.util.ArrayList;
import java.util.List;

public class Possessed extends AbstractRole {


    @Override
    public void dayStart(BoardSurface boardSurface) {
        forceVoteTarget = null;
        GameInfo gameInfo = boardSurface.getGameInfo();

        // 占い師CO
        boardSurface.setCoRole(Role.SEER);
        Observer.opposeCo(boardSurface, Role.SEER);
        Utterance.getInstance().offer("はい！ボク占い師だよ！");
        // 占い結果作成と発言（1つの要素が入ったマップor空のマップ
        lieDivined(boardSurface, gameInfo.getAliveAgentList());

        // PP発生確認（3人判定）
        if (gameInfo.getAliveAgentList().size() == 3) {
            boardSurface.setPP(true);
        }
    }

    @Override
    public Agent vote(BoardSurface boardSurface) {
        if (boardSurface.isPP()) {
            // 狂狼COしているAgentを取得
            List<Agent> roleCoAgentList = new ArrayList<>();
            roleCoAgentList.addAll(boardSurface.getCoAgentList(Role.POSSESSED));
            roleCoAgentList.addAll(boardSurface.getCoAgentList(Role.WEREWOLF));
            roleCoAgentList.retainAll(boardSurface.getGameInfo().getAliveAgentList());

            if (roleCoAgentList.size() == 1) {
                // 狂狼COしていないAgentを取得
                List<Agent> candidateAgentList = boardSurface.getGameInfo().getAliveAgentList();
                candidateAgentList.remove(boardSurface.getGameInfo().getAgent());
                candidateAgentList.removeAll(roleCoAgentList);
                if (!candidateAgentList.isEmpty()) {
                    return Util.randomElementSelect(candidateAgentList);
                }
            }
            // 狂狼COしているエージェントがいない，2人以上狂狼COがいる場合はPP失敗．
        }
        if (forceVoteTarget != null) {
            return forceVoteTarget;
        }
        return null;
    }

    @Override
    public void finish() {

    }
}
