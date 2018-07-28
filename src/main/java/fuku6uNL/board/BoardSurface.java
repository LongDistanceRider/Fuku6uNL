package fuku6uNL.board;

import org.aiwolf.common.data.Agent;
import org.aiwolf.common.data.Role;
import org.aiwolf.common.data.Species;
import org.aiwolf.common.net.GameInfo;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class BoardSurface {

    // GameInfo
    private GameInfo gameInfo;
    // 各プレイヤー情報（自分を除く)
    private Map<Agent, PlayerInfo> playerInfoMap = new HashMap<>();
    // 役職持ち情報
    private RoleInfo roleInfo = new RoleInfo();
    // 占い師CO人数
    private int numSeerCo = 0;
    // 占い結果（発言した占い結果）
    Map<Agent, Species> divinedMap = new HashMap<>();
    // 占い結果（本当の占い結果）
    Map<Agent, Species> trueDvinedMap = new HashMap<>();


    // getter
    public GameInfo getGameInfo() {
        return gameInfo;
    }
    public Role getCoRole() {
        return roleInfo.getCoRole();
    }
    // setter
    public void setNumSeerCo(int numSeerCo) {
        this.numSeerCo = numSeerCo;
    }

    public void setCoRole (Role role) {
        roleInfo.setCoRole(role);
    }

    public BoardSurface(GameInfo gameInfo) {
        this.gameInfo = gameInfo;
        gameInfo.getAgentList().stream()
                .filter(agent -> agent != gameInfo.getAgent())
                .forEach(agent -> playerInfoMap.put(agent, new PlayerInfo()));
    }

    public void update(GameInfo gameInfo) {
        this.gameInfo = gameInfo;
    }
    /**
     * submitが発言したCO役職を保管
     *
     * @param submit 発言者
     * @param coRole 発言したCO役職
     */
    public void playerCoRole(Agent submit, Role coRole) {
        playerInfoMap.get(submit).addCoRole(coRole);
    }

    /**
     * submitが発言した占い結果を保管
     * @param submit 発言者
     * @param target 占い先
     * @param result 占い結果
     */
    public void playerDivMap(Agent submit, Agent target, Species result) {
        playerInfoMap.get(submit).putDivined(target, result);
    }

    /**
     * submitが発言した投票発言を保管
     * @param submit 発言者
     * @param target 投票発言先
     */
    public void playerVote(Agent submit, Agent target) {
        playerInfoMap.get(submit).addVote(target);
    }

    /**
     * ある役職をCOしたエージェントのリストを返す
     * @param role COした役職
     * @return roleをCOしたエージェントのリスト
     */
    public List<Agent> getCoAgentList(Role role) {
        List<Agent> coRoleAgentList = new ArrayList<>();
        playerInfoMap.forEach((agent, playerInfo) -> {
            if (playerInfo.getCoRole().equals(role)) {
                coRoleAgentList.add(agent);
            }
        });
        return coRoleAgentList;
    }

    /**
     * speciesで指定された占い判定を受けたエージェントのリストを返す
     * @param species HUMAN または WEREWOLF
     * @return 占い判定を受けたエージェントのリスト
     */
    public List<Agent> getDivinedAgentList(Species species) {
        List<Agent> divinedAgentList = new ArrayList<>();
        divinedMap.forEach((agent, result) -> {
            if (result.equals(species)) {
                divinedAgentList.add(agent);
            }
        });
        return divinedAgentList;
    }
}
