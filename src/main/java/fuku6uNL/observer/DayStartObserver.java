package fuku6uNL.observer;

import fuku6uNL.board.BoardSurface;
import fuku6uNL.utterance.Utterance;
import org.aiwolf.common.data.Agent;
import org.aiwolf.common.data.Role;

import java.util.List;

public class DayStartObserver {
    /**
     * 対抗COをチェックする
     * 自分がCOする時のチェックメソッド
     * @param coRole COする役職
     */
    public static void opposeCo(Role coRole) {
        List<Agent> opposeCoAgentList = BoardSurface.getInstance().getCoAgentList(coRole);
        opposeCoAgentList.forEach(agent -> Utterance.getInstance().offer(agent + "に対抗します！"));
    }
}
