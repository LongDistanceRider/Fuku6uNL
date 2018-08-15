package fuku6uNL.observer;

import fuku6uNL.board.BoardSurface;
import fuku6uNL.utterance.Utterance;
import org.aiwolf.client.lib.Topic;
import org.aiwolf.common.data.Agent;
import org.aiwolf.common.data.Role;
import org.aiwolf.common.data.Species;

public class TopicObserver {

    /**
     * 対抗をチェック
     * @param submit 発言者
     * @param role 発言役職
     */
    public static void oppseCo(Agent submit, Role role) {
        Role myCo = BoardSurface.getInstance().getCoRole();
        if (myCo != null && role.equals(myCo)) {
            switch (myCo) {
                case SEER:
                    Utterance.getInstance().offer(submit + "は偽物だよ！騙されないで！");
                    break;
                case POSSESSED:
                    if (BoardSurface.getInstance().isPP()) {
                        Utterance.getInstance().offer(submit + "は村人さ！ボクが狂人だよ。ご主人。");
                    }
                    break;
                case WEREWOLF:
                    if (BoardSurface.getInstance().isPP()) {
                        Utterance.getInstance().offer(submit + "は村人だよ。ボクがご主人。人狼なのさ。");
                    }
                    break;
            }
        }

    }

    public static void checkEstimateTarget(Agent submit, Agent target, Role role) {
        BoardSurface boardSurface = BoardSurface.getInstance();
        // 対象が自分か
        Agent me = boardSurface.getGameInfo().getAgent();
        Role myCo = boardSurface.getCoRole();
        if (target.equals(me)) {
            if (myCo != null) {
                switch (myCo) {
                    case VILLAGER:
                        if (role.equals(Role.VILLAGER)) {
                            Utterance.getInstance().offer(">>" + submit + " そうだよ。ボクはただの村人さ");
                        } else {
                            if (role.equals(Role.POSSESSED) || role.equals(Role.WEREWOLF)) {
                                Utterance.getInstance().offer(">>" + submit + " ひどいや。ボクはただの村人なのに。");
                            }
                        }
                        break;
                    case SEER:
                        if (role.equals(Role.SEER)) {
                            Utterance.getInstance().offer(">>" + submit + " そう。ボクがこの村の占い師なのさ。");
                        } else {
                            if (role.equals(Role.POSSESSED) || role.equals(Role.WEREWOLF)) {
                                Utterance.getInstance().offer(">>" + submit + " ボクが真占い師だよ！");
                            }
                        }
                        break;
                    case POSSESSED:
                        if (role.equals(Role.POSSESSED)) {
                            Utterance.getInstance().offer(">>" + submit + "そうだよ。ボクが狂人なのさ。");
                        }
                        break;
                    case WEREWOLF:
                        if (role.equals(Role.WEREWOLF)) {
                            Utterance.getInstance().offer(">>" + submit + "そうだよ。ボクが人狼なのさ。わおーん。");
                        }
                        break;
                }
            }
            if (myCo != null && myCo.equals(role)) {
                Utterance.getInstance().offer(">>" + submit + " そうだよ！ボクが" + role + "なんだ！");
            }

        }
        // 対象が白or黒出しエージェントか
        boardSurface.getDivinedMap().forEach(((divinedTarget, divinedResult) -> {
            if (target.equals(divinedTarget)) {
                if (divinedResult.equals(Species.HUMAN)) { // 占い結果は白
                    if (role.equals(Role.WEREWOLF)) {
                        Utterance.getInstance().offer(">>" + submit + " " + target + "は人間だよ？ボクを信じてないの？");
                    } else if (role.equals(Role.POSSESSED)) {
                        Utterance.getInstance().offer(">>" + submit + " " + target + "を狂人だと思うの？んーどうだろう。");
                    }
                } else {    // 占い結果は黒
                    if (role.equals(Role.WEREWOLF)) {
                        Utterance.getInstance().offer(">>" + submit + " そうそう、" + target + " は人狼なのさ。");
                    } else if (role.equals(Role.POSSESSED)) {
                        Utterance.getInstance().offer(">>" + submit + " いやいや、" + target + " は人狼だって。");
                    } else {
                        Utterance.getInstance().offer(">>" + submit + " " + Utterance.convertRoleToNl(role) + "じゃなくて、人狼なんだって。");
                    }
                }
            }
        }));
    }

    public static void checkDivinedTarget(Agent submit, Agent target, Species result) {
        BoardSurface boardSurface = BoardSurface.getInstance();
        // 対象が自分か
        Agent me = boardSurface.getGameInfo().getAgent();
        Role myCo = boardSurface.getCoRole();
        if (target.equals(me)) {
            if (result.equals(Species.HUMAN)) { // 白出し
                Utterance.getInstance().offer("ボクに白出しした" + submit + "は白よりだよね。");
            } else {    // 黒出し
                if (myCo != null && myCo.equals(Role.WEREWOLF)) {
                    Utterance.getInstance().offer(">>" + submit + " わおーん。");
                } else {
                    Utterance.getInstance().offer("ボクは人間だよ！" + submit + "は偽物だったんだね。");
                }
            }
        }
        // 対象が白or黒出しエージェントか
        boardSurface.getDivinedMap().forEach((divinedTarget, divinedResult) -> {
            if (divinedTarget.equals(target)) {
                if (divinedResult.equals(Species.HUMAN)) {  // 占い結果は白
                    if (result.equals(Species.HUMAN)) { // 発言は白
                        Utterance.getInstance().offer(submit + "も" + target + "に白出ししたから、" + target + "は白確定でいいんじゃないかな。");
                    } else { // 発言は黒
                        Utterance.getInstance().offer(">>" + submit + " " + target + "は人間だからね。みんな投票しないで！");
                    }
                } else {    // 占い結果は黒
                    if (result.equals(Species.HUMAN)) { // 発言は白
                        Utterance.getInstance().offer( ">>" + submit + " " + target + "は人狼だからね。みんな" + target + "に投票するんだ！");
                    } else {    // 発言は黒
                        Utterance.getInstance().offer(submit + "も" + target + "に黒出ししたから、" + target + "は黒確定でいいんじゃないかな。");
                    }
                }
            }
        });
    }

    public static void checkVoteTarget(Agent submit, Agent target) {
        BoardSurface boardSurface = BoardSurface.getInstance();
        // 対象が自分か
        Agent me = boardSurface.getGameInfo().getAgent();
        if (target.equals(me)) {
            Utterance.getInstance().offer(">>" + submit + " ボクに投票するの！？もっと人狼っぽい人いるよ！");
        }
        // 対象が白or黒出しエージェントか
        boardSurface.getDivinedMap().forEach((divinedTarget, divinedResult) -> {
            if (divinedTarget.equals(target)) {
                if (divinedResult.equals(Species.HUMAN)) {
                    Utterance.getInstance().offer(">>" + submit + " え？" + target + "は白だよ？");
                } else {
                    Utterance.getInstance().offer(">>" + submit + " そうだよね。" + target + "は人狼だから投票しよう！");
                }
            }
        });
    }

    public static void checkRequestVoteTarget(Agent submit, Agent target) {
        BoardSurface boardSurface = BoardSurface.getInstance();
        // 対象が自分か
        Agent me = boardSurface.getGameInfo().getAgent();
        if (target.equals(me)) {
            Utterance.getInstance().offer(">>" + submit + " 反対反対！" + submit + "の投票に反対です！");
        }
        // 対象が白or黒出しエージェントか
        boardSurface.getDivinedMap().forEach((divinedTarget, divinedResult) -> {
            if (divinedTarget.equals(target)) {
                if (divinedResult.equals(Species.HUMAN)) {
                    Utterance.getInstance().offer(">>" + submit + " 待って待って、" + target + "は白だって！");
                } else {
                    Utterance.getInstance().offer(">>" + submit + " 賛成！ボクも" + target + "に投票するよ！");
                }
            }
        });
    }

    public static void checkLiarTarget(Agent submit, Agent target) {
        // 対象が自分か
        if (target.equals(BoardSurface.getInstance().getGameInfo().getAgent())) {
            Utterance.getInstance().offer(">>" + submit + " ボクを嘘つき呼ばわりですか？そういうの良くないですよ。");
        }
    }


    public static void checkSuspiciousTarget(Agent submit, Agent target) {
        // 対象が自分か
        if (target.equals(BoardSurface.getInstance().getGameInfo().getAgent())) {
            Utterance.getInstance().offer(">>" + submit + " ボクを疑うの？ボクは人間だよ！");
        }
    }

    public static void checkTrustTarget(Agent submit, Agent target) {
        // 対象が自分か
        if (target.equals(BoardSurface.getInstance().getGameInfo().getAgent())) {
            Utterance.getInstance().offer(">>" + submit + " ボクも" + submit + "を信じてるよ！");
        }
    }
}
