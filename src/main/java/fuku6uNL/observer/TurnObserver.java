package fuku6uNL.observer;

import fuku6uNL.board.BoardSurface;
import fuku6uNL.utterance.Utterance;
import org.aiwolf.common.data.Agent;
import org.aiwolf.common.data.Role;

import java.util.List;

public class TurnObserver {

    public static void checkSeerCo() {
        BoardSurface boardSurface = BoardSurface.getInstance();
        List<Agent> seerCoAgentList = boardSurface.getCoAgentList(Role.SEER);
        // 占い師COしている人数を更新
        Role coRole = boardSurface.getCoRole();
        if (coRole != null && coRole.equals(Role.SEER)) {
            boardSurface.setNumSeerCo(seerCoAgentList.size() + 1);
        } else {
            boardSurface.setNumSeerCo(seerCoAgentList.size());
        }

        // 2人の場合
        int seerNum = boardSurface.getNumSeerCo();
        if (seerNum == 2 && coRole == null) {
            Utterance.getInstance().offer("占い師COしている人が2人いるね。1人は狂人かな？");
        }

        // 3人の場合
        if (seerNum == 3) {
            Utterance.getInstance().offer("3人も占い師CO！？ボクの考えだと占い師の中に人狼がいるね。");
        }
    }
}
