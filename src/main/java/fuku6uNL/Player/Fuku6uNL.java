package fuku6uNL.Player;

import fuku6uNL.board.BoardSurface;
import fuku6uNL.listen.Listen;
import fuku6uNL.listen.NLP;
import fuku6uNL.log.Log;
import fuku6uNL.log.LogWriter;
import fuku6uNL.observer.TurnObserver;
import fuku6uNL.role.*;
import fuku6uNL.util.Util;
import fuku6uNL.utterance.Utterance;
import org.aiwolf.common.data.*;
import org.aiwolf.common.net.GameInfo;
import org.aiwolf.common.net.GameSetting;

import java.util.List;
import java.util.Map;

public class Fuku6uNL implements Player {

    // Listen
    private Listen listen = new Listen();
    // 役職ごとの処理
    private AbstractRole assignRole;

    @Override
    public void initialize(GameInfo gameInfo, GameSetting gameSetting) {
        Log.startLog();
        Log.trace("initialize()");
        BoardSurface.getInstance().initialize(gameInfo);
    }

    @Override
    public void update(GameInfo gameInfo) {
        Log.trace("update()");
        BoardSurface.getInstance().update(gameInfo);
        listen.update();
    }

    @Override
    public void dayStart() {
        BoardSurface.getInstance().setForceVoteTarget(null);
        GameInfo gameInfo = BoardSurface.getInstance().getGameInfo();
        Log.trace("dayStart()");
        Log.info("=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=");
        Log.info("\t" + gameInfo.getDay() + "day start : My number is " + gameInfo.getAgent().toString());
        Log.info("=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=");

        switch (gameInfo.getDay()) {
            case 0:
                Utterance.getInstance().offer("こんにちは！これからよろしくね！");
                break;
            case 1:
                // 役職セット
                Role role = gameInfo.getRole();
                Log.info("自分の役職: " + role);
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
                // 役職ごとの処理
                assignRole.dayStart();
                break;
            default:
                // 発言リセット
                Utterance.getInstance().clear();
                // 被投票者
                Agent executedAgent = gameInfo.getExecutedAgent();
                Utterance.getInstance().offer(executedAgent + "が追放されたね。");
                Log.info("追放者: " + executedAgent);
                // 被害者
                Agent attackedAgent = null;
                for (Agent agent :
                        gameInfo.getLastDeadAgentList()) {  // 狐がいると2人返ってくると思われるため，この処理のままにしておく
                    if (!agent.equals(executedAgent)) {
                        attackedAgent = agent;
                    }
                }
                if (attackedAgent != null) {
                    Utterance.getInstance().offer(attackedAgent + "が襲われてる！");
                    Log.info("被害者 : " + attackedAgent);
                } else {
                    Log.info("被害者 : なし（GJ発生）");
                }
                // 役職ごとの処理
                assignRole.dayStart();
        }

    }

    @Override
    public String talk() {
        BoardSurface boardSurface = BoardSurface.getInstance();
        GameInfo gameInfo = boardSurface.getGameInfo();
        // 0日目処理
        if (gameInfo.getDay() == 0) {
            String string = Utterance.getInstance().poll();
            if (string != null) {
                return string;
            } else {
                return "Over";
            }
        }
        Log.trace("talk()");
        // ターン数取得
        int turn = 0;
        List<Talk> talkList = gameInfo.getTalkList();
        if (!talkList.isEmpty()) {
            turn = talkList.get(talkList.size()-1).getTurn();
        }
        // ターン数3の時に状況確認と雑談発言
        if (turn == 3 && gameInfo.getDay() == 1) {
            // 占い師の人数をチェックする
            TurnObserver.checkSeerCo();
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
            StringBuilder utteranceString = new StringBuilder();
            for (Agent seer :
                    seerAgent) {
                Map.Entry<Agent, Species> divinedMapEntry = boardSurface.getLatestDivinedMap(seer);
                if (divinedMapEntry != null) {
                    if (!utteranceString.toString().equals("")) {
                        utteranceString.append("を、");
                    }
                    String target = divinedMapEntry.getKey().toString();
                    if (target.equals(gameInfo.getAgent().toString())) {
                        target = "ボク";
                    }
                    utteranceString.append(seer).append("は").append(target).append("に").append(Utterance.convertSpeciesToWhiteBlack(divinedMapEntry.getValue()));
                    // 占い師2人の時に，対抗に黒出しするエージェントは怪しいよね　と言う．
                    Role targetCo = boardSurface.getPlayerCoRole(divinedMapEntry.getKey());
                    if (targetCo != null
                            && targetCo.equals(Role.SEER)
                            && divinedMapEntry.getValue().equals(Species.WEREWOLF)) {
                        if (boardSurface.getNumSeerCo() == 2) {
                            Utterance.getInstance().offer("対抗占い師に黒出し？2人占い師COなら対抗は狂人がセオリーだし、" + seer + "の判定は怪しいよ。");
                        } else if (boardSurface.getNumSeerCo() == 3) {
                            Utterance.getInstance().offer("3人占い師COで、対抗占い師に黒出しした" + seer + "は信用できると思うよ。");
                        }
                    }
                } else {
                    Utterance.getInstance().offer(seer +"って占い結果話したっけ？");
                }
            }
            if (!utteranceString.toString().equals("")) {
                utteranceString.append("を出したよね。");
                Utterance.getInstance().offer(utteranceString.toString());
            }
            utteranceString = new StringBuilder();
            // 投票者と投票先についてまとめる
            List<Agent> voterAgent = boardSurface.getGameInfo().getAliveAgentList();
            voterAgent.remove(boardSurface.getGameInfo().getAgent());
            for (Agent voter :
                    voterAgent) {
                Agent target = boardSurface.submitVoteAndTarget(voter);
                if (target != null) {
                    if (!utteranceString.toString().equals("")) {
                        utteranceString.append("で、");
                    }
                    utteranceString.append(voter).append("は").append(target).append("に投票");
                }
            }
            if (!utteranceString.toString().equals("")) {
                utteranceString.append("だね。");
                Utterance.getInstance().offer(utteranceString.toString());
            }
            // 最多被投票数について確認
            Agent maxVoteTarget = boardSurface.maxVotedAgent();
            if (maxVoteTarget != null) {
                Utterance.getInstance().offer("最多投票先は、" + maxVoteTarget + "だね。");
            }
            // 役職固有の処理
            assignRole.talk(turn);

            // 黒出されたAgentに投票発言をする？占い師で白出ししてる時，狂人で白出ししている時，人狼で白出ししてる時

            // 占い師１黒出し１は投票
            // 占い師２黒出し１は投票，２はどちらかに投票
            // 占い師３黒出し１は黒出した占い師を投票黒出し２はどちらかの占い師に投票，３は占い師に投票

        }
        String utterance = Utterance.getInstance().poll();
        if (utterance != null) {
            return utterance;
        } else {
            if (turn > 7) {
                return "Over";
            } else {
                return "Skip";
            }
        }
    }

    @Override
    public Agent vote() {
        Log.trace("vote()");
        return assignRole.vote();
    }

    @Override
    public Agent attack() {
        Log.trace("attack()");
        GameInfo gameInfo = BoardSurface.getInstance().getGameInfo();
        // 追放者以外のエージェントにアタック
        Agent executedAgent = gameInfo.getLatestExecutedAgent();
        List<Agent> candidateAgentList = gameInfo.getAliveAgentList();
        candidateAgentList.remove(gameInfo.getAgent());
        candidateAgentList.remove(executedAgent);

        Agent attakedAgent = Util.randomElementSelect(candidateAgentList);

        Log.info("襲撃先: " + attakedAgent);
        return attakedAgent;
    }

    @Override
    public Agent divine() {
        Log.trace("divine");
        BoardSurface boardSurface = BoardSurface.getInstance();
        GameInfo gameInfo = boardSurface.getGameInfo();
        List<Agent> candidateAgentList = gameInfo.getAliveAgentList();
        candidateAgentList.remove(gameInfo.getAgent());
        candidateAgentList.remove(gameInfo.getLatestExecutedAgent());
        candidateAgentList.removeAll(boardSurface.getDivinedMap().keySet());
        candidateAgentList.removeAll(boardSurface.getTrueDivinedMap().keySet());

        Agent divinedTarget = Util.randomElementSelect(candidateAgentList);

        Log.info("占い先: " + divinedTarget);
        return divinedTarget;
    }

    @Override
    public void finish() {
        Log.trace("finish()");
        // 勝敗を出力
        boolean isWerewolfSideWin = false;
        GameInfo gameInfo = BoardSurface.getInstance().getGameInfo();
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
        Utterance.getInstance().resetFlag();
        Utterance.getInstance().clear();
        Log.endLog();
        LogWriter.finish();
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
