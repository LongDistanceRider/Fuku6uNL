package fuku6uNL.board;

import org.aiwolf.common.data.Agent;
import org.aiwolf.common.data.Role;
import org.aiwolf.common.data.Species;

import java.util.*;

class PlayerInfo {

    private Role coRole;
    private Map<Agent, Species> divinedMap = new LinkedHashMap<>();
    private List<Agent> voteList = new ArrayList<>();

    void addCoRole(Role coRole) {
        this.coRole = coRole;
    }

    void putDivined(Agent target, Species result) {
        divinedMap.put(target, result);
    }

    void addVote(Agent target) {
        voteList.add(target);
    }

    Role getCoRole() {
        return coRole;
    }

    List<Agent> getVoteList() {
        return voteList;
    }

    Map<Agent, Species> getDivinedMap() {
        return divinedMap;
    }
}
