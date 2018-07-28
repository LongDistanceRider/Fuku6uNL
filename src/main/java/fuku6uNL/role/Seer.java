package fuku6uNL.role;

import fuku6uNL.board.BoardSurface;
import fuku6uNL.observer.Observer;
import fuku6uNL.util.Util;
import fuku6uNL.utterance.Utterance;
import org.aiwolf.common.data.Agent;
import org.aiwolf.common.data.Judge;
import org.aiwolf.common.data.Role;
import org.aiwolf.common.data.Species;
import org.aiwolf.common.net.GameInfo;
import java.util.List;

public class Seer extends AbstractRole {

    // 強制投票先
    private Agent forceVoteTarget;

    @Override
    public void dayStart(BoardSurface boardSurface) {
        forceVoteTarget = null;
        GameInfo gameInfo = boardSurface.getGameInfo();
        // 占い師CO
        boardSurface.setCoRole(Role.SEER);
        Observer.opposeCo(boardSurface, Role.SEER);
        Utterance.getInstance().offer("ボクは占い師！");
        // 占い結果取得
        Judge divination = gameInfo.getDivineResult();
        if (divination != null) {
            Agent target = divination.getTarget();
            Species result = divination.getResult();
            boardSurface.putTrueDivinedMap(target, result); // 本当の占い結果を保管

            // 占い結果が白の場合は別の人に黒出し(0.8の確率で），黒の場合はそのまま発言
            if (result.equals(Species.HUMAN) && Util.cheatingCoin(0.8)) {
                // 占い先を探す
                List<Agent> candidateAgentList = gameInfo.getAliveAgentList();
                candidateAgentList.remove(gameInfo.getAgent()); // 自分は除く
                // 占い結果（発言と本当）が出ているものは除く
                candidateAgentList.removeAll(boardSurface.getDivinedAgentList(Species.HUMAN));
                candidateAgentList.removeAll(boardSurface.getDivinedAgentList(Species.WEREWOLF));
                candidateAgentList.removeAll(boardSurface.getTrueDivinedAgentList(Species.HUMAN));
                candidateAgentList.removeAll(boardSurface.getTrueDivinedAgentList(Species.WEREWOLF));

                if (!candidateAgentList.isEmpty()) {
                    Agent lieTarget = Util.randomElementSelect(candidateAgentList);
                    boardSurface.putDivinedMap(lieTarget, Species.WEREWOLF);
                    Utterance.getInstance().offer("占い結果は、" + lieTarget + "が人狼！やったね！");
                    forceVoteTarget = lieTarget;
                }
            } else {
                boardSurface.putDivinedMap(target, result);
                boardSurface.putTrueDivinedMap(target, result);
                Utterance.getInstance().offer(target +"の占い結果は" + Utterance.convertSpeciesToNl(result) + "でした！");
                if (result.equals(Species.WEREWOLF)) {
                    forceVoteTarget = target;
                }
            }
        }
    }

    @Override
    public Agent vote() {
        if (forceVoteTarget != null) {
            return forceVoteTarget;
        }
        return null;
    }

    @Override
    public void talk() {

    }

    @Override
    public void finish() {

    }
}
