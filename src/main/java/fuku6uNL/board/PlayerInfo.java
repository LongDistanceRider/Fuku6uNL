package fuku6uNL.board;

import org.aiwolf.common.data.Agent;
import org.aiwolf.common.data.Role;
import org.aiwolf.common.data.Species;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

class PlayerInfo {

    private Role coRole;
    private Map<Agent, Species> divinedMap = new HashMap<>();
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
}
