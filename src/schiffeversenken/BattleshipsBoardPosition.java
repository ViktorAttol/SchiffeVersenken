package schiffeversenken;

public class BattleshipsBoardPosition {
    private final String sCoordinate;
    private final int iCoordinate;
    private final String key;

    private Ship shipOnPosition = null;

    public BattleshipsBoardPosition(String sCoordinate, int iCoordinate){
        this.sCoordinate = sCoordinate;
        this.iCoordinate = iCoordinate;
        this.key = sCoordinate + iCoordinate;
    }

    public BattleshipsBoardPosition(String key){
        this.key = key;
        this.sCoordinate = key.substring(0,1);
        this.iCoordinate = Integer.parseInt(key.substring(1));
    }

    public String getsCoordinate() {
        return this.sCoordinate;
    }

    public int getiCoordinate() {
        return this.iCoordinate;
    }

    public String getKey() {
        return this.key;
    }

    public Ship getShipOnPosition() {
        return this.shipOnPosition;
    }

    public void setShipOnPosition(Ship shipOnPosition) {
        this.shipOnPosition = shipOnPosition;
    }
}
