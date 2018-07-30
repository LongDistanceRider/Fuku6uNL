package fuku6uNL.board;

import org.aiwolf.common.data.Agent;
import org.aiwolf.common.data.Role;
import org.aiwolf.common.data.Species;

import java.util.*;

class PlayerInfo {

    private Role coRole;
    private Map<Agent, Species> divinedMap = new LinkedHashMap<>();
    private List<Agent> voteList = new ArrayList<>();

    public void addCoRole(Role coRole) {
        this.coRole = coRole;
    }

    public void putDivined(Agent target, Species result) {
        divinedMap.put(target, result);
    }

    public void addVote(Agent target) {
        voteList.add(target);
    }

    public Role getCoRole() {
        return coRole;
    }

    public List<Agent> getVoteList() {
        return voteList;
    }

    public Map<Agent, Species> getDivinedMap() {
        return divinedMap;
    }
}
