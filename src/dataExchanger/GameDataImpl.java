package dataExchanger;

import java.util.Date;

public class GameDataImpl implements GameData {

    private long timestamp;
    private String positionInput;
    private String playerName;

    public GameDataImpl(String positionInput, String playerName){
        Date date = new Date();
        this.timestamp = date.getTime();
        this.positionInput = positionInput;
        this.playerName = playerName;
    }

    @Override
    public long getTimeStamp() {
        return this.timestamp;
    }

    @Override
    public String getPositionInput() {
        return this.positionInput;
    }

    @Override
    public String getPlayername() {
        return this.playerName;
    }
}
