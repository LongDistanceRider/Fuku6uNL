package fuku6uNL.role;

import fuku6uNL.board.BoardSurface;
import fuku6uNL.utterance.Utterance;
import org.aiwolf.common.data.Agent;
import org.aiwolf.common.data.Role;
import org.aiwolf.common.data.Species;

import java.util.List;

public class Villager extends AbstractRole {
    @Override
    public void dayStart(BoardSurface boardSurface) {
        int day = boardSurface.getGameInfo().getDay();
        switch (day) {
            case 1:
                Utterance.getInstance().offer("人狼怖いよー");
                break;
            case 2:
                Utterance.getInstance().offer("まだ人狼いるの！？");
                break;
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
        switch (turn) {
            case 4:
                List<Agent> seerCoAgentList = boardSurface.getCoAgentList(Role.SEER);
                // 占い師が2人の時
                if (seerCoAgentList.size() == 2) {
                    // 自分に黒出ししたエージェントがいるかチェックする
                    List<Agent> liarAgent = boardSurface.divinedMeBlackAgentList(boardSurface.getGameInfo().getAgent(), Species.WEREWOLF);
                    if (!liarAgent.isEmpty()) {
                        seerCoAgentList.removeAll(liarAgent);
                        if (seerCoAgentList.size() == 1) {
                            // 真占い師確定
                            Agent trustSeer = seerCoAgentList.get(0);
                            Utterance.getInstance().offer("ボクに黒出しした" + liarAgent.get(0) + "は信用できない！だから、対抗の" + trustSeer + "を信じることにするよ。");
                            Agent votedAgent = boardSurface.submitVoteAndTarget(trustSeer);
                            if (votedAgent != null) {
                                // 真占い師と同じ投票先に決定する
                                Utterance.getInstance().offer(trustSeer + "と同じく" + votedAgent + "に投票するよ");
                                forceVoteTarget = votedAgent;
                            }
                        }
                    }
                } else if (seerCoAgentList.size() == 3) {
                    Utterance.getInstance().offer("占い師の中から1人投票するね。");
                    Agent maxVotedAgent = boardSurface.maxVotedAgent();
                    if (boardSurface.getCoAgentList(Role.SEER).contains(maxVotedAgent)) {
                        Utterance.getInstance().offer("占い師の中で一番投票されそうな" + maxVotedAgent + "に決定！");
                        forceVoteTarget = maxVotedAgent;
                    }
                }
                break;
            case 6:
                break;
        }
    }
}
