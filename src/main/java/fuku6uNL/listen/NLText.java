package fuku6uNL.listen;

import fuku6uNL.log.Log;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

class NLText {

    // PATH
    private static final String dir = System.getProperty("user.dir");
    // フィルタ情報
    private static List<String> filterList = new ArrayList<>();
    // タグファイル
    private static Map<String, String[]> tagMap = new HashMap<>();
    // タグ変換されたString
    private List<String> tagStringList = new ArrayList<>();
    // 生文から抽出されたtargetが入るリスト
    private List<String> targetList = new ArrayList<>();
    // 生文から抽出されたtag.csv上の1行が入るリスト key = 自然言語単語, value = [タグ名, (Role), (Species)]
    private ArrayDeque<Map.Entry<String, String[]>> tagEntryList = new ArrayDeque<>();

    static {
        // フィルタ情報の読み込み
        try {
            File csv = new File(dir + "/lib/filter-info.txt");

            BufferedReader bufferedReader = new BufferedReader(new FileReader(csv));
            String readLine;
            while ((readLine = bufferedReader.readLine()) != null) {
                filterList.add(readLine.trim());    // 制御文字と空白を削除してからリストへ追加
            }
            bufferedReader.close();
        } catch (IOException e) {
            Log.fatal("フィルタ情報読み込みでエラー" + e);
        }
        // タグファイルの読み込み
        try(BufferedReader bufferedReader = Files.newBufferedReader(Paths.get(dir + "/lib/tag.csv"))) {
            List<String> tagList = new ArrayList<>();
            String readLine;
            while((readLine = bufferedReader.readLine()) != null) {
                tagList.add(readLine.trim());    // 制御文字と空白を削除してからリストへ追加
            }

            for (String line :
                    tagList) {
                String[] arrayLine = line.split(",");
                tagMap.put(arrayLine[0], Arrays.copyOfRange(arrayLine, 1, arrayLine.length));
            }
        } catch (IOException e) {
            Log.fatal("タグファイルの読み込みでエラー" + e);
        }
    }

    // getter
    List<String> getTagStringList() {
        return tagStringList;
    }
    List<String> getTargetList() {
        return targetList;
    }

    NLText(String oneSentence) {
        // 句点までの内容にフィルターが引っかからない場合は句点までの文を削除する．
        String[] textArray = oneSentence.split("、");
        for (int j = 0; j < textArray.length; j++) {
            if (isChat(textArray[j])) {
                textArray[j] = "";
            }
        }

        // タグ変換する文をセット
        String sentence = String.join("", textArray);

        // 文をタグ変換（<TARGET>を除く）
        String convertToTag = sentence;
        for (Map.Entry<String, String[]> tagEntry :
                tagMap.entrySet()) {
            int index;  // タグ設定されている単語を発見したStringの要素番号
            String tmpSentence = sentence;  // 削除されながら走査される文字列
            while (true) {
                index = tmpSentence.indexOf(tagEntry.getKey()); // タグ文字を走査
                if (index != -1) {
                    addTagEntryList(tagEntry, sentence, index);  // 変換を保存
                    tmpSentence = tmpSentence.substring(index + tagEntry.getKey().length());    // 変換したタグ文字を削除して再走査
                } else {
                    break;  // 見つからなければループを抜ける
                }
            }
            convertToTag = convertToTag.replaceAll(tagEntry.getKey(), tagEntry.getValue()[0]); // タグ文字に変換
        }
        // タグ変換<TARGET>をする
        // -- "Agent["を走査してその後の"]"までを保存する 「Agent[01]は人狼」から「Agent[01]」を取り出す
        String tmpText = convertToTag;   // 削除されながら走査される文字列
        while (true) {
            int index = tmpText.indexOf("Agent[");
            if (index != -1) {
                int endIndex = tmpText.indexOf("]");
                if (endIndex != -1) {
                    addTargetList((String) tmpText.subSequence(index, endIndex + 1));
                    tmpText = tmpText.substring(endIndex + 1);
                } else {
                    Log.error("<TARGET>のタグ変換時にエラー発生 tmpText: " + tmpText);
                    break;
                }
            } else {
                break;
            }
        }
        Pattern pattern = Pattern.compile("Agent\\[[0-9]{2}]");
        Matcher matcher = pattern.matcher(convertToTag);
        tagStringList.add(matcher.replaceAll("<TARGET>"));  // タグ文字に変換
    }

    /**
     * 生文からtagに変換された文字列を返す
     *
     * @param tag 取得したいタグ（ex: <!-- <ROLE> -->）
     * @param cast いくつめのtagが欲しいか
     *             例えば「私は占い師で，Agent[01]は人狼でした」の文の場合は，
     *             「私はROLEで，Agent[01]はROLEでした」に変換される．
     * @return tagに変換された文字列
     */
    List<String> getTagStringList(String tag, int cast) {
        List<String> reTagStringList = new ArrayList<>();
        tagEntryList.forEach((Entry -> {
            if (Entry.getValue()[0].equals(tag)) {// Topic
                reTagStringList.add(Entry.getValue()[cast]);
            }
        }));
        return reTagStringList;
    }

    private void addTargetList(String target) {
        this.targetList.add(target);
    }

    private void addTagEntryList(Map.Entry<String, String[]> tagEntry, String sentence, int addIndex) {
        // sentenceの出現順にソートする必要があるため，addFirstかaddLastのどちらかを判定する
        for (Map.Entry<String, String[]> entry :
                tagEntryList) {
            int index = sentence.indexOf(entry.getKey());
            if (addIndex < index) {
                this.tagEntryList.addFirst(tagEntry);
                return;
            }
        }
        this.tagEntryList.addLast(tagEntry);
    }
    static boolean isChat(String text) {
        for (String filter : filterList) {
            if (text.contains(filter)) {
                return false;
            }
        }
        return true;
    }
}
