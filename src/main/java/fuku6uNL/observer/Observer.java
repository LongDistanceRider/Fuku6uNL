package fuku6uNL.observer;

import fuku6uNL.board.BoardSurface;
import fuku6uNL.utterance.Utterance;
import org.aiwolf.common.data.Agent;
import org.aiwolf.common.data.Role;
import java.util.List;
public class Observer {

    /**
     * 対抗COをチェックする
     * 他プレイヤが発言した時のチェックメソッド
     * @param myCoRole 自分がCOした役職
     * @param submit 発言者
     * @param role 発言された役職
     */
    public static void opposeCo(Role myCoRole, Agent submit, Role role) {
        switch (myCoRole) {
            case SEER:
                if (role.equals(Role.SEER)) {
                    Utterance.getInstance().offer(submit + "は偽物だよ！騙されないで！");
                }
        }
    }

    /**
     * 対抗COをチェックする
     * 自分がCOする時のチェックメソッド
     * @param boardSurface 盤面情報
     * @param coRole COする役職
     */
    public static void opposeCo(BoardSurface boardSurface, Role coRole) {
        List<Agent> opposeCoAgentList = boardSurface.getCoAgentList(coRole);
        opposeCoAgentList.forEach(agent -> {
            Utterance.getInstance().offer(agent + "に対抗します！私が" + Utterance.convertRoleToNl(coRole) + "です！");
        });
    }

    public static void checkSeerCo(BoardSurface boardSurface) {
        List<Agent> seerCoAgentList = boardSurface.getCoAgentList(Role.SEER);
        // 占い師COしている人数を更新
        if (boardSurface.getCoRole().equals(Role.SEER)) {
            boardSurface.setNumSeerCo(seerCoAgentList.size() + 1);
        } else {
            boardSurface.setNumSeerCo(seerCoAgentList.size());
        }

        // 2人の場合
        if (seerCoAgentList.size() == 2) {
            Utterance.getInstance().offer("占い師COしている人が2人いるね。どっちかが狂人かな？");
        }

        // 3人の場合
        if (seerCoAgentList.size() == 3) {
            Utterance.getInstance().offer("3人も占い師CO！？ボクの考えだと占い師の中に人狼がいるね。");
        }
    }
}
