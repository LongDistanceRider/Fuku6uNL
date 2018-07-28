package fuku6uNL.observer;

import fuku6uNL.board.BoardSurface;
import fuku6uNL.utterance.Utterance;
import org.aiwolf.common.data.Agent;
import org.aiwolf.common.data.Role;
import org.aiwolf.common.data.Species;
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

    public static void estimateTargetMe(Agent me, Role myCoRole, Agent submit, Agent target, Role role) {
        if (target.equals(me)) {
            if (myCoRole != null) { // nullチェック
                if (myCoRole.equals(role)) {    // 自分がCOした役職とESTIMATEされた役職が同じ
                    Utterance.getInstance().offer(">>" + submit + " そうだよ！ボクが" + role + "なんだ！");
                }
                if (!myCoRole.equals(Role.WEREWOLF) && role.equals(Role.WEREWOLF)) { // 人狼呼ばわりされた
                    Utterance.getInstance().offer(">>" + submit + " ひどいよ！ボクは人間なのに。");
                }
                // 狂人呼ばわりされた
                if (!myCoRole.equals(Role.WEREWOLF)
                        && !myCoRole.equals(Role.POSSESSED)
                        && role.equals(Role.POSSESSED)) {
                    Utterance.getInstance().offer(">>" + submit + " ボクは狂人じゃないよ。真面目な村人の1人だよ。");
                }
            }
        }
    }

    public static void estimateTargetBlack(List<Agent> divinedBlackAgentList, Agent submit, Agent target, Role role) {
        if (divinedBlackAgentList.contains(target)) {
            switch (role) {
                case WEREWOLF:
                    Utterance.getInstance().offer(">>" + submit + " そうそう、" + target + " は人狼なのさ。");
                    break;
                case POSSESSED:
                    Utterance.getInstance().offer(">>" + submit + " いやいや、" + target + " は人狼だって。");
                    break;
                default:
                    Utterance.getInstance().offer(">>" + submit + " " + Utterance.convertRoleToNl(role) + "じゃなくて、人狼なんだって。");
            }
        }
    }

    public static void estimateTargetWhite(List<Agent> divinedAgentList, Agent submit, Agent target, Role role) {
        if (divinedAgentList.contains(target)) {
            switch (role) {
                case WEREWOLF:
                    Utterance.getInstance().offer(">>" + submit + " " + target + "は人間だよ？ボクを信じてないの？");
                    break;
                case POSSESSED:
                    Utterance.getInstance().offer(">>" + submit + " " + target + "を狂人だと思うの？んーどうだろう。");
                    break;
            }
        }
    }

    public static void divinedTargetMe(Agent me, Agent submit, Agent target, Species result) {
        if (target.equals(me)) {
            switch (result) {
                case HUMAN:
                    Utterance.getInstance().offer("ボクに白出しした" + submit + "は白よりだよね。");
                    break;
                case WEREWOLF:
                    Utterance.getInstance().offer("ボクは人間だよ！" + submit + "は偽物だったんだね。");
                    break;
            }
        }
    }

    public static void voteTargetMe(Agent me, Agent submit, Agent target) {
        if (target.equals(me)) {
            Utterance.getInstance().offer(">>" + submit + " ボクに投票するの！？もっと人狼っぽい人いるよ！");
        }
    }

    public static void voteTargetBlack(List<Agent> divinedAgentList, Agent submit, Agent target) {
        if (divinedAgentList.contains(target)) {
            Utterance.getInstance().offer(">>" + submit + " そうだよね。" + target + "は人狼だから投票しよう！");
        }
    }

    public static void voteTargetWhite(List<Agent> divinedAgentList, Agent submit, Agent target) {
        if (divinedAgentList.contains(target)) {
            Utterance.getInstance().offer(">>" + submit + " え？" + target + "は白だよ？");
        }
    }
}
