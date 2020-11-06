package dataExchanger;

import java.io.IOException;
import java.io.OutputStream;

public interface GameDataSender {
    /**
     * send game data set
     * @param data
     * @param os stream to recipient
     */

    void sendGameData(GameData data, OutputStream os) throws IOException;
    /**
     * send game layout
     * @param layout width and height of layout
     * @param os stream to recipient
     */
    void sendGameLayout(GameState layout, OutputStream os) throws IOException;
}
