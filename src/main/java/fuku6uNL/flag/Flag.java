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

    public static LogLevel getMaxLogLevel() {
        return maxLogLevel;
    }
}
