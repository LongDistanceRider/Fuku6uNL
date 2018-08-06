package fuku6uNL.board;

import org.aiwolf.client.lib.Topic;
import org.aiwolf.common.data.Agent;
import org.aiwolf.common.data.Role;
import org.aiwolf.common.data.Species;
import org.aiwolf.common.net.GameInfo;

import java.util.*;


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
    private Map<Agent, Species> divinedMap = new LinkedHashMap<>();
    // 占い結果（本当の占い結果）
    private Map<Agent, Species> trueDivinedMap = new LinkedHashMap<>();
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
    public int getNumSeerCo() {
        return numSeerCo;
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

    /**
     * Constructor
     * @param gameInfo ゲーム情報
     */
    public BoardSurface(GameInfo gameInfo) {
        this.gameInfo = gameInfo;
        gameInfo.getAgentList().stream()
                .filter(agent -> agent != gameInfo.getAgent())
                .forEach(agent -> playerInfoMap.put(agent, new PlayerInfo()));
    }

    /**
     * update()で呼び出される関数
     * @param gameInfo ゲーム情報
     */
    public void update(GameInfo gameInfo) {
        this.gameInfo = gameInfo;
    }

    /* === === === === === === === === === === === === === === === === === === === === === */
    /*  agentが受けている印象 */
    /* === === === === === === === === === === === === === === === === === === === === === */
    /* === === === === === === === === === === === === === === === === === === === === === */
    /*  agentの発言 */
    /* === === === === === === === === === === === === === === === === === === === === === */
    // getter
    /**
     * エージェントが発言したCO役職を返す
     * @param submit
     * @return
     */
    public Role getPlayerCoRole(Agent submit) {
        return playerInfoMap.get(submit).getCoRole();
    }

    /**
     * エージェントが発言した占い結果を返す
     * @param submit
     * @return
     */
    public Map<Agent, Species> getPlayerDivMap(Agent submit) {
        return playerInfoMap.get(submit).getDivinedMap();
    }

    /**
     * エージェントが発言した投票先を返す
     * @param submit
     * @return
     */
    public List<Agent> getPlayerVote(Agent submit) {
        return playerInfoMap.get(submit).getVoteList();
    }
    // setter
    /**
     * エージェントが発言したCO役職を保管
     *
     * @param submit 発言者
     * @param coRole 発言したCO役職
     */
    public void setPlayerCoRole(Agent submit, Role coRole) {
        playerInfoMap.get(submit).addCoRole(coRole);
    }

    /**
     * エージェントが発言した占い結果を保管
     * @param submit 発言者
     * @param target 占い先
     * @param result 占い結果
     */
    public void setPlayerDivMap(Agent submit, Agent target, Species result) {
        playerInfoMap.get(submit).putDivined(target, result);
    }

    /**
     * エージェントが発言した投票発言を保管
     * @param submit 発言者
     * @param target 投票発言先
     */
    public void setPlayerVote(Agent submit, Agent target) {
        playerInfoMap.get(submit).addVote(target);
    }

    /* === === === === === === === === === === === === === === === === === === === === === */
    /*  meの発言 */
    /* === === === === === === === === === === === === === === === === === === === === === */
    /* === === === === === === === === === === === === === === === === === === === === === */
    /*  状況 */
    /* === === === === === === === === === === === === === === === === === === === === === */

    /* === === === === === === === === === === === === === === === === === === === === === */
    /*  追放者・被害者情報 */
    /* === === === === === === === === === === === === === === === === === === === === === */

    /* === === === === === === === === === === === === === === === === === === === === === */
    /* 推論後のagentに対する情報 */
    /* === === === === === === === === === === === === === === === === === === === === === */

    /* === === === === === === === === === === === === === === === === === === === === === */
    /* その他 */
    /* === === === === === === === === === === === === === === === === === === === === === */

    /**
     * 呼び出された時点での真占い師を返す（自分の役職が占い師以外を想定）
     * @return
     */
    public Agent trueSeerAgent() {
        List<Agent> seerCoAgentList = getCoAgentList(Role.SEER);
        switch (numSeerCo) {
            case 1:
                if (seerCoAgentList.size() == 1) {
                    return seerCoAgentList.get(0);
                }
            case 2:
                // 自分に黒出ししたエージェントはいるか
                for (Agent seerCoAgent :
                        seerCoAgentList) {
                    Map<Agent, Species> divined = getPlayerDivMap(seerCoAgent);
                    divined.forEach((target, result) -> {
                        if (target.equals(getGameInfo().getAgent()) && result.equals(Species.WEREWOLF)) {
                            seerCoAgentList.remove(seerCoAgent);
                        }
                    });
                }
                if (seerCoAgentList.size() == 1) {
                    return seerCoAgentList.get(0);
                }
                break;
        }
        return null;
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

//    /**
//     * 最後の（自分が出した）占い結果を取得する
//     * @return 最後の占い結果
//     */
//    public Map.Entry<Agent, Species> getLatestDivinedMap(){
//        Map.Entry<Agent, Species> latestDivinedMap = null;
//        for (Map.Entry<Agent, Species> divinedMapEntry:
//                divinedMap.entrySet()) {
//            latestDivinedMap = divinedMapEntry;
//        }
//        return latestDivinedMap;
//    }

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

//    /**
//     * speciesで指定された占い判定を受けたエージェントのリストを返す（他プレイヤからの占い結果のみ）
//     * @param species HUMAN or WEREWOLF
//     * @return 占い判定を受けたエージェントのリスト
//     */
//    public List<Agent> getPlayerDivinedAgentList(Species species) {
//        List<Agent> agentList = new ArrayList<>();
//        List<Agent> seerCoAgentList = getCoAgentList(Role.SEER);
//        seerCoAgentList.forEach(agent -> {
//            Map.Entry<Agent, Species> divinedMap = getLatestDivinedMap(agent);
//            if (divinedMap.getValue().equals(species)) {
//                agentList.add(divinedMap.getKey());
//            }
//        });
//        return agentList;
//    }
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

    /**
     * 最大投票数を受けたAgentを返す
     * @return 最大投票数を受けたAgent（同数の場合はどちらか一方）
     */
    // TODO 同数の場合にどうするか
    public Agent maxVotedAgent() {
        Map<Agent, Integer> votedMap = getVotedAgentMap();

        int maxCount = 0;
        Agent maxVotedAgent = null;
        for (Map.Entry<Agent, Integer> voteEntry :
                votedMap.entrySet()) {
            if (voteEntry.getValue() < maxCount) {
                maxVotedAgent = voteEntry.getKey();
            }
        }

        return maxVotedAgent;
    }

    /**
     * 投票されたAgentリストを返す（最後に投票発言をしたエージェントだけをリスト追加）
     *
     * @return key=被投票者 value=投票数のマップ
     */
    private Map<Agent, Integer> getVotedAgentMap () {
        List<Agent> votedAgentList = new ArrayList<>();
        playerInfoMap.forEach((agent, playerInfo) -> {
            List<Agent> votedAgent = playerInfo.getVoteList();
            if (!votedAgent.isEmpty()) {
                votedAgentList.add(votedAgent.get(votedAgent.size()-1));
            }
        });

        Map<Agent, Integer> votedMap = new HashMap<>();
        for (Agent votedAgent :
                votedAgentList) {
            int count = votedMap.getOrDefault(votedAgent, 0);
            votedMap.put(votedAgent, count+1);
        }
        return votedMap;
    }

    /**
     * Agentが最後に発言した占い結果を返す
     * @param seer 占い結果を発言したエージェント
     * @return Agentが最後に発言した占い結果（nullあり）
     */
    public Map.Entry<Agent, Species> getLatestDivinedMap(Agent seer) {
        Map<Agent, Species> divinedMap = playerInfoMap.get(seer).getDivinedMap();
        Map.Entry<Agent, Species> latestDivinedMap = null;
        for (Map.Entry<Agent, Species> divinedMapEntry:
                divinedMap.entrySet()) {
            latestDivinedMap = divinedMapEntry;
        }
        return latestDivinedMap;
    }

    /**
     * submitが最後に発言した投票先エージェントを取得
     * @param submit 発言者
     * @return 最後に発言した投票先エージェント
     */
    public Agent submitVoteAndTarget(Agent submit) {
        List<Agent> voteList = playerInfoMap.get(submit).getVoteList();
        if (!voteList.isEmpty()) {
            return voteList.get(voteList.size()-1);
        }
        return null;
    }
}
