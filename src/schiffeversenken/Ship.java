package schiffeversenken;

public class Ship {
    ShipTypes type;
    BattleshipsBoardPosition[] positions;

    Ship(ShipTypes type, BattleshipsBoardPosition[] positions){
        this.type = type;
        this.positions = positions;
    }

    public ShipTypes getType() {
        return type;
    }

    public BattleshipsBoardPosition[] getPositions() {
        return positions;
    }
}
