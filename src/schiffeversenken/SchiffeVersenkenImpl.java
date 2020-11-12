package schiffeversenken;

import java.util.ArrayList;
import java.util.HashMap;

public class SchiffeVersenkenImpl implements SchiffeVersenken {
    private Status status = Status.START;
    private String player1 = null;
    private String player2 = null;
    private int shipPlaces = 8;
    private int squareBoardSize = 6;
    private int yDimBoardSize = 6;
    private ArrayList<String> allowedSCoordinates;
    HashMap<String, String> mapPlayer1 = new HashMap<>(); //player1 ships
    HashMap<String, String> mapPlayer2 = new HashMap<>(); //playe 2 ships

    public SchiffeVersenkenImpl(){
        setAllowedSCoordinates(6);
    }
    public SchiffeVersenkenImpl(int shipPlaces, int boardSize){
        int size = boardSize;
        this.shipPlaces = shipPlaces;
        if(size > 16) size = 16;
        this.squareBoardSize = size;
        setAllowedSCoordinates(size);
    }

    @Override
    public ArrayList<BattleshipsBoardPosition> placeShips(String userName, ArrayList<BattleshipsBoardPosition> positions) throws GameException, StatusException {
        if(this.status != Status.START && this.status != Status.SHIPS_PLACEMENT) throw new StatusException("placeShips call but wrong status! Status: " + this.status);
        if(userName == null) throw new GameException("userName == null");
        if(positions.size() != shipPlaces) throw new GameException("Invalid ArrayList size for ship positions from " + userName + ". Size should be: " + shipPlaces + ", but is " + positions.size());
        if(positions == null) throw new GameException("placeShips: positions == null");

        if(player1 == null){
            player1 = userName;
        } else if(player1 != userName && player2 == null){
            player2 = userName;
        } else{
            throw new GameException("Invalid try from " + userName + "to place ships!");
        }
        changeStatusToShipPlacement();
        ArrayList<BattleshipsBoardPosition> returnPositions = fillHashmapWithShipPositions(userName, positions);
        if(player1 != null && player2 != null){
            changeStatusAfterShipPlacement();
        }
        return returnPositions;
    }

    @Override
    public String attackPos(String userName, BattleshipsBoardPosition position) throws GameException, StatusException {
        if(this.status != Status.ACTIVE_Player1 && this.status != Status.ACTIVE_Player2) throw new StatusException("attackPos call but wrong status! Status: " + this.status);
        if(userName == null || (player1 != userName && player2 != userName)) throw new GameException("Invalid User! User: " + userName);
        if(!checkIfCoordinatesAreInBounds(position.getsCoordinate(), position.getiCoordinate())){
            throw new GameException("Invalid input from" + userName + "to attack! Input: " + position.getsCoordinate() + " " + position.getiCoordinate());
        }
        return prozessAttack(userName, position);
    }

    @Override
    public boolean setBoardSize(int xSize, int ySize) throws GameException, StatusException {
        return false;
    }


    private String prozessAttack(String userName, BattleshipsBoardPosition position) throws GameException{
        HashMap<String, String> mapTempPositions = getHashmapForPlayer(userName, true);

        if(mapTempPositions.containsKey(position.getKey())){
            String posValue = mapTempPositions.get(position.getKey());
            if(posValue == "F" || posValue == "X"){
                throw new GameException("attack pos was already cleared!");
            }
            if(posValue == "O"){
                mapTempPositions.put(position.getKey(), "X");
                if(checkIfWon(mapTempPositions)){
                    this.status = Status.ENDED;
                    return "W";
                }
                return "X";
            }
        } else {
            mapTempPositions.put(position.getKey(), "F");
        }
        return "F";
    }

    private ArrayList<BattleshipsBoardPosition> fillHashmapWithShipPositions(String userName, ArrayList<BattleshipsBoardPosition> positions)throws GameException{
        HashMap<String, String> mapTempPositions = getHashmapForPlayer(userName, false);
        ArrayList<BattleshipsBoardPosition> returnPositions = new ArrayList<BattleshipsBoardPosition>();

        for (BattleshipsBoardPosition position: positions) {
            if(!checkIfCoordinatesAreInBounds(position.getsCoordinate(), position.getiCoordinate())){
                throw new GameException("placeShips: koordinates are out of bounds");
            }
            if(mapTempPositions.containsKey(position.getKey())) {
                throw new GameException("placeShips: same element found twice");
            }
            mapTempPositions.put(position.getKey(), "O");
            BattleshipsBoardPosition newPos = new BattleshipsBoardPosition(position.getsCoordinate(), position.getiCoordinate());
            returnPositions.add(newPos);

        }
        return returnPositions;
    }

    private boolean checkIfCoordinatesAreInBounds(String sCoord, int iCoord){
        if(allowedSCoordinates.contains(sCoord) && iCoord >= 0 && iCoord < squareBoardSize){
            return true;
        }
        return false;
    }

    private HashMap<String, String> getHashmapForPlayer(String userName, boolean isAttacking){
        if(userName == player1){
            if(isAttacking) return mapPlayer2;
            return mapPlayer1;
        } else {
            if(isAttacking) return mapPlayer1;
            return mapPlayer2;
        }
    }

    private boolean checkIfWon(HashMap<String, String> map){
        if(map.containsValue("O")) return false;
        return true;
    }

    private boolean startPlayerCalculation(int numberServer, int numberClient){
        if((numberServer + numberClient) % 2 == 0){
            return true;
        }
        return false;
    }

    private void changeStatusToShipPlacement(){
        this.status = Status.SHIPS_PLACEMENT;
    }

    private void changeStatusAfterShipPlacement(){
        this.status = Status.ACTIVE_Player1;
    }

    private void setAllowedSCoordinates(int size){
        String[] letters = {"A", "B", "C", "D", "E", "F", "G", "H", "I", "J", "K", "L", "M", "N", "O", "P"};
        allowedSCoordinates = new ArrayList<>();
        for (int i = 0; i < size; i++) {
            allowedSCoordinates.add(letters[i]);
        }
    }

}
