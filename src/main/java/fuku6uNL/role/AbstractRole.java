package fuku6uNL.role;

import fuku6uNL.board.BoardSurface;
import org.aiwolf.common.data.Agent;

public abstract class AbstractRole {

    public abstract void dayStart(BoardSurface boardSurface);
    public abstract Agent vote();
    public abstract void talk();
    public abstract void finish();
}
