package fuku6uNL.Player;

import org.aiwolf.sample.lib.AbstractRoleAssignPlayer;

public class Fuku6uNLRoleAssignPlayer extends AbstractRoleAssignPlayer {

    public Fuku6uNLRoleAssignPlayer() {
        setSeerPlayer(new Fuku6uNL());
        setPossessedPlayer(new Fuku6uNL());
        setWerewolfPlayer(new Fuku6uNL());
        setVillagerPlayer(new Fuku6uNL());
        setBodyguardPlayer(new Fuku6uNL());
        setMediumPlayer(new Fuku6uNL());
    }

    @Override
    public String getName() {
        return "Fuku6u";
    }
}

