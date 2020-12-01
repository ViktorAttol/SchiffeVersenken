package schiffeversenken;

import network.ShipsPlacedListener;
import network.GameSessionEstablishedListener;
import network.ProtocolEngine;
import view.PrintStreamView;
import view.SVPrintStreamView;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class SchiffeVersenkenImpl implements SchiffeVersenken, GameSessionEstablishedListener, SVLocalBoard{
    private Status status = Status.START;

    private String localPlayer = null;
    private boolean isActive = false;
    private String remotePlayer = null;
    private boolean localShipsPlaced = false;
    private boolean remoteShipsPlaced = false;

    private boolean allShipsPlaced = false;

    private int shipPlaces = 8;
    private int squareBoardSize = 6;
    private int yDimBoardSize = 6;
    private ProtocolEngine protocolEngine;
    private ArrayList<String> allowedSCoordinates;
    HashMap<String, String> localMap = new HashMap<>(); //player1 ships
    HashMap<String, String> remoteMap = new HashMap<>(); //playe 2 ships
    public SchiffeVersenkenImpl(){
        setAllowedSCoordinates(6);
    }

    public SchiffeVersenkenImpl(String username){
        this.localPlayer = username;
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
        if(!userName.equals(localPlayer)) remotePlayer = userName;
        if(userName.equals(localPlayer)){
            SchiffeVersenken sv = (SVProtocolEngine)protocolEngine;
            sv.placeShips(this.localPlayer,positions);
        }

        ArrayList<BattleshipsBoardPosition> returnPositions = fillHashmapWithShipPositions(userName, positions);

        printMap(getHashmapForPlayer(userName, false));

        if(userName.equals(localPlayer)) localShipsPlaced = true;
        if(userName.equals(remotePlayer)) remoteShipsPlaced = true;
        if(localShipsPlaced && remoteShipsPlaced){
            allShipsPlaced = true;
            if(this.isActive) this.status = Status.ACTIVE_LOCAL;
            else this.status = Status.ACTIVE_REMOTE;
            //todo change state to local or remote player
        }
        notifyShipsPlaced(userName);
        return returnPositions;
    }

    @Override
    public ArrayList<BattleshipsBoardPosition> placeShips(ArrayList<BattleshipsBoardPosition> positions) throws GameException, StatusException {
        return placeShips(localPlayer, positions);
    }

    @Override
    public String attackPos(String userName, BattleshipsBoardPosition position) throws GameException, StatusException {
        if(this.status != Status.ACTIVE_LOCAL && this.status != Status.ACTIVE_REMOTE) throw new StatusException("attackPos call but wrong status! Status: " + this.status);
        //if(userName == null || (player1 != userName && player2 != userName)) throw new GameException("Invalid User! User: " + userName); //todo throws exception but playernames are proper set
        if((userName == localPlayer && this.status == Status.ACTIVE_REMOTE) || (userName == remotePlayer && this.status == Status.ACTIVE_LOCAL)) throw new StatusException("attackPos calle in other players move");
        if(!checkIfCoordinatesAreInBounds(position.getsCoordinate(), position.getiCoordinate())){
            throw new GameException("Invalid input from" + userName + "to attack! Input: " + position.getsCoordinate() + " " + position.getiCoordinate());
        }
        String result = prozessAttack(userName, position);
        if(userName.equals(this.localPlayer)){
            SchiffeVersenken sv = (SVProtocolEngine)protocolEngine;
            sv.attackPos(this.localPlayer, position);
        }

        System.out.println(userName + " is attacking position: " + position.getKey());
        printMap(getHashmapForPlayer(userName, true));

        if(userName != localPlayer) notifyBoardChanged();

        if(result.equals("W")){
            if(hasWon()){
                System.out.println(this.localPlayer + " has won the game!");
                this.status = Status.ENDED;
            }
            if(hasLost()){
                System.out.println(this.localPlayer + " has lost the game!");
                this.status = Status.ENDED;
            }
            return result;
        }
        if(status == Status.ACTIVE_LOCAL){
            status = Status.ACTIVE_REMOTE;
            this.isActive = false;
        }
        else {
            status = Status.ACTIVE_LOCAL;
            this.isActive = true;
        }
        return result;
    }

    @Override
    public boolean attackPos(BattleshipsBoardPosition position) throws GameException, StatusException {
        attackPos(localPlayer, position);
        return true;
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

    @Override
    public boolean setBoardSize(int xSize, int ySize) throws GameException, StatusException {
        return false;
    }

    private boolean checkIfCoordinatesAreInBounds(String sCoord, int iCoord){
        if(allowedSCoordinates.contains(sCoord) && iCoord >= 0 && iCoord < squareBoardSize){
            return true;
        }
        return false;
    }

    private HashMap<String, String> getHashmapForPlayer(String userName, boolean isAttacking){
        if(userName == localPlayer){
            if(isAttacking) return remoteMap;
            return localMap;
        } else {
            if(isAttacking) return localMap;
            return remoteMap;
        }
    }

    private boolean checkIfWon(HashMap<String, String> map){
        if(map.containsValue("O")) return false;
        return true;
    }


    private void setAllowedSCoordinates(int size){
        String[] letters = {"A", "B", "C", "D", "E", "F", "G", "H", "I", "J", "K", "L", "M", "N", "O", "P"};
        allowedSCoordinates = new ArrayList<>();
        for (int i = 0; i < size; i++) {
            allowedSCoordinates.add(letters[i]);
        }
    }

    public void setProtocolEngine(ProtocolEngine protocolEngine) {
        this.protocolEngine = protocolEngine;
        this.protocolEngine.subscribeGameSessionEstablishedListener(this);
    }

    public Status getStatus() {
        return this.status;
    }

    @Override
    public boolean isActive() {
        return this.isActive;
    }

    @Override
    public boolean hasWon() {
        if(remoteMap.containsValue("O")) return false;
        return true;
    }

    @Override
    public boolean hasLost() {

        if(!localMap.containsValue("O")) return true;
        return false;
    }

    private List<LocalBoardChangedListener> boardChangeListenerList = new ArrayList<>();
    @Override
    public void subscribeChangeListener(LocalBoardChangedListener changeListener) {
        this.boardChangeListenerList.add(changeListener);
    }

    private void notifyBoardChanged() {
        // are there any listeners ?
        if(this.boardChangeListenerList == null || this.boardChangeListenerList.isEmpty()) return;

        // yes - there are - create a thread and inform them
        (new Thread(new Runnable() {
            @Override
            public void run() {
                for(LocalBoardChangedListener listener : SchiffeVersenkenImpl.this.boardChangeListenerList) {
                    listener.changed();
                }
            }
        })).start();
    }

    private void printMap(HashMap<String, String> map){
        for (int i = 0; i < this.squareBoardSize; i++) {
            for (String sCoord: allowedSCoordinates) {
                String coord = sCoord + i;
                if(map.containsKey(coord)){
                    System.out.print(map.get(coord) + " ");
                } else {
                    System.out.print("N ");
                }
            }
            System.out.println("");
        }
        System.out.println("//////////////////////////////////////");
    }

    @Override
    public void gameSessionEstablished(boolean oracle, String partnerName) {

    }

    public PrintStreamView getPrintStreamView(){
        return new SVPrintStreamView(); //todo
    }

    public boolean areAllShipsPlaced() {
        return this.allShipsPlaced;
    }

    private List<ShipsPlacedListener> shipsPlacedListenerList = new ArrayList<>();

    @Override
    public void subscribeAllShipsPlacedListener(ShipsPlacedListener shipsPlacedListener) {
        this.shipsPlacedListenerList.add(shipsPlacedListener);
    }

    @Override
    public void setInitializationValues(boolean localIsStarting, String remoteName) {
        this.remotePlayer = remoteName;
        if(localIsStarting) this.isActive = true;
        this.status = Status.SHIPS_PLACEMENT;

    }

    private void notifyShipsPlaced(String userName) {
        // are there any listeners ?
        if(this.shipsPlacedListenerList == null || this.shipsPlacedListenerList.isEmpty()) return;

        // yes - there are - create a thread and inform them
        (new Thread(new Runnable() {
            @Override
            public void run() {
                for(ShipsPlacedListener listener : SchiffeVersenkenImpl.this.shipsPlacedListenerList) {
                    listener.shipsPlaced(userName);
                }
            }
        })).start();
    }

    public boolean hasPlacedShips(){
        return this.localShipsPlaced;
    }
}
