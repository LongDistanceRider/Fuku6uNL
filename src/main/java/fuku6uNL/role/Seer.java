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
import java.util.Map;

public class Seer extends AbstractRole {

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
                // 占い結果（本当）が出ているものは除く
                candidateAgentList.removeAll(boardSurface.getTrueDivinedMap().keySet());

                // 偽占い結果作成と発言
                lieDivined(boardSurface, candidateAgentList);
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
    public Agent vote(BoardSurface boardSurface) {
        if (forceVoteTarget != null) {
            return forceVoteTarget;
        }
        return null;
    }

    @Override
    public void talk(int turn, BoardSurface boardSurface) {
        if (turn == 5) {
//            // 黒を出しているエージェントに対して発言
//            Map.Entry<Agent, Species> latestDivinedMap = boardSurface.getLatestDivinedMap();
//            if (latestDivinedMap != null && latestDivinedMap.getValue().equals(Species.WEREWOLF)) {
//                Utterance.getInstance().offer("人狼の" + latestDivinedMap.getKey() + "に投票すれば村は平和になるよ！みんな" + latestDivinedMap.getKey() + "に投票しよう！");
//            } else {
//                // 黒を出されているエージェントに対して発言
//                List<Agent> divinedBlackAgentList = boardSurface.getPlayerDivinedAgentList(Species.WEREWOLF);
//                divinedBlackAgentList.forEach(agent -> {
//                    Utterance.getInstance().offer(agent + "が黒かどうかは怪しいね。");
//                });
//
//            }
        }

    }

    @Override
    public void finish() {

    }
}
