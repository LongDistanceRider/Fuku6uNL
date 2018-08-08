package fuku6uNL.utterance;

import fuku6uNL.log.Log;
import org.aiwolf.common.data.Role;
import org.aiwolf.common.data.Species;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.List;
/**
 * 発言の一括管理クラス
 * すでに発言をしたかを管理し，何度も発言を繰り返すことを避ける
 * initialize-on-demand holder
 */
public class Utterance {

    /* すでに発言したかを管理するリスト */
    private List<String> flagList = new ArrayList<>();
    /* 発言キュー */
    private ArrayDeque<String> utteranceQueue = new ArrayDeque<>();

    public void clear() {
        utteranceQueue.clear();
    }
    /**
     * キューに発言を追加
     * @param utterance
     *  追加したい発言
     */
    public void offer(String utterance) {
        // 空文字は処理しない
        if (utterance.equals("")) {
            return;
        }
        // 同じ発言を何度もしないように，フラグ管理を施す
        StackTraceElement[] stackTraceElements = (new Throwable()).getStackTrace(); // スタックトレースより呼び出し元情報の取り出し
        String className = stackTraceElements[1].getClassName();    // クラス名取得
        String methodName = stackTraceElements[1].getMethodName();  // メソッド名取得
        int line = stackTraceElements[1].getLineNumber();   // 呼び出し元行数取得
        String flagString = className + methodName + line + utterance;  // フラグ名作成
        Log.trace("flagString" + flagString);

        // フラグチェック
        if (!flagList.contains(flagString)) {
            utteranceQueue.offer(utterance);
            flagList.add(flagString);
        }
    }

    /**
     * キューから発言を取り出し
     * @return キューが空の場合はnull
     */
    public String poll() {
        // キューにある発言をアンカーがあるところまで繋げて返す（アンカー発言が冒頭に来るようにするため）
        // また，アンカー発言は必ず1回分使用する．
        String utterance = utteranceQueue.poll();
        if (utterance == null) {
            return null;
        }
        // キューの先頭がアンカー発言の場合は，そのまま返す
        if (utterance.startsWith(">>Agent[")) {
            return utterance;
        }
        // キューの発言がアンカー発言以外の場合は，アンカー発言，またはnullが入るまでStringを繋げる
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append(utterance);
        while (true) {
            utterance = utteranceQueue.poll();
            if (utterance == null) {
                return stringBuilder.toString();
            }
            if (utterance.startsWith(">>Agent[")) {
                utteranceQueue.offerFirst(utterance);   // キューに戻す
                return stringBuilder.toString();
            }
            stringBuilder.append(utterance);
        }
    }

    /* *** initialize-on-demand holder *** */
    public static Utterance getInstance() {
        return UtteranceHolder.INSTANCE;
    }
    private Utterance(){}
    private static class UtteranceHolder {
        static final Utterance INSTANCE = new Utterance();
    }
    /* *** *** */

    /**
     * Speciesを自然言語に変換
     * @param species　変換するSpecies型
     * @return 「人間」または「人狼」のStringを返却
     */
    public static String convertSpeciesToNl (Species species) {
        if (species.equals(Species.HUMAN)) {
            return "人間";
        }
        return "人狼";
    }

    /**
     * Speciesを自然言語（白黒）に変換
     * @param species 変換するSpecies型
     * @return 「白」または「黒」のStringを返却
     */
    public static String convertSpeciesToWhiteBlack (Species species) {
        if (species.equals(Species.HUMAN)) {
            return "白";
        }
        return "黒";
    }

    /**
     * Roleを自然言語に変換
     */
    public static String convertRoleToNl (Role role) {
        switch (role) {
            case SEER:
                return "占い師";
            case MEDIUM:
                return "霊能者";
            case BODYGUARD:
                return "狩人";
            case POSSESSED:
                return "裏切り者";
            case WEREWOLF:
                return "人狼";
            default:
                return "村人";
        }
    }
}

