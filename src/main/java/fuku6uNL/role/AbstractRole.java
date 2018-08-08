package fuku6uNL.role;

import fuku6uNL.board.BoardSurface;
import fuku6uNL.util.Util;
import fuku6uNL.utterance.Utterance;
import org.aiwolf.common.data.Agent;
import org.aiwolf.common.data.Species;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public abstract class AbstractRole {

    public abstract void dayStart(BoardSurface boardSurface);
    public abstract Agent vote(BoardSurface boardSurface);
    public abstract void talk(int turn, BoardSurface boardSurface);

    /**
     * 偽占い結果を作成し発言する
     * @param boardSurface 盤面情報
     * @param candidateAgentList 偽占い候補にあるエージェント．
     *                           生存プレイヤと本当の占い結果を出したエージェントを考慮したリストを引数に渡す
     *
     *                           このリストから自分自身と発言した占い結果を除く処理が加わる
     * @return 1つの占い結果が帰って来る　または　からのマップ
     */
    protected void lieDivined (BoardSurface boardSurface, List<Agent> candidateAgentList) {
        candidateAgentList.remove(boardSurface.getGameInfo().getAgent());
        // 自分が発言した占い結果をもとに，すでに結果を出したエージェントを削除する
        List<Agent> divinedAgentList = new ArrayList<>();
        boardSurface.getDivinedMap().forEach(((agent, species) -> {
            divinedAgentList.add(agent);
        }));
        candidateAgentList.removeAll(divinedAgentList);

        if (!candidateAgentList.isEmpty()) {
            Agent lieTarget = Util.randomElementSelect(candidateAgentList);
            //0.8の確率で黒出し
            Species result = Species.HUMAN;
            if (Util.cheatingCoin(0.8)) {
                result = Species.WEREWOLF;
                boardSurface.setForceVoteTarget(lieTarget);
            }
            boardSurface.putDivinedMap(lieTarget, result);
            Utterance.getInstance().offer(lieTarget +"の占い結果は" + Utterance.convertSpeciesToNl(result) + "でした！");
        }
    }
}
