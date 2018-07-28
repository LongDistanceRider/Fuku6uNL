package fuku6uNL.listen;

/**
 * Content型の自然言語話題バージョン
 * 話題と発言先を保持する構造体
 */
class ContentNL {
    // NL話題
    private String nlTopic;
    // target
    private String target;

    public String getNlTopic() {
        return nlTopic;
    }

    public String getTarget() {
        return target;
    }

    ContentNL(String nlTopic, String target) {
        this.nlTopic = nlTopic;
        this.target = target;
    }

    @Override
    public String toString() {
        return nlTopic + " " + target;
    }
}
