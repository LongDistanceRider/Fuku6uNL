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
    Map<Agent, Species> trueDivinedMap = new HashMap<>();
    // PP
    private boolean PP = false;

    // getter
    public GameInfo getGameInfo() {
        return gameInfo;
    }
    public Role getCoRole() {
        return roleInfo.getCoRole();
    }

    public boolean isPP() {
        return PP;
    }

    // setter
    public void setPP(boolean PP) {
        this.PP = PP;
    }

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
            Role coRole = playerInfo.getCoRole();
            if (coRole != null) {
                if (coRole.equals(role)) {
                    coRoleAgentList.add(agent);
                }
            }
        });
        return coRoleAgentList;
    }

    /**
     * speciesで指定された占い判定を受けたエージェントのリストを返す（自分の（発言した）占い結果のみ）
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

    /**
     * speciesで指定された占い判定を受けたエージェントのリストを返す（自分の（本当の）占い結果のみ）
     * @param species HUMAN または WEREWOLF
     * @return 占い判定を受けたエージェントのリスト
     */
    public List<Agent> getTrueDivinedAgentList(Species species) {
        List<Agent> trueDivinedAgentList = new ArrayList<>();
        trueDivinedMap.forEach((agent, result) -> {
            if (result.equals(species)) {
                trueDivinedAgentList.add(agent);
            }
        });
        return trueDivinedAgentList;
    }

    /**
     * 占い結果（発言した）を保管
     * @param target 占い先
     * @param result 占い結果
     */
    public void putDivinedMap(Agent target, Species result) {
        divinedMap.put(target, result);
    }

    /**
     * 占い結果（本当）を保管
     * @param target 占い先
     * @param result 占い結果
     */
    public void putTrueDivinedMap(Agent target, Species result) {
        trueDivinedMap.put(target, result);
    }
}
