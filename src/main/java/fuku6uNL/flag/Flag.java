package fuku6uNL.flag;

import fuku6uNL.log.LogLevel;

/**
 * 実験時に使用するフラグの一括管理構造体クラス
 * 制約：
 *  private static finalのみ保持
 *  getterのみ実装
 */
public class Flag {

    /* Logを書き出す最大レベル */
    private static final LogLevel maxLogLevel = LogLevel.DEBUG;

    /* ファイルを書き出すか */
    private static final boolean write = false;

    // getter
    public static LogLevel getMaxLogLevel() {
        return maxLogLevel;
    }

    public static boolean isWrite() {
        return write;
    }
}
