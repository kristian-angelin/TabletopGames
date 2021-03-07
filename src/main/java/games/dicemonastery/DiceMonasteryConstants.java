package games.dicemonastery;

import core.interfaces.IGamePhase;

import java.awt.*;

public class DiceMonasteryConstants {

    public static Color[] playerColours = {Color.GREEN, Color.RED, Color.BLUE, Color.ORANGE};

    public enum ActionArea {
        MEADOW(1), KITCHEN(2), WORKSHOP(3),
        GATEHOUSE(1), LIBRARY(4), CHAPEL(1),
        DORMITORY(0), STOREROOM(0), SUPPLY(0),
        RETIRED(0), GRAVEYARD(0);

        public final int dieMinimum;

        ActionArea(int dieMinimum) {
            this.dieMinimum = dieMinimum;
        }

        public ActionArea next() {
            switch (this) {
                case MEADOW:
                    return KITCHEN;
                case KITCHEN:
                    return WORKSHOP;
                case WORKSHOP:
                    return GATEHOUSE;
                case GATEHOUSE:
                    return LIBRARY;
                case LIBRARY:
                    return CHAPEL;
                case CHAPEL:
                    return MEADOW;
                default:
                    throw new AssertionError("Should not be processing " + this);
            }
        }
    }

    public enum Resource {
        GRAIN, HONEY, WAX, SKEP, BREAD, SHILLINGS, PRAYERS, PIGMENT, INK, CALF_SKIN, VELLUM, BEER, MEAD, CANDLE, BERRIES
    }

    public enum Phase implements IGamePhase {
        PLACE_MONKS, USE_MONKS
    }

    public enum Season {
        SPRING, SUMMER, AUTUMN, WINTER;

        public Season next() {
            switch (this) {
                case SPRING:
                    return SUMMER;
                case SUMMER:
                    return AUTUMN;
                case AUTUMN:
                    return WINTER;
                case WINTER:
                    return SPRING;
                default:
                    throw new AssertionError("Should not be processing " + this);
            }
        }

    }

    public final static int[] RETIREMENT_REWARDS = {6, 5, 4, 3, 2, 1};

    // rows are number of players; columns are ordinal position in bidding
    public final static int[][] VIKING_REWARDS = {
            {0, 0, 0, 0},
            {2, 0, 0, 0},
            {4, 2, 0, 0},
            {6, 4, 2, 0}
    };
}