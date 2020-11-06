package dataExchanger;

public interface GameState {
    /**
     *
     * @return width of game layout
     */
    int getGameLayoutWidth();

    /**
     *
     * @return height of game layout
     */
    int getGameLayoutHeight();

    /**
     *
     * @return 2dim int array with game State, 0 for neutral, 1 for server, 2 for host
     */
    int[][] getGameState();
}
