package schiffeversenken;

class BattleshipsBoardPosition {
    private final String sCoordinate;
    private final int iCoordinate;

    private final String key;

    BattleshipsBoardPosition(String sCoordinate, int iCoordinate){
        this.sCoordinate = sCoordinate;
        this.iCoordinate = iCoordinate;
        this.key = sCoordinate + iCoordinate;
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
}
