package fuku6uNL.listen;

import fuku6uNL.board.BoardSurface;
import fuku6uNL.log.Log;
import fuku6uNL.observer.TopicObserver;
import fuku6uNL.utterance.Utterance;
import org.aiwolf.client.lib.Content;
import org.aiwolf.common.data.Agent;
import org.aiwolf.common.data.Role;
import org.aiwolf.common.data.Talk;
import org.aiwolf.common.net.GameInfo;

import java.util.List;
import java.util.Objects;

/**
 * トークリストを読み取り処理を行うクラス
 *
 * 発言された内容に対して自然言語処理を施す
 * プロトコル言語で処理できる話題とNL言語（追加話題）で処理する話題に分け，
 * 各話題ごとに処理を行う
 */
public class Listen {

    // トークリストをどこまで読み込んだか
    private int talkListHead = 0;

    public void update() {
        GameInfo gameInfo = BoardSurface.getInstance().getGameInfo();

        List<Talk> talkList = gameInfo.getTalkList();
        for (int i = talkListHead; i < talkList.size(); i++) {
            Talk talk = talkList.get(i);
            int turn = talk.getTurn();
            Log.info("[" + turn + "]発言者: " + talk.getAgent() + " Mes(NL): " + talk.getText());

            // 0日目は処理をしない
            if (gameInfo.getDay() == 0) {
                continue;
            }
            // 自分の発言は処理をしない
            if (talk.getAgent().equals(gameInfo.getAgent())) {
                continue;
            }
            // Over Skip発言は処理をしない
            if (talk.getText().equals("Over") || talk.getText().equals("Skip")) {
                continue;
            }

            // 自然言語処理を行う
            NLP nlp = new NLP(gameInfo.getAgentList(), talk);

            // プロトコル文処理
            List<String> protocolTextList = nlp.getProtocolTextList();
            protocolTextList.forEach(text -> protocolTopic(talk.getAgent(), text));

            // 自然言語話題処理
            List<ContentNL> nlTextList = nlp.getNlpTextList();
            nlTextList.forEach((contentNl -> nlpTopic(talk.getAgent(), contentNl)));

        }
        talkListHead = talkList.size();
    }

    private void protocolTopic(Agent submit, String text) {
        Log.info("発言者: " + submit + " Mes(PT): " + text);
        Content content = null;
        try {
            content = new Content(text);
        } catch (Exception e) {
            Log.error("Content変換エラー発生 text: " + text + " e: " + e);
        }
        if (Objects.nonNull(content)) {
            switch (content.getTopic()) {
                    /* --- 意図表明に関する文 --- */
                case COMINGOUT:
                    BoardSurface.getInstance().setPlayerCoRole(submit, content.getRole());
                    // 対抗COのチェック
                    TopicObserver.oppseCo(submit, content.getRole());
                    break;
                case ESTIMATE:
                    // 対象をチェック
                    TopicObserver.checkEstimateTarget(submit, content.getTarget(), content.getRole());
                    break;
                    /* --- 能力結果に関する文 --- */
                case DIVINED:
                    BoardSurface.getInstance().setPlayerDivMap(submit, content.getTarget(), content.getResult()); // 占い結果を保管
                    // 対象をチェック
                    TopicObserver.checkDivinedTarget(submit, content.getTarget(), content.getResult());
                    // 占い師COしていない場合は，占い師COとして処理をする
                    Role coRole = BoardSurface.getInstance().getPlayerCoRole(submit);
                    if (coRole == null) {
                        Log.debug("占い師COしていないエージェントが占い結果を話したため，COMINGOUT SEERを生成しました．");
                        protocolTopic(submit, "COMINGOUT " + submit + " SEER");
                    }
                    break;
//                case IDENTIFIED:
//                    break;
//                case GUARDED:
//                    break;
//                  /* --- ルール行動・能力に関する文 --- */
//                case DIVINATION:
//                    break;
//                case GUARD:
//                    break;
                case VOTE:
                    BoardSurface.getInstance().setPlayerVote(submit, content.getTarget()); // 投票先発言を保管
                    // 対象をチェック
                    TopicObserver.checkVoteTarget(submit, content.getTarget());
                    break;
//                case ATTACK:
//                    break;
//                  /* --- 同意・非同意に関する文 --- */
//                case AGREE:
//                    break;
//                case DISAGREE:
//                    break;
//                  /* --- 発話制御に関する文 --- */
//                case OVER:
//                    break;
//                case SKIP:
//                    break;
//                  /* --- REQUEST文 --- */
//                case OPERATOR:
//                    break;
                default:
                    break;
            }
        }
    }

    private void nlpTopic(Agent submit, ContentNL contentNl) {
        Log.info("発言者: " + submit + " Mes(NT): " + contentNl);
        Agent me = BoardSurface.getInstance().getGameInfo().getAgent();
        switch (contentNl.getNlTopic()) {
            case "REQUEST_VOTE":
                // 対象をチェック
                TopicObserver.checkRequestVoteTarget(submit, contentNl.getTarget());
                break;
            case "LIAR":
                // 対象が自分かチェック
                TopicObserver.checkLiarTarget(submit, contentNl.getTarget());
                break;
            case "SUSPICIOUS":
                // 対象が自分かチェック
                TopicObserver.checkSuspiciousTarget(submit, contentNl.getTarget());
                break;
            case "TRUST":
                // 対象が自分かチェック
                TopicObserver.checkTrustTarget(submit, contentNl.getTarget());
                break;
            case "WHO_VOTE":
                // 対象が自分かチェック
                if (contentNl.getTarget().equals(me)) {
                    Agent forceVoteTarget = BoardSurface.getInstance().getForceVoteTarget();
                    if (forceVoteTarget != null) {
                        Utterance.getInstance().offer(">>" + submit + " 今のところ" + forceVoteTarget + "に投票するつもりだよ。");
                    } else {
                        Utterance.getInstance().offer(">>" + submit + " 今は特に決めてないな。");
                    }
                }
                break;
            case "WHO_ROLE":
                // 対象が自分かチェック
                if (contentNl.getTarget().equals(me)) {
                    Role myCo = BoardSurface.getInstance().getCoRole();
                    switch (contentNl.getRole()) {
                        case VILLAGER:
                            break;
                        case SEER:
                            // 占い師COしている場合は無視，していない場合は占い師COしている中で悩む
                            if (myCo != null && myCo.equals(Role.SEER)) {
                                List<Agent> seerAgentList = BoardSurface.getInstance().getCoAgentList(Role.SEER);
                                StringBuilder stringBuilder = new StringBuilder();
                                stringBuilder.append(">>" + submit + " ");
                                for (int i = 0; i < seerAgentList.size(); i++) {
                                    if (i > 0) {
                                        stringBuilder.append("か");
                                    }
                                    stringBuilder.append(seerAgentList.get(i));
                                }
                                stringBuilder.append("だろうね。");
                                Utterance.getInstance().offer(stringBuilder.toString());
                            }
                            break;
                        case POSSESSED:
                            break;
                        case WEREWOLF:
                            // 人狼COしている場合は無視，していない場合は占い師3人の場合は3人の中から，そうでない場合は非COが怪しいという
                            if (myCo != null && myCo.equals(Role.WEREWOLF)) {
                                List<Agent> seerAgentList = BoardSurface.getInstance().getCoAgentList(Role.SEER);
                                if (seerAgentList.size() < 3) {
                                    Utterance.getInstance().offer(">>" + submit + " 占い師COをしていないプレイヤの中にいると思うよ。");
                                } else {
                                    Utterance.getInstance().offer(">>" + submit + " 占い師COをしているプレイヤの中にいると思うよ。");
                                }
                            }
                            break;
                    }
                }
                break;
        }
    }
}
