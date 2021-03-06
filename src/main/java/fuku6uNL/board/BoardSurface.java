package fuku6uNL.board;

import fuku6uNL.utterance.Utterance;
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
    // 強制投票先
    private Agent forceVoteTarget;

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
    public Map<Agent, Species> getDivinedMap() {
        return divinedMap;
    }

    public Map<Agent, Species> getTrueDivinedMap() {
        return trueDivinedMap;
    }

    public Agent getForceVoteTarget() {
        return forceVoteTarget;
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

    public void setForceVoteTarget(Agent forceVoteTarget) {
        this.forceVoteTarget = forceVoteTarget;
    }

    /**
     * Initialize
     * @param gameInfo ゲーム情報
     */
    public void initialize(GameInfo gameInfo) {
        this.gameInfo = gameInfo;
        gameInfo.getAgentList().stream()
                .filter(agent -> agent != gameInfo.getAgent())
                .forEach(agent -> playerInfoMap.put(agent, new PlayerInfo()));
        // TODO ロジックエラーを取り除くために自分自身もPlayerInfoに入れるが，これは設計に予定していない．ロジックエラーが発生するのは，Map.getKey(me)した時
        playerInfoMap.put(gameInfo.getAgent(), new PlayerInfo());
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
    /**
     * 占い結果（発言した）を保管
     * @param target 占い先
     * @param result 占い結果
     */
    public void putDivinedMap(Agent target, Species result) {
        divinedMap.put(target, result);
    }

    /* === === === === === === === === === === === === === === === === === === === === === */
    /*  状況 */
    /* === === === === === === === === === === === === === === === === === === === === === */

    /**
     * 占い結果（本当の）からtargetのSpeciesを取得する
     * @param target 知りたいエージェント
     * @return 結果があればSpeciesを返す．結果がない場合はnullを返却
     */
    public Species getTrueDivinedMapResult (Agent target) {
        for (Map.Entry<Agent, Species> divinedEntry :
                trueDivinedMap.entrySet()) {
            if (divinedEntry.getKey().equals(target)) {
                return divinedEntry.getValue();
            }
        }
        return null;
    }
    /* === === === === === === === === === === === === === === === === === === === === === */
    /*  追放者・被害者情報 */
    /* === === === === === === === === === === === === === === === === === === === === === */

    /* === === === === === === === === === === === === === === === === === === === === === */
    /* 特定の条件に合うエージェントを返す */
    /* === === === === === === === === === === === === === === === === === === === === === */

    /**
     * checkTargetにcheckResultを出したエージェントを返すメソッド
     * ex:
     *  checkTarget = 自分，checkResult = WEREWOLFの時
     *  自分に黒出ししたエージェントリストが帰ってくる
     *
     * @param checkTarget 調べたいターゲット
     * @param checkResult 条件となるSpecies
     * @return 条件にあったエージェントリスト
     */
    public List<Agent> divinedTargetResult(Agent checkTarget, Species checkResult) {
        List<Agent> correctAgentList = new ArrayList<>();
        playerInfoMap.forEach(((agent, playerInfo) ->  {
            Map<Agent, Species> divined = playerInfo.getDivinedMap();
            divined.forEach(((target, result) -> {
                if (target.equals(checkTarget) && result.equals(checkResult)) {
                    correctAgentList.add(agent);
                }
            }));
        }));
        return correctAgentList;
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
    /* === === === === === === === === === === === === === === === === === === === === === */
    /* その他 */

    /* === === === === === === === === === === === === === === === === === === === === === */




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
    /* *** initialize-on-demand holder *** */
    public static BoardSurface getInstance() {
        return BoardSurface.BoardSurfaceHolder.INSTANCE;
    }
    private BoardSurface(){}
    private static class BoardSurfaceHolder {
        static final BoardSurface INSTANCE = new BoardSurface();
    }
    /* *** *** */
}
