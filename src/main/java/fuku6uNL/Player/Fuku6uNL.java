package fuku6uNL.Player;

import fuku6uNL.board.BoardSurface;
import fuku6uNL.listen.Listen;
import fuku6uNL.log.Log;
import fuku6uNL.role.*;
import fuku6uNL.util.Util;
import fuku6uNL.utterance.Utterance;
import org.aiwolf.common.data.*;
import org.aiwolf.common.net.GameInfo;
import org.aiwolf.common.net.GameSetting;

import java.util.List;

public class Fuku6uNL implements Player {

    // BoardSurface
    private BoardSurface boardSurface;
    // Listen
    private Listen listen = new Listen();
    // 役職ごとの処理
    private AbstractRole assignRole;

    @Override
    public void initialize(GameInfo gameInfo, GameSetting gameSetting) {
        Log.startLog();
        Log.trace("initialize()");
        this.boardSurface = new BoardSurface(gameInfo);
    }

    @Override
    public void update(GameInfo gameInfo) {
        Log.trace("update()");
        this.boardSurface.update(gameInfo);
        listen.update(boardSurface);
    }

    @Override
    public void dayStart() {
        Log.trace("dayStart()");
        Log.info("=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=");
        Log.info("\t" + boardSurface.getGameInfo().getDay() + "day start : My number is " + boardSurface.getGameInfo().getAgent().toString());
        Log.info("=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=");

        switch (boardSurface.getGameInfo().getDay()) {
            case 0:
                Utterance.getInstance().offer("こんにちは！これからよろしくね！");
                break;
            case 1:
                // 役職セット
                Role role = boardSurface.getGameInfo().getRole();
                switch (role) {
                    case VILLAGER:
                        assignRole = new Villager();
                        break;
                    case SEER:
                        assignRole = new Seer();
                        break;
                    case POSSESSED:
                        assignRole = new Possessed();
                        break;
                    case WEREWOLF:
                        assignRole = new Werewolf();
                        break;
                }
                break;
            default:
                // 役職ごとの処理
                assignRole.dayStart(boardSurface);
        }

    }

    @Override
    public String talk() {
        Log.trace("talk()");
        // ターン数取得
        List<Talk> talkList = boardSurface.getGameInfo().getTalkList();
        int turn = talkList.get(talkList.size()).getTurn();
        // ターン数3の時に状況確認と雑談発言
        if (turn == 3) {
            // 占い師COがn人の場合
            int seerNum = boardSurface.getNumSeerCo();
            Utterance.getInstance().offer("今回は" + seerNum +"-0進行だね。");
            switch (seerNum) {
                case 0:
                    Utterance.getInstance().offer("あれ？占い師さんCOしてなかったかな？");
                    break;
                case 1:
                    Utterance.getInstance().offer("対抗COなし？");
                    break;
                case 2:
                    Utterance.getInstance().offer("いつも通りの進行だね。");
                    break;
                case 3:
                    Utterance.getInstance().offer("占い師ローラー確定だね。");
                    break;
            }
        }
        // ターン数5の時に状況確認と雑談発言
        if (turn == 5) {
            // 占い結果について確認
            List<Agent> seerAgent = boardSurface.getCoAgentList(Role.SEER);
            seerAgent.forEach(agent -> {
                boardSurface.getDivinedMap(agent);

            });
            // 投票者と投票先についてまとめる
            // 最多被投票数について確認


        }
        return Utterance.getInstance().poll();
    }

    @Override
    public Agent vote() {
        Log.trace("vote()");
        return assignRole.vote(boardSurface);
    }

    @Override
    public Agent attack() {
        Log.trace("attack()");
        // 追放者以外のエージェントにアタック
        Agent executedAgent = boardSurface.getGameInfo().getLatestExecutedAgent();
        List<Agent> candidateAgentList = boardSurface.getGameInfo().getAliveAgentList();
        candidateAgentList.remove(boardSurface.getGameInfo().getAgent());
        candidateAgentList.remove(executedAgent);

        Agent attakedAgent = Util.randomElementSelect(candidateAgentList);

        Log.info("襲撃先: " + attakedAgent);
        return attakedAgent;
    }

    @Override
    public Agent divine() {
        Log.trace("divine");
        List<Agent> candidateAgentList = boardSurface.getGameInfo().getAliveAgentList();
        candidateAgentList.remove(boardSurface.getGameInfo().getAgent());
        candidateAgentList.remove(boardSurface.getGameInfo().getLatestExecutedAgent());
        candidateAgentList.removeAll(boardSurface.getDivinedAgentList(Species.HUMAN));
        candidateAgentList.removeAll(boardSurface.getDivinedAgentList(Species.WEREWOLF));
        candidateAgentList.removeAll(boardSurface.getTrueDivinedAgentList(Species.HUMAN));
        candidateAgentList.removeAll(boardSurface.getTrueDivinedAgentList(Species.WEREWOLF));

        Agent divinedTarget = Util.randomElementSelect(candidateAgentList);

        Log.info("占い先: " + divinedTarget);
        return divinedTarget;
    }

    @Override
    public void finish() {
        Log.trace("finish()");
        // 勝敗を出力
        boolean isWerewolfSideWin = false;
        GameInfo gameInfo = boardSurface.getGameInfo();
        for (Agent agent :
                gameInfo.getAliveAgentList()) {
            if (gameInfo.getRoleMap().get(agent).equals(Role.WEREWOLF)) {
                // 人狼勝利
                isWerewolfSideWin = true;
            }
        }
        if (isWerewolfSideWin) {
            Log.info("勝敗結果: 人狼陣営 勝利");
            if (gameInfo.getRole().equals(Role.WEREWOLF)) {
                Log.info("勝ち");
            } else {
                Log.info("負け");
            }
        } else {
            Log.info("勝敗結果: 村人陣営 勝利");
            if (gameInfo.getRole().equals(Role.WEREWOLF)) {
                Log.info("負け");
            } else {
                Log.info("勝ち");
            }
        }
        Log.endLog();
    }

    @Override
    public String getName() {
        return "Fuku6uNL";
    }

    @Override
    public Agent guard() {
        Log.trace("guard()");
        return null;
    }

    @Override
    public String whisper() {
        return null;
    }
}
