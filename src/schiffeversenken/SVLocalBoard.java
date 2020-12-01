package schiffeversenken;

import network.ShipsPlacedListener;

import java.util.ArrayList;

public interface SVLocalBoard extends SchiffeVersenken{

    /**
     * local player attacks remote player on position.
     * @param position
     * @return
     * @throws GameException
     * @throws StatusException
     */
    boolean attackPos(BattleshipsBoardPosition position) throws GameException, StatusException;

    /**
     * local player places his ships
     * @param positions
     * @return
     * @throws GameException
     * @throws StatusException
     */
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

    /**
     * set additional SchiffeVersenkenImpl initialization data
     * @param localIsStarting true if the local player attacks first
     * @param remoteName name of the remote player
     */
    void setInitializationValues(boolean localIsStarting, String remoteName);
}
