package fuku6uNL.role;

import fuku6uNL.board.BoardSurface;
import fuku6uNL.utterance.Utterance;
import org.aiwolf.common.data.Agent;
import org.aiwolf.common.data.Role;

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
                // 占い師が2人の時
                int NumSeerCo = boardSurface.getNumSeerCo();
                if (NumSeerCo == 2) {
                    // 真占い師確定しているかチェックする
                    Agent trueSeerAgent = boardSurface.trueSeerAgent();
                    if (trueSeerAgent != null) {
                        Utterance.getInstance().offer(trueSeerAgent + "は信用できるね。");
                        Agent votedAgent = boardSurface.submitVoteAndTarget(trueSeerAgent);
                        if (votedAgent != null) {
                            Utterance.getInstance().offer(trueSeerAgent + "と同じく" + votedAgent + "に投票するよ");
                            forceVoteTarget = votedAgent;
                        }
                    }
                }
                // 占い師が3人の時
                if (NumSeerCo == 3) {
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

    @Override
    public void finish() {

    }
}
