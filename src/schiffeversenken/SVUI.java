package schiffeversenken;

import network.*;

import java.io.*;
import java.util.ArrayList;
import java.util.NoSuchElementException;
import java.util.StringTokenizer;

public class SVUI implements TCPStreamCreatedListener, GameSessionEstablishedListener, LocalBoardChangedListener, ShipsPlacedListener {
        private static final String PRINT = "print";
        private static final String EXIT = "exit";
        private static final String CONNECT = "connect";
        private static final String OPEN = "open";
        private static final String Attack = "attack";
        private static final String Place = "place";

        private final PrintStream outStream;
        private final BufferedReader inBufferedReader;
        private final String playerName;
        private final SchiffeVersenkenImpl gameEngine;
        private final SVLocalBoard localBord;
        private TCPStream tcpStream;
        private ProtocolEngine protocolEngine;
        private String partnerName;

    public static void main(String[] args) throws IOException {
        System.out.println("Welcome to Battleships!");

        if (args.length < 1) {
            System.err.println("need playerName as parameter");
            System.exit(1);
        }

        System.out.println("Welcome " + args[0]);
        System.out.println("Let's play a game");

        SVUI userCmd = new SVUI(args[0], System.out, System.in);

        userCmd.printUsage();
        userCmd.runCommandLoop();
    }

    public SVUI(String playerName, PrintStream os, InputStream is) throws IOException {
        this.playerName = playerName;
        this.outStream = os;
        this.inBufferedReader = new BufferedReader(new InputStreamReader(is));

        this.gameEngine = new SchiffeVersenkenImpl(playerName);
        this.localBord = this.gameEngine;
        this.localBord.subscribeChangeListener(this);
        this.localBord.subscribeAllShipsPlacedListener(this);
    }

    public void printUsage() {
        StringBuilder b = new StringBuilder();

        b.append("\n");
        b.append("\n");
        b.append("valid commands:");
        b.append("\n");
        b.append(CONNECT);
        b.append(".. connect as tcp client");
        b.append("\n");
        b.append(OPEN);
        b.append(".. open port become tcp server");
        b.append("\n");
        b.append(PRINT);
        b.append(".. print board");
        b.append("\n");
        b.append(Attack);
        b.append(".. attack a piece");
        b.append("\n");
        b.append(Place);
        b.append(".. place ships");
        b.append("\n");
        b.append(EXIT);
        b.append(".. exit");

        this.outStream.println(b.toString());
    }

    public void runCommandLoop() {
        boolean again = true;

        while (again) {
            boolean rememberCommand = true;
            String cmdLineString = null;

            try {
                // read user input
                cmdLineString = inBufferedReader.readLine();

                // finish that loop if less than nothing came in
                if (cmdLineString == null) break;

                // trim whitespaces on both sides
                cmdLineString = cmdLineString.trim();

                // extract command
                int spaceIndex = cmdLineString.indexOf(' ');
                spaceIndex = spaceIndex != -1 ? spaceIndex : cmdLineString.length();

                // got command string
                String commandString = cmdLineString.substring(0, spaceIndex);

                // extract parameters string - can be empty
                String parameterString = cmdLineString.substring(spaceIndex);
                parameterString = parameterString.trim();

                // start command loop
                switch (commandString) {
                    case PRINT:
                        this.doPrint();
                        break;
                    case CONNECT:
                        this.doConnect(parameterString);
                        break;
                    case OPEN:
                        this.doOpen();
                        break;
                    case Attack:
                        this.doAttack(parameterString);
                        // redraw
                        this.doPrint();
                        break;
                    case Place:
                        this.doPlace(parameterString);
                        break;
                    case "q": // convenience
                    case EXIT:
                        again = false; this.doExit(); break; // end loop

                    default:
                        this.outStream.println("unknown command:" + cmdLineString);
                        this.printUsage();
                        rememberCommand = false;
                        break;
                }
            } catch (IOException ex) {
                this.outStream.println("cannot read from input stream - fatal, give up");
                try {
                    this.doExit();
                } catch (IOException e) {
                    // ignore
                }
            } catch (StatusException ex) {
                this.outStream.println("wrong status: " + ex.getLocalizedMessage());
            } catch (GameException ex) {
                this.outStream.println("game exception: " + ex.getLocalizedMessage());
            } catch (RuntimeException ex) {
                this.outStream.println("runtime problems: " + ex.getLocalizedMessage());
            }
        }
    }

    private void doPlace(String parameterString) throws StatusException, GameException {
        this.checkConnnectionStatus();

        ArrayList<BattleshipsBoardPosition> inputBoardPositions = new ArrayList<>();

        String inputShipPositions = parameterString;
        //String inputShipPositions = "A0 A1 A2 A3 B0 B1 B2 B3"; // ui testing
        String[] inputStringArray = inputShipPositions.split(" ");
        for (String input: inputStringArray) {
            inputBoardPositions.add(new BattleshipsBoardPosition(input));
        }
        this.gameEngine.placeShips(inputBoardPositions);
    }

    /// ui methods ///

    private void doAttack(String parameterString) throws StatusException, GameException {
        // call guards
        this.checkConnnectionStatus();

        BattleshipsBoardPosition position = new BattleshipsBoardPosition(parameterString);

        this.gameEngine.attackPos(position);
    }

    private void doExit() throws IOException {
        // shutdown engines which needs to be
        this.protocolEngine.close();
    }

    private void doOpen() {
        if (this.alreadyConnected()) return;

        this.tcpStream = new TCPStream(SchiffeVersenken.DEFAULT_PORT, true, this.playerName);
        this.tcpStream.setStreamCreationListener(this);
        this.tcpStream.start();
    }

    private void doConnect(String parameterString) {
        if (this.alreadyConnected()) return;

        String hostname = null;

        try {
            StringTokenizer st = new StringTokenizer(parameterString);
            hostname = st.nextToken();
        }
        catch(NoSuchElementException e) {
            System.out.println("no hostname provided - take localhost");
            hostname = "localhost";
        }

        this.tcpStream = new TCPStream(SchiffeVersenken.DEFAULT_PORT, false, this.playerName);
        this.tcpStream.setRemoteEngine(hostname);
        this.tcpStream.setStreamCreationListener(this);
        this.tcpStream.start();
    }

    private void doPrint() throws IOException {
        this.gameEngine.getPrintStreamView().print(System.out);

        if(this.gameEngine.getStatus() == Status.ENDED) {
            if(this.gameEngine.hasWon()) {
                System.out.println("you won");
            } else {
                System.out.println("you lost");
            }
        } else if(this.gameEngine.getStatus() == Status.SHIPS_PLACEMENT) {
            if(!this.gameEngine.hasPlacedShips()) System.out.println("Place your ships!");
        } else{
            if(this.gameEngine.isActive()) {
                System.out.println("your turn");
            } else {
                System.out.println("please wait");
            }
        }
    }

    /// utils ///

    /**
     * Guard method - checks if already connected
     *
     * @throws StatusException
     */
    private void checkConnnectionStatus() throws StatusException {
        if (this.protocolEngine == null) {
            throw new StatusException("not yet connected - call connect or open before");
        }
    }

    private boolean alreadyConnected() {
        if (this.tcpStream != null) {
            System.err.println("connection already established or connection attempt in progress");
            return true;
        }

        return false;
    }

    /// Listener ///

    @Override
    public void gameSessionEstablished(boolean oracle, String partnerName) {
        System.out.println("game session created");
        this.partnerName = partnerName;
        this.gameEngine.setInitializationValues(oracle, partnerName);
        try {
            this.doPrint();
        } catch (IOException e) {
            System.err.println("very very unexpected: " + e.getLocalizedMessage());
        }
    }

    @Override
    public void streamCreated(TCPStream stream) {
        // connection established - setup protocol engine
        System.out.println("stream created - setup engine - we can play quite soon.");
        this.protocolEngine = new SVProtocolEngine(this.gameEngine, this.playerName);
        this.gameEngine.setProtocolEngine(protocolEngine);

        this.protocolEngine.subscribeGameSessionEstablishedListener(this);

        try {
            protocolEngine.handleConnection(stream.getInputStream(), stream.getOutputStream());
        } catch (IOException e) {
            System.err.println("cannot get streams from tcpStream - fatal, give up: " + e.getLocalizedMessage());
            System.exit(1);
        }
    }

    @Override
    public void changed() {
        try {
            this.doPrint();
        } catch (IOException e) {
            System.err.println("very very unexpected: " + e.getLocalizedMessage());
        }
    }

    @Override
    public void shipsPlaced(String userName) {
        System.out.println(userName + " placed his ships");
        if(this.gameEngine.areAllShipsPlaced()) {
            System.out.println("All ships placed");
        }

        try {
            this.doPrint();
        } catch (IOException e) {
            System.err.println("very very unexpected: " + e.getLocalizedMessage());
        }

    }
}
