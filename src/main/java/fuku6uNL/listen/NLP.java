package fuku6uNL.listen;

import fuku6uNL.log.Log;
import fuku6uNL.log.LogWriter;
import org.aiwolf.common.data.Agent;
import org.aiwolf.common.data.Role;
import org.aiwolf.common.data.Species;
import org.aiwolf.common.data.Talk;
import org.apache.lucene.search.spell.LevensteinDistance;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;

import static fuku6uNL.listen.NLText.isChat;

public class NLP {

    // PATH
    private static final String dir = System.getProperty("user.dir");
    // 照合する際に用いるレーベンシュタイン距離の閾値
    private static final double DISTANCE_THRESHOLD = 0.6;
    // 照合ファイル
    private static Map<String, String[]> comparisonMap = new HashMap<>();
    // プロトコル話題のリスト
    private List<String> protocolTextList = new ArrayList<>();
    // 自然言語話題のリスト
    private List<ContentNL> nlpTextList = new ArrayList<>();


    static {
        // 照合ファイルの読み込み
        try(BufferedReader bufferedReader = Files.newBufferedReader(Paths.get(dir +"/lib/comparison.csv"))) {
            List<String> comparisonList = new ArrayList<>();
            String readLine;
            while((readLine = bufferedReader.readLine()) != null) {
                comparisonList.add(readLine.trim());    // 制御文字と空白を削除してからリストへ追加
            }

            for (String line :
                    comparisonList) {
                String[] arrayLine = line.split(",");
                comparisonMap.put(arrayLine[0], Arrays.copyOfRange(arrayLine, 1, arrayLine.length));
            }
        } catch (IOException e) {
            Log.fatal("照合ファイルの読み込みでエラー" + e);
        }

    }

    // getter
    List<String> getProtocolTextList() {
        return protocolTextList;
    }

    List<ContentNL> getNlpTextList() {
        return nlpTextList;
    }

    /**
     * Constracter
     *
     * @param talk talk
     */
    NLP(List<Agent> agentList, Talk talk) {
        // 1文に分解後，フィルタリングを施し，NLTextインスタンスを生成する．
        String[] oneSentences = talk.getText().split("[!.。！？]");
        for (String oneSentence : oneSentences) {
            // フィルタリング
            if (oneSentence.equals("") || isChat(oneSentence)) {
                // 解析する必要のない発話を除外（中身のない発言，雑談）
                Log.trace("NL解析不要: " + oneSentence);
                continue;
            }
            // NLTextインスタンス生成
            NLText nlText = new NLText(oneSentence);
            // タグ変換後の文字列を取得
            List<String> tagStringList = nlText.getTagStringList();
            // ユークリッド距離を測る
            Map<String, String[]> validEntry = new HashMap<>();
            tagStringList.forEach(tagString -> {
                double maxDistance = 0; // 最大ユークリッド距離（一番近い距離が1，遠い距離が0のdouble型
                Map.Entry<String, String[]> maxComparisonEntry = null;
                for (Map.Entry<String, String[]> comparisonEntry :
                        comparisonMap.entrySet()) {
                    LevensteinDistance levensteinDistance = new LevensteinDistance();
                    double distance = levensteinDistance.getDistance(comparisonEntry.getKey(), tagString);
                    if (distance > maxDistance) {
                        maxDistance = distance;
                        maxComparisonEntry = comparisonEntry;
                    }
                }
                // 距離がDISTANCE_THRESHOLD以下は変換不可能とする
                if (maxDistance > DISTANCE_THRESHOLD) {
                    Log.trace("最大ユークリッド距離獲得照合ファイル文: " + maxComparisonEntry.getKey() + " 距離: " + maxDistance);
                    validEntry.put(maxComparisonEntry.getKey(), maxComparisonEntry.getValue());
                    LogWriter.addNlList(tagString + "," + maxComparisonEntry.getKey() + "," + maxComparisonEntry.getValue()[0] + "," + oneSentence + "," + maxDistance);
                } else {
                    if (maxComparisonEntry != null) {
                        Log.debug("ユークリッド距離不足．tagString: " + tagString + " 最大ユークリッド距離獲得照合ファイル文:" + maxComparisonEntry.getKey() + " 距離: " + maxDistance);
                        LogWriter.addNlList(tagString + "," + maxComparisonEntry.getKey() + "," + maxComparisonEntry.getValue()[0] + "," + oneSentence + "," + maxDistance);
                    } else {
                        Log.error("最大ユークリッド距離獲得ができませんでした．");
                    }
                }
            });

            // 有効なユークリッド距離を取得できたエントリーのみ処理を行う
            validEntry.forEach((key, value) -> {
                for (int i = 0; i < value.length; i += 3) {
                    String target;
                    Agent targetAgent;
                    Role role;
                    Species species;
                    switch (value[i]) {
                        case "COMINGOUT":
                            // <ROLE>照合
                            role = getRole(nlText, Integer.parseInt(value[i + 1]));
                            if (role != null) {
                                protocolTextList.add("COMINGOUT " + talk.getAgent() + " " + role);   // プロトコル文変換
                            } else {
                                Log.error("Role型がnullのため変換に失敗しました．talk.getText(): " + talk.getText());
                                break;
                            }
                            break;
                        case "DIVINED":
                            // <TARGET>照合
                            target = getTargetString(nlText, Integer.parseInt(value[i + 1]));
                            if (target == null) {
                                Log.error("DIVINED変換中に予期しないエラー（null）が発生しました．talk.getText(): " + talk.getText());
                                break;
                            }
                            // <ROLE>からSPECIES照合
                            species = getSpecies(nlText, Integer.parseInt(value[i + 2]));
                            if (species != null) {
                                protocolTextList.add("DIVINED " + target + " " + species);
                            }
                            break;
                        case "ESTIMATE":
                            // <TARGET>照合
                            target = getTargetString(nlText, Integer.parseInt(value[i + 1]));
                            if (target == null) {
                                Log.error("DIVINED変換中に予期しないエラー（null）が発生しました．");
                                break;
                            }
                            // <ROLE>照合
                            role = getRole(nlText, Integer.parseInt((value[i + 2])));
                            if (role != null) {
                                protocolTextList.add("ESTIMATE " + target + " " + role);
                            } else {
                                Log.error("DIVINED変換中に予期しないエラー（null）が発生しました．");
                                break;
                            }
                            break;
                        case "VOTE":
                            // <TARGET>照合
                            target = getTargetString(nlText, Integer.parseInt(value[i + 1]));
                            if (target != null) {
                                protocolTextList.add("VOTE " + target);
                            } else {
                                Log.error("VOTE変換中に予期しないエラー（null）が発生しました．");
                                break;
                            }
                            break;
                        case "REQUEST_VOTE":
                            Log.debug("NlTopic: " + value[i]);
                            // <TARGET>照合
                            target = getTargetString(nlText, Integer.parseInt(value[i + 1]));
                            targetAgent = convertStrToAgent(agentList, target);
                            if (targetAgent != null) {
                                nlpTextList.add(new ContentNL("REQUEST_VOTE", targetAgent));
                                protocolTextList.add("VOTE " + target);
//                            // 自分に投票発言をしているか
//                            Agent ageTarget = convertStringToAgent(target);
//                            if (boardSurface.getMe().toString().equals(target)) {
//                                Utterance.getInstance().offerNL(">>" + talk.getAgent() + " " + "ちょっと待ってよ。自分は人間だって。そんなことを言う" + talk.getAgent() + "の印象はすごく悪いよ！");
//                            } else if (ageTarget != null && boardSurface.getAssignRole().getBlackAgentList().contains(ageTarget)) {
//                                // 黒出ししたエージェントに投票しているか
//                                Utterance.getInstance().offerNL(">>" + talk.getAgent() + " " + "賛成！" + target + "に投票しよう。");
//                            } else {
//                                Utterance.getInstance().offerNL(">>" + talk.getAgent() + " " + target + "に投票？んーまだ分からないから保留したいよ。");
//                            }
//                            protocolTextList.add("REQUEST VOTE " + target);   // REQUESTの書き方がわからないので，コメントアウトしておく
                            } else {
                                Log.error("REQUEST_VOTE変換中に予期しないエラー（null）が発生しました．");
                                break;
                            }
                            break;
                        case "LIAR":
                            Log.debug("NlTopic: " + value[i]);
                            // <TARGET>照合
                            target = getTargetString(nlText, Integer.parseInt(value[i + 1]));
                            targetAgent = convertStrToAgent(agentList, target);
                            if (targetAgent != null) {
                                nlpTextList.add(new ContentNL("LIAR", targetAgent));
//                            // 自分に嘘つき発言をしているか
//                            if (boardSurface.getMe().toString().equals(target)) {
//                                Utterance.getInstance().offerNL(">>" + talk.getAgent() + " " + "そう言う君も本当のことを言ってるか怪しい。");
//                            }
                            } else {
                                Log.error("LIAR変換中に予期しないエラー（null）が発生しました．");
                                break;
                            }
                            break;
                        case "SUSPICIOUS":
                            Log.debug("NlTopic: " + value[i]);
                            // <TARGET>照合
                            target = getTargetString(nlText, Integer.parseInt(value[i + 1]));
                            targetAgent = convertStrToAgent(agentList, target);
                            if (targetAgent != null) {
                                nlpTextList.add(new ContentNL("SUSPICIOUS", targetAgent));
//                            // 自分に疑い発言をしているか
//                            if (boardSurface.getMe().toString().equals(target)) {
//                                Utterance.getInstance().offerNL(">>" + talk.getAgent() + " " + "ボクは" + talk.getAgent() + "のこと信じてるよ。半分くらいね。");
//                            }
                            } else {
                                Log.error("SUSPICIOUS変換中に予期しないエラー（null）が発生しました．");
                                break;
                            }
                            break;
                        case "TRUST":
                            Log.debug("NlTopic: " + value[i]);
                            // <TARGET>照合
                            target = getTargetString(nlText, Integer.parseInt(value[i + 1]));
                            targetAgent = convertStrToAgent(agentList, target);
                            if (targetAgent != null) {
                                nlpTextList.add(new ContentNL("TRUST", targetAgent));
//                            // 自分に疑い発言をしているか
//                            if (boardSurface.getMe().toString().equals(target)) {
//                                Utterance.getInstance().offerNL(">>" + talk.getAgent() + " " + "ボクも" + talk.getAgent() + "のこと信じてるよ！");
//                            }
                            } else {
                                Log.error("TRUST変換中に予期しないエラー（null）が発生しました．");
                                break;
                            }
                            break;
                        case "WHO_VOTE":
                            Log.debug("NlTopic: " + value[i]);
                            // <TARGET>照合
                            target = getTargetString(nlText, Integer.parseInt(value[i + 1]));
                            targetAgent = convertStrToAgent(agentList, target);
                            if (targetAgent != null) {
                                nlpTextList.add(new ContentNL("WHO_VOTE", targetAgent));
                            } else {
                                Log.error("VOTE変換中に予期しないエラー（null）が発生しました．");
                                break;
                            }
                            break;
                        case "WHO_ROLE":
                            Log.debug("NlTopic: " + value[i]);
                            // <TARGET>照合
                            target = getTargetString(nlText, Integer.parseInt(value[i + 1]));
                            targetAgent = convertStrToAgent(agentList, target);
                            if (targetAgent == null) {
                                Log.error("DIVINED変換中に予期しないエラー（null）が発生しました．");
                                break;
                            }
                            // <ROLE>照合
                            role = getRole(nlText, Integer.parseInt((value[i + 2])));
                            if (role != null) {
                                nlpTextList.add(new ContentNL("WHO_ROLE", targetAgent, role));
                            } else {
                                Log.error("DIVINED変換中に予期しないエラー（null）が発生しました．");
                                break;
                            }
                            break;
                        case "IMPOSSIBLE":  // 現在の手法では取り扱うことができない話題
                            Log.debug("NlTopic: " + value[i]);
                            break;
                        case "NO_REQUIRED": // 処理不要な話題
                            Log.debug("NlTopic: " + value[i]);
                            break;
                        default:
                            Log.error("想定していないSwitch-defaultに分岐しました．talk.getText(): " + talk.getText() + " value[i]" + value[i]);
                            break;
                    }
                }
            });
        }

    }

    /**
     * Speciesを取得
     * @param text NLText
     * @param number
     *          何番目に出現したROLEタグを取得するか
     * @return Species
     */
    private Species getSpecies (NLText text, int number) {
        Species species = null;
        String speciesString = getRoleOrSpecies(text, number, 2);

        if (speciesString != null) {
            try {
                species = Species.valueOf(speciesString);
            } catch (IllegalArgumentException e) {
                Log.error("存在しないSpecies型に変換しようとして失敗しました．");
            }
        } else {
            Log.error("変換文からRoleに変換できる単語が発見されませんでした．");
        }
        return species;
    }
    /**
     * Roleを取得
     * @param text NLText
     * @param number
     *          照合ファイルの4列目を指定する場合，1列目はKey担っているため，Valueが2列目から始まる．２を指定すると4列目の数字を取ってくる
     *          大抵は2（というか2しかないはず）
     * @return Role
     */
    private Role getRole (NLText text, int number) {
        Role role = null;
        String speciesString = getRoleOrSpecies(text, number, 1);

        if (speciesString != null) {
            try {
                // Role型に変換
                role = Role.valueOf(speciesString);
            } catch (IllegalArgumentException e) {
                Log.error("存在しないRole型に変換しようとして失敗しました．");
            }
        } else {
            Log.error("変換文からRoleに変換できる単語が発見されませんでした．");
        }
        return role;
    }

    private String getRoleOrSpecies (NLText text, int number, int cast) {
        String string = null;
        List<String> tagStringListCo = text.getTagStringList("<ROLE>",cast);
        if (!tagStringListCo.isEmpty()) {
            try {
                string = tagStringListCo.get(number);   // tagString = SEER | WEREWOLF |　などなど．．．
            } catch(IndexOutOfBoundsException e) {
                Log.error("存在しないインデックスを取得しようとして失敗しました．");
            }
        } else {
            Log.trace("COMINGOUT変換中に<ROLE>タグが見つからず，解析終了");
        }
        return string;

    }

    private String getTargetString (NLText text, int number) {
        String targetString = null;
        List<String> targetList = text.getTargetList();
        if (!targetList.isEmpty()) {
            try {
                targetString = targetList.get(number);
            } catch(IndexOutOfBoundsException e) {
                Log.error("存在しないインデックスを取得しようとして失敗しました．");
            }
        } else {
            Log.trace("DIVINED変換中に<TARGET>タグが見つからず，解析終了．");
        }
        return targetString;
    }

    public static Agent convertStrToAgent(List<Agent> agentList, String string) {
        for (Agent agent :
                agentList) {
            if (agent.toString().equals(string)) {
                return agent;
            }
        }
        Log.error("stringからAgentへの変換に失敗しました．string: " + string);
        return null;
    }
}
