package fuku6uNL.listen;

import org.aiwolf.common.data.Agent;
import org.aiwolf.common.data.Role;

/**
 * Content型の自然言語話題バージョン
 * 話題と発言先を保持する構造体
 */
class ContentNL {
    // NL話題
    private String nlTopic;
    // target
    private Agent target;
    // role
    private Role role;

    String getNlTopic() {
        return nlTopic;
    }

    Agent getTarget() {
        return target;
    }

    Role getRole() {
        return role;
    }

    ContentNL(String nlTopic, Agent target) {
        this.nlTopic = nlTopic;
        this.target = target;
    }

    ContentNL(String nlTopic, Agent target, Role role) {
        this.nlTopic = nlTopic;
        this.target = target;
        this.role = role;
    }

    @Override
    public String toString() {
        return nlTopic + " " + target;
    }
}
