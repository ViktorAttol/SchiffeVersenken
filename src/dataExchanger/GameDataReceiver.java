package dataExchanger;

import java.io.IOException;
import java.io.InputStream;

public interface GameDataReceiver {
    /**
     * Received game data from stream and create game data object
     * @param is
     * @return set of game data
     */
    GameData receiveGameData(InputStream is) throws IOException;

    /**
     * Received layout data from stream and create game layout object
     * @param is
     * @return layout of game
     */
    GameState receiveGameLayout(InputStream is) throws IOException;
}
