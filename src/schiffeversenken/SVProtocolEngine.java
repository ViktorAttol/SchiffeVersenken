package schiffeversenken;

import network.GameSessionEstablishedListener;
import network.ProtocolEngine;

import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class SVProtocolEngine implements SchiffeVersenken, Runnable, ProtocolEngine{
    private String name;
    private OutputStream os;
    private InputStream is;
    private final SchiffeVersenken gameEngine;

    private static final String DEFAULT_NAME = "anonymousProtocolEngine";


    private static final int METHOD_PLACE = 0;
    private static final int METHOD_ATTACK = 1;
    private static final int RESULT_ATTACK = 2;
    private static final int RESULT_PLACE = 3;

    private Thread protocolThread = null;
    private Thread attackWaitThread = null;
    private Thread placeWaitThread = null;
    private String resultAttack;
    private ArrayList<BattleshipsBoardPosition> resultPlace;
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
            System.out.println("place called");
            dos.writeInt(METHOD_PLACE);
            dos.writeUTF(userName);
            dos.writeInt(positions.size());
            for (BattleshipsBoardPosition position: positions) {
                dos.writeUTF(position.getsCoordinate());
                dos.writeInt(position.getiCoordinate());
            }
            try{

                this.placeWaitThread = Thread.currentThread();
                Thread.sleep(Long.MAX_VALUE);
            } catch (InterruptedException e) {
                System.out.println("place thread is arouse - result arrived");
            }
            this.placeWaitThread = null;

            return this.resultPlace;
        } catch (IOException e) {
            throw new GameException("could not serialize command", e);
        }
    }

    private void deserializePlace() throws GameException{
        DataInputStream dis = new DataInputStream(this.is);
        ArrayList<BattleshipsBoardPosition> positions = new ArrayList<>();

        try{
            System.out.println("deserialize place called");
            String userName = dis.readUTF();
            int arraySize = dis.readInt();
            for (int i = 0; i < arraySize; i++) {
                String sCoordinate = dis.readUTF();
                int iCoordinate = dis.readInt();

                positions.add(new BattleshipsBoardPosition(sCoordinate, iCoordinate));
            }
            this.gameEngine.placeShips(userName, positions);

            DataOutputStream dos = new DataOutputStream(this.os);
            dos.writeInt(RESULT_PLACE);
        } catch (IOException | StatusException e) {
            throw new GameException("could not deserialize command ", e);
        }
    }

    @Override
    public String attackPos(String userName, BattleshipsBoardPosition position) throws GameException, StatusException {
        DataOutputStream dos = new DataOutputStream(this.os);

        try{
            System.out.println("attackPos called");
            dos.writeInt(METHOD_ATTACK);
            dos.writeUTF(userName);
            dos.writeUTF(position.getsCoordinate());
            dos.writeInt(position.getiCoordinate());

        try{
            this.attackWaitThread = Thread.currentThread();
            Thread.sleep(Long.MAX_VALUE);
        } catch (InterruptedException e) {
           System.out.println("attack thread is arouse - result arrived");
        }
        this.attackWaitThread = null;

        return this.resultAttack;

        } catch (IOException e) {
            throw new GameException("could not serialize command", e);
        }
    }

    @Override
    public boolean setBoardSize(int xSize, int ySize) throws GameException, StatusException {
        return false;
    }

    private void deserializeAttack() throws GameException {
        DataInputStream dis = new DataInputStream(this.is);
        try {
            System.out.println("deserialize attack");

            String userName = dis.readUTF();
            String sCoordinate = dis.readUTF();
            int iCoordinate = dis.readInt();
            BattleshipsBoardPosition position = new BattleshipsBoardPosition(sCoordinate, iCoordinate);

            String returnValue = this.gameEngine.attackPos(userName, position);

            DataOutputStream dos = new DataOutputStream(this.os);
            dos.writeInt(RESULT_ATTACK);
            dos.writeUTF(returnValue);
        } catch (IOException | StatusException e) {
            throw new GameException("could not deserialize command ", e);
        }
    }


    public boolean read() throws GameException{
        DataInputStream dis = new DataInputStream(this.is);

        try{
            int commandID = dis.readInt();
            switch (commandID){
                case METHOD_PLACE: this.deserializePlace(); break;
                case METHOD_ATTACK: this.deserializeAttack(); break;
                case RESULT_ATTACK: this.deserializeResultAttack(); break;
                case RESULT_PLACE: this.deserializeResultPlace(); break;
                default: throw new GameException("Unknown method id: " + commandID);
            }
        } catch (IOException e) {
            System.err.println("read: could not deserialize command");
            try {
                this.close();
            } catch (IOException ioException) {
                // ignore
            }
            return false;
        }
        return true;
    }

    private void deserializeResultAttack() throws GameException {
        DataInputStream dis = new DataInputStream(this.is);
        try{
            System.out.println("deserialize result of attack");
            this.resultAttack = dis.readUTF();
            this.attackWaitThread.interrupt();
        } catch (IOException e) {
            throw new GameException("Could not deserialize command", e);

        }
    }

    private void deserializeResultPlace() throws GameException{
        System.out.println("deserialize result place");
        this.resultPlace = null;
        this.placeWaitThread.interrupt();
        /*

        DataInputStream dis = new DataInputStream(this.is);
        try{
            System.out.println("deserialize result place");
            this.resultAttack = dis.readUTF();
        } catch (IOException e) {
            throw new GameException("Could not deserialize command", e);
        }

         */
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
        if(this.is != null) this.is.close();
        if(this.os != null) this.os.close();

    }

    @Override
    public boolean getOracle() throws StatusException {
        return this.oracle;
    }

    private List<GameSessionEstablishedListener> sessionCreatedListenerList = new ArrayList<>();
    //@Override
    public void subscribeGameSessionEstablishedListener(GameSessionEstablishedListener ocListener) {
        this.sessionCreatedListenerList.add(ocListener);
    }

    //@Override
    public void unsubscribeGameSessionEstablishedListener(GameSessionEstablishedListener ocListener) {
        this.sessionCreatedListenerList.remove(ocListener);
    }




    @Override
    public void run() {
        System.out.println("Protocol Engine started - flip a coin");
        long seed = this.hashCode() * System.currentTimeMillis();
        Random random = new Random(seed);

        int localInt = 0, remoteInt = 0;
        try {
            DataOutputStream dos = new DataOutputStream(this.os);
            DataInputStream dis = new DataInputStream(this.is);
            do {
                localInt = random.nextInt();
                //this.log("flip and take number " + localInt);
                dos.writeInt(localInt);
                remoteInt = dis.readInt();
            } while(localInt == remoteInt);

            this.oracle = localInt < remoteInt;
            //this.log("Flipped a coin and got an oracle == " + this.oracle);
            //this.oracleSet = true;

            // finally - exchange names
            dos.writeUTF(this.name);
            this.partnerName = dis.readUTF();
        } catch (IOException e) {
            e.printStackTrace();
        }


        // call listener
        if(this.sessionCreatedListenerList != null && !this.sessionCreatedListenerList.isEmpty()) {
            for(GameSessionEstablishedListener oclistener : this.sessionCreatedListenerList) {
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            Thread.sleep(1); // block a moment to let read thread start - just in case
                        } catch (InterruptedException e) {
                            // will not happen
                        }
                        oclistener.gameSessionEstablished(
                                SVProtocolEngine.this.oracle,
                                SVProtocolEngine.this.partnerName);
                    }
                }).start();
            }
        }

        try {
            boolean again = true;
            while(again) {
                again = this.read();
            }
        } catch (GameException e) {
            System.err.println("exception called in protocol engine thread - fatal and stop");
            e.printStackTrace();
            // leave while - end thread
        }
    }
}
