package schiffeversenken;

import network.ShipsPlacedListener;

import java.util.ArrayList;

public interface SVLocalBoard extends SchiffeVersenken{


    boolean attackPos(BattleshipsBoardPosition position) throws GameException, StatusException;

    ArrayList<BattleshipsBoardPosition> placeShips(ArrayList<BattleshipsBoardPosition> positions) throws GameException, StatusException;

    /**
     *
     * @return game status
     */
    Status getStatus();

    /**
     * @return if active - can set a piece, false otherwise
     */
    boolean isActive();

    /**
     *
     * @return true if ships are placed, false otherwise
     */
    boolean hasPlacedShips();

    /**
     * @return true if won, false otherwise
     */
    boolean hasWon();

    /**
     * @return true if lost, false otherwise
     */
    boolean hasLost();

    /**
     * Subscribe for changes
     * @param changeListener
     */
    void subscribeChangeListener(LocalBoardChangedListener changeListener);

    /**
     * subscribe for the notification of the end of the place state
     * @param shipsPlacedListener
     */
    void subscribeAllShipsPlacedListener(ShipsPlacedListener shipsPlacedListener);

    void setInitializationValues(boolean localIsStarting, String remoteName);
}
