package fuku6uNL.listen;

import fuku6uNL.board.BoardSurface;
import fuku6uNL.log.Log;
import fuku6uNL.observer.Observer;
import org.aiwolf.client.lib.Content;
import org.aiwolf.common.data.Agent;
import org.aiwolf.common.data.Species;
import org.aiwolf.common.data.Talk;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
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
    // BoardSurface
    private BoardSurface boardSurface;

    public void update(BoardSurface boardSurface) {
        this.boardSurface = boardSurface;

        List<Talk> talkList = boardSurface.getGameInfo().getTalkList();
        for (int i = talkListHead; i < talkList.size(); i++) {
            Talk talk = talkList.get(i);
            Log.info("発言者: " + talk.getAgent() + " Mes(NL): " + talk.getText());

            // 自分の発言は処理をしない
            if (talk.getAgent().equals(boardSurface.getGameInfo().getAgent())) {
                continue;
            }
            // Over Skip発言は処理をしない
            if (talk.getText().equals("Over") || talk.getText().equals("Skip")) {
                continue;
            }

            // 自然言語処理を行う
            NLP nlp = new NLP(boardSurface.getGameInfo().getAgentList(), talk);

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
                    boardSurface.setPlayerCoRole(submit, content.getRole()); // CO役職を保管
                    // 対抗COのチェック
                    Observer.opposeCo(boardSurface.getCoRole(), submit, content.getRole());
                    // 占い師の人数をチェック
                    Observer.checkSeerCo(boardSurface);
                    break;
                case ESTIMATE:
                    // 対象が自分かチェック
                    Observer.estimateTargetMe(boardSurface.getGameInfo().getAgent(), boardSurface.getCoRole(), submit, content.getTarget(), content.getRole());
                    // 対象が黒出しエージェントかチェック
                    Observer.estimateTargetBlack(divinedSpeciesTargetList(boardSurface.getDivinedMap(), Species.WEREWOLF), submit, content.getTarget(), content.getRole());
                    // 対象が白だしエージェントかチェック
                    Observer.estimateTargetWhite(divinedSpeciesTargetList(boardSurface.getDivinedMap(), Species.HUMAN), submit, content.getTarget(), content.getRole());
                    break;
                    /* --- 能力結果に関する文 --- */
                case DIVINED:
                    boardSurface.setPlayerDivMap(submit, content.getTarget(), content.getResult()); // 占い結果を保管
                    // 対象が自分かチェック
                    Observer.divinedTargetMe(boardSurface.getGameInfo().getAgent(), submit, content.getTarget(), content.getResult());
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
                    boardSurface.setPlayerVote(submit, content.getTarget()); // 投票先発言を保管
                    // 対象が自分かチェック
                    Observer.voteTargetMe(boardSurface.getGameInfo().getAgent(), submit, content.getTarget());
                    // 対象が黒出ししたエージェントかチェック
                    Observer.voteTargetBlack(divinedSpeciesTargetList(boardSurface.getDivinedMap(), Species.WEREWOLF), submit, content.getTarget());
                    // 対象が白だししたエージェントがチェック
                    Observer.voteTargetWhite(divinedSpeciesTargetList(boardSurface.getDivinedMap(), Species.HUMAN), submit, content.getTarget());
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
        switch (contentNl.getNlTopic()) {
            case "REQUEST_VOTE":
                // 対象が自分かチェック
                Observer.requestVoteTargetMe(boardSurface.getGameInfo().getAgent(), submit, contentNl.getTarget());
                // 対象が黒出ししたエージェントかチェック
                Observer.requestVoteTargetBlack(divinedSpeciesTargetList(boardSurface.getDivinedMap(), Species.WEREWOLF), submit, contentNl.getTarget());
                // 対象が白だししたエージェントがチェック
                Observer.requestVoteTargetWhite(divinedSpeciesTargetList(boardSurface.getDivinedMap(), Species.HUMAN), submit, contentNl.getTarget());
                break;
            case "LIAR":
                // 対象が自分かチェック
                Observer.liarTargetMe(boardSurface.getGameInfo().getAgent(), submit, contentNl.getTarget());
                break;
            case "SUSPICIOUS":
                // 対象が自分かチェック
                Observer.suspiciousTargetMe(boardSurface.getGameInfo().getAgent(), submit, contentNl.getTarget());
                break;
            case "TRUST":
                // 対象が自分かチェック
                Observer.trustTargetMe(boardSurface.getGameInfo().getAgent(), submit, contentNl.getTarget());
                break;
            case "WHO_VOTE":
                break;
            case "WHO_ROLE":
                break;
        }
    }

    /**
     * speciesで指定された占い判定を受けたエージェントのリストを返す（自分の（発言した）占い結果のみ）
     * @param species HUMAN または WEREWOLF
     * @return 占い判定を受けたエージェントのリスト
     */
    private List<Agent> divinedSpeciesTargetList(Map<Agent, Species> divinedMap, Species species) {
        List<Agent> divinedAgentList = new ArrayList<>();
        divinedMap.forEach((agent, result) -> {
            if (result.equals(species)) {
                divinedAgentList.add(agent);
            }
        });
        return divinedAgentList;
    }

}
