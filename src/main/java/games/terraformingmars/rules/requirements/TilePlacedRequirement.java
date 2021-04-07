package games.terraformingmars.rules.requirements;

import games.terraformingmars.TMGameState;
import games.terraformingmars.TMTypes;
import utilities.ImageIO;

import java.awt.*;

public class TilePlacedRequirement implements Requirement<TMGameState> {

    public TMTypes.Tile tile;
    int threshold;
    public boolean max;  // if true, value of counter must be <= threshold, if false >=
    boolean any;  // tiles placed by any player, or by the player who checks this

    public TilePlacedRequirement(TMTypes.Tile tile, int threshold, boolean max, boolean any) {
        this.tile = tile;
        this.threshold = threshold;
        this.max = max;
    }

    @Override
    public boolean testCondition(TMGameState gs) {
        int nPlaced = nPlaced(gs);
        if (max && nPlaced <= threshold) return true;
        return !max && nPlaced >= threshold;
    }

    private int nPlaced(TMGameState gs) {
        int player = gs.getCurrentPlayer();
        int nPlaced = 0;
        if (!any) {
            nPlaced = gs.getPlayerTilesPlaced()[player].get(tile).getValue();
        } else {
            for (int i = 0; i < gs.getNPlayers(); i++) {
                nPlaced = gs.getPlayerTilesPlaced()[i].get(tile).getValue();
            }
        }
        return nPlaced;
    }

    @Override
    public boolean isMax() {
        return max;
    }

    @Override
    public boolean appliesWhenAnyPlayer() {
        return any;
    }

    @Override
    public String getDisplayText(TMGameState gs) {
        return null;
    }

    @Override
    public String getReasonForFailure(TMGameState gs) {
        int nPlaced = nPlaced(gs);
        return nPlaced + "/" + threshold + " " + tile + " tiles placed" + (!any? " by you" : "");
    }

    @Override
    public Image[] getDisplayImages() {
        return new Image[] {ImageIO.GetInstance().getImage(tile.getImagePath())};
    }

    @Override
    public String toString() {
        return "Tile Placed";
    }
}