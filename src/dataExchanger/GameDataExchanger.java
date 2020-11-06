package dataExchanger;

import java.io.*;

public class GameDataExchanger implements GameDataSender, GameDataReceiver {
    @Override
    public void sendGameData(GameData data, OutputStream os) throws IOException {
        DataOutputStream daos = new DataOutputStream(os);

        daos.writeLong(data.getTimeStamp());
        daos.writeUTF(data.getPositionInput());
        daos.writeUTF(data.getPlayername());
    }

    @Override
    public GameData receiveGameData(InputStream is)  throws IOException {
        DataInputStream dais = new DataInputStream(is);

        long timeStamp = dais.readLong();
        String positionInput = dais.readUTF();
        String playerName = dais.readUTF();

        GameData returnGameData = new GameDataImpl(positionInput,playerName);
        return returnGameData;
    }

    @Override
    public void sendGameLayout(GameState layout, OutputStream os) throws IOException {
        DataOutputStream daos = new DataOutputStream(os);

        daos.writeInt(layout.getGameLayoutWidth());
        daos.writeInt(layout.getGameLayoutHeight());
    }

    @Override
    public GameState receiveGameLayout(InputStream is) throws IOException {
        DataInputStream dais = new DataInputStream(is);

        int width = dais.readInt();
        int height = dais.readInt();

        GameState gamestate = new GameStateImpl(width,height);
        return gamestate;
    }
}
