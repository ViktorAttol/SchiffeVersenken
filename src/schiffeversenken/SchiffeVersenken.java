package schiffeversenken;

import java.util.ArrayList;

public interface SchiffeVersenken {

    /**
     * Method will ask the Player for input to set the position of his ships
     * @param userName username
     * @param positions positions user wants to set his ships
     * @return positions of the placed  ships
     * @throws GameException 2 ships on the same pos, or pos out of table
     * @throws StatusException can only be called if game hasn`t started yet
     */
    ArrayList<BattleshipsBoardPosition> placeShips(String userName, ArrayList<BattleshipsBoardPosition> positions) throws GameException, StatusException;

    /**
     * attack at position
     * @param position koordinates for the attack
     * @param userName player who attacks
     * @return F fail, X hit, W won //todo enum nutzen
     * @throws GameException 2 ships on the same pos, or pos out of table
     * @throws StatusException can only be called if game has started
     */
    String attackPos(String userName, BattleshipsBoardPosition position) throws GameException, StatusException;
}
