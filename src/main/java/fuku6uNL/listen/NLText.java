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
    private List<String> tagStringList = new ArrayList();

    private List<String> targetList = new ArrayList<>();

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
        } catch (FileNotFoundException e) {
            Log.fatal("フィルタ情報読み込みでエラー" + e);
        } catch (IOException e) {
            Log.fatal("フィルタ情報読み込みでエラー" + e);
        }
        // タグファイルの読み込み
        try(BufferedReader bufferedReader = Files.newBufferedReader(Paths.get(dir + "/lib/tag.csv"))) {
            List<String> tagList = new ArrayList<>();
            String readLine = "";
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
    public List<String> getTagStringList() {
        return tagStringList;
    }
    public List<String> getTargetList() {
        return targetList;
    }

    public NLText(String text) {
        // 1文に分解する
        String[] oneSentences = text.split("[!.。！]");
        // 1文ずつ処理をする
        for (int i = 0; i < oneSentences.length; i++) {
            String sentence = oneSentences[i];
            // フィルタにかける「わーい」とか「頑張るぞー」とかは解析不要のため，コンティニュー
            if (sentence.equals("") || sentence.equals("Over")
                    || sentence.equals("Skip") || !filterList.contains(sentence)) {
                // 解析する必要のない発話を除外（中身のない発言，Over・Skip発言，雑談
                Log.trace("NL解析不要: " + sentence);
                continue;
            }

            // 句点までの内容にフィルターが引っかからない場合は句点までの文を削除する．
            String[] textArray = sentence.split("、");
            for (int j = 0; j < textArray.length; j++) {
                if (!filterList.contains(textArray[j])) {
                    textArray[j] = "";
                }
            }

            sentence = String.join("", textArray);

            // 文をタグ変換（<TARGET>を除く）
            String convertToTag = sentence;
            for (Map.Entry<String, String[]> tagEntry:
                    tagMap.entrySet()) {
                int index;
                String tmpText = sentence;  // 削除されながら走査される文字列
                while (true) {
                    index = tmpText.indexOf(tagEntry.getKey());
                    if (index != -1) {
                        addTagEntryList(tagEntry, sentence, index);  // 変換を保存
                        tmpText = tmpText.substring(index + tagEntry.getKey().length());    // 変換したとこまでの文字列を削除して再走査
                    } else {
                        break;
                    }
                }
                convertToTag = convertToTag.replaceAll(tagEntry.getKey(), tagEntry.getValue()[0]); // タグ文字に変換
            }
            // タグ変換<TARGET>をする
            // -- "Agent["を走査してその後の"]"までを保存する 「Agent[01]は人狼」から「Agent[01]」を取り出す
            String tmpText = convertToTag;   // 削除されながら走査される文字列
            while(true) {
                int index = tmpText.indexOf("Agent[");
                if (index != -1) {
                    int endIndex = tmpText.indexOf("]");
                    addTargetList((String)tmpText.subSequence(index, endIndex+1));
                    tmpText = tmpText.substring(endIndex+1);
                } else {
                    break;
                }
            }
            Pattern pattern = Pattern.compile("Agent\\[[0-9]{2}]");
            Matcher matcher = pattern.matcher(convertToTag);
            tagStringList.add(matcher.replaceAll("<TARGET>"));  // タグ文字に変換
        }

    }
    public List<String> getTagStringList (String tag, int cast) {
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
}
