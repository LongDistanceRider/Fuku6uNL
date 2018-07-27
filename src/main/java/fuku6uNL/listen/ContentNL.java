package fuku6uNL.listen;

public class ContentNL {
    // NL話題
    private String nlTopic;
    // target
    private String target;
    public ContentNL(String nlTopic, String target) {
        this.nlTopic = nlTopic;
        this.target = target;
    }

    @Override
    public String toString() {
        return nlTopic + " " + target;
    }
}
