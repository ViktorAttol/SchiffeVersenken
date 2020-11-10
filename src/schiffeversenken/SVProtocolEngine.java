package schiffeversenken;

import network.ProtocolEngine;

import java.io.*;
import java.util.ArrayList;

public class SVProtocolEngine implements SchiffeVersenken, Runnable, ProtocolEngine {
    private String name;
    private OutputStream os;
    private InputStream is;
    private final SchiffeVersenken gameEngine;

    private static final String DEFAULT_NAME = "anonymousProtocolEngine";


    private static final int METHOD_PLACE = 0;
    private static final int METHOD_ATTACK = 1;

    private Thread protocolThread = null;
    private boolean oracle;
    private String partnerName;

    public SVProtocolEngine(InputStream is, OutputStream os, SchiffeVersenken gameEngine) {
        this.is = is;
        this.os = os;
        this.gameEngine = gameEngine;
    }

    public SVProtocolEngine(SchiffeVersenken gameEngine, String name) {
        this.gameEngine = gameEngine;
        this.name = name;
    }

    public SVProtocolEngine(SchiffeVersenken gameEngine) {
        this(gameEngine, DEFAULT_NAME);
    }

    @Override
    public ArrayList<BattleshipsBoardPosition> placeShips(String userName, ArrayList<BattleshipsBoardPosition> positions) throws GameException, StatusException {
        DataOutputStream dos = new DataOutputStream(this.os);

        try{
            dos.writeInt(METHOD_PLACE);
            dos.writeUTF(userName);
            dos.writeInt(positions.size());
            for (BattleshipsBoardPosition position: positions) {
                dos.writeUTF(position.getsCoordinate());
                dos.writeInt(position.getiCoordinate());
            }
        } catch (IOException e) {
            throw new GameException("could not serialize command", e);
        }
        return null; //todo
    }

    private void deserializePlace() throws GameException{
        DataInputStream dis = new DataInputStream(this.is);
        ArrayList<BattleshipsBoardPosition> positions = new ArrayList<>();

        try{
            String userName = dis.readUTF();
            int arraySize = dis.readInt();
            for (int i = 0; i < arraySize; i++) {
                String sCoordinate = dis.readUTF();
                int iCoordinate = dis.readInt();

                positions.add(new BattleshipsBoardPosition(sCoordinate, iCoordinate));
            }
            this.gameEngine.placeShips(userName, positions);
        } catch (IOException | StatusException e) {
            throw new GameException("could not deserialize command ", e);
        }
    }

    @Override
    public String attackPos(String userName, BattleshipsBoardPosition position) throws GameException, StatusException {
        DataOutputStream dos = new DataOutputStream(this.os);

        try{
            dos.writeInt(METHOD_ATTACK);
            dos.writeUTF(userName);
            dos.writeUTF(position.getsCoordinate());
            dos.writeInt(position.getiCoordinate());
        } catch (IOException e) {
            throw new GameException("could not serialize command", e);
        }
        return null; //todo
    }

    private void deserializeAttack() throws GameException {
        DataInputStream dis = new DataInputStream(this.is);
        try {
            String userName = dis.readUTF();
            String sCoordinate = dis.readUTF();
            int iCoordinate = dis.readInt();
            BattleshipsBoardPosition position = new BattleshipsBoardPosition(sCoordinate, iCoordinate);

            this.gameEngine.attackPos(userName, position);
        } catch (IOException | StatusException e) {
            throw new GameException("could not deserialize command ", e);
        }
    }


    public void read() throws GameException{
        DataInputStream dis = new DataInputStream(this.is);

        try{
            int commandID = dis.readInt();
            switch (commandID){
                case METHOD_PLACE: this.deserializePlace(); break;
                case METHOD_ATTACK: this.deserializeAttack(); break;
                default: throw new GameException("Unknown method id: " + commandID);
            }
        } catch (IOException e) {
            throw new GameException("could not deserialize command ", e);
        }
    }

    @Override
    public void handleConnection(InputStream is, OutputStream os) throws IOException {
        this.is = is;
        this.os = os;
        this.protocolThread = new Thread(this);
        this.protocolThread.start();
    }

    @Override
    public void close() throws IOException {

    }

    @Override
    public boolean getOracle() throws StatusException {
        return false;
    }

    @Override
    public void subscribeGameSessionEstablishedListener(GameSessionEstablishedListener ocListener) {

    }

    @Override
    public void unsubscribeGameSessionEstablishedListener(GameSessionEstablishedListener ocListener) {

    }

    @Override
    public void run() {
        try{
            while(true){
                this.read();
            }
        } catch (GameException e) {
            System.err.println("exception called in protocol engine thread - fatal and stop");
            e.printStackTrace();
        }
    }
}
