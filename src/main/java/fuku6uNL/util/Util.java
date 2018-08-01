package fuku6uNL.util;

import fuku6uNL.log.Log;

import java.util.List;

public class Util {
    /**
     * リストから要素を一つランダムに返す
     *
     * @param list
     * @param <T>
     * @return
     */
    public static <T> T randomElementSelect(List<T> list) {
        if (list.isEmpty()) return null;
        else return list.get((int) (Math.random() * list.size()));
    }

    /**
     * trueになる確率を指定してランダムにbooleanを返す
     * @param probability trueになる確率（0.0 ~ 1.0)0.5で普通のコイントス
     * @return trueまたはfalseを返す
     */
    public static boolean cheatingCoin(double probability) {
        if (0 > probability || probability > 1) {
            Log.warn("不正な引数が渡されたため，probabilityを0.5に変換しました");
            probability = 0.5;
        }
        if (Math.random() < probability) {
            return true;
        }
        return false;
    }
}
