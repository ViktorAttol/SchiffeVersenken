package schiffeversenken;

import network.ProtocolEngine;
import network.TCPStream;
import org.junit.Assert;
import org.junit.Test;

import java.io.*;
import java.util.ArrayList;

public class ProtocolEngineTests {
    public static final String ALICE = "Alice";
    public static final String BOB = "Bob";
    public static final int X = 6;
    public static final int Y = 6;
    public static final int SHIPPLACES = 8;
    public static final int PORTNUMBER = 5555;
    public static final int TEST_THREAD_SLEEP_DURATION = 1000;
    private static int port = 3333;
    public static final String[] SHIPPOS1 = {"A", "0", "A", "1", "C", "2", "C", "3", "D", "0", "C", "5", "E", "2", "E", "3"};
    public static final String[] SHIPPOS2 = {"A", "0", "A", "5", "F", "0", "F", "5", "D", "0", "C", "5", "E", "2", "E", "3"};
    public static final String[] SHIPPOS3 = {"A", "0", "A", "0", "C", "2", "C", "5", "D", "0", "C", "5", "E", "2", "E", "3"};
    public static final String[] SHIPPOS4 = {"A", "1", "B", "3"};
    public static final String[] SHIPPOS5 = {"0", "A", "A", "5", "F", "0", "5", "F", "D", "0", "C", "5", "E", "2", "E", "3"};
    public static final String[] SHIPPOS6 = {"K", "8", "J", "-3", "F", "15", "F", "5", "D", "0", "C", "5", "E", "2", "E", "3"};

    private SchiffeVersenken getSVEngine(InputStream is, OutputStream os, SchiffeVersenken gameEngine){
        return new SVProtocolEngine(is, os, gameEngine);
    }

    private ArrayList<BattleshipsBoardPosition> buildPositions(String[] positions){
        ArrayList<BattleshipsBoardPosition> shipPositions = new ArrayList<>();
        for(int i = 0; i < positions.length; i = i + 2){
            shipPositions.add(new BattleshipsBoardPosition(positions[i], Integer.parseInt(positions[(i+1)])));
        }
        return shipPositions;
    }

    private int getPortNumber() {
        if(ProtocolEngineTests.port == 0) {
            ProtocolEngineTests.port = PORTNUMBER;
        } else {
            ProtocolEngineTests.port++;
        }

        System.out.println("use portnumber " + ProtocolEngineTests.port);
        return ProtocolEngineTests.port;
    }

    /* out of order
    @Test
    public void placeTest1() throws GameException, StatusException{
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        SchiffeVersenken svProtocolSender = this.getSVEngine(null, baos, null);

        ArrayList<BattleshipsBoardPosition> positions = svProtocolSender.placeShips(ALICE, buildPositions(SHIPPOS1));

        // simulate network
        byte[] serializedBytes = baos.toByteArray();
        ByteArrayInputStream bais = new ByteArrayInputStream(serializedBytes);

        SVToReadTester svReceiver = new SVToReadTester();
        SchiffeVersenken svProtocolReceiver = this.getSVEngine(bais, null, svReceiver);

        SVProtocolEngine svEngine = (SVProtocolEngine) svProtocolReceiver;
        svEngine.read();

        BattleshipsBoardPosition testPos = new BattleshipsBoardPosition("A", 0);
        Assert.assertTrue(svReceiver.lastCallPlace);
        Assert.assertTrue(svReceiver.userName.equalsIgnoreCase(ALICE));
        Assert.assertTrue(testPos.getKey().equalsIgnoreCase(svReceiver.positions.get(0).getKey()));
    }

     */

    @Test
    public void attackTest1() throws GameException, StatusException {
        String sValue = "A";
        int iValue = 0;
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        SchiffeVersenken svProtocolSender = this.getSVEngine(null, baos, null);

        String resultValue = svProtocolSender.attackPos(ALICE, new BattleshipsBoardPosition(sValue, iValue));

        // simulate network
        byte[] serializedBytes = baos.toByteArray();
        ByteArrayInputStream bais = new ByteArrayInputStream(serializedBytes);

        SVToReadTester svReceiver = new SVToReadTester();
        SchiffeVersenken svProtocolReceiver = this.getSVEngine(bais, null, svReceiver);

        SVProtocolEngine svEngine = (SVProtocolEngine) svProtocolReceiver;
        svEngine.read();

        Assert.assertTrue(svReceiver.lastCallAttack);
        Assert.assertTrue(svReceiver.userName.equalsIgnoreCase(ALICE));
        Assert.assertEquals("F", svReceiver.result);
        Assert.assertEquals(sValue + Integer.toString(iValue), svReceiver.position.getKey());
        //todo
    }

    @Test
    public void integrationTest1() throws IOException, InterruptedException {
        SchiffeVersenkenImpl aliceGameEngine = new SchiffeVersenkenImpl();
        SVProtocolEngine aliceSVProtocolEngine = new SVProtocolEngine(aliceGameEngine, ALICE);

        aliceGameEngine.setProtocolEngine(aliceSVProtocolEngine);

        SchiffeVersenkenImpl bobGameEngine = new SchiffeVersenkenImpl();
        SVProtocolEngine bobSVProtocolEngine = new SVProtocolEngine(bobGameEngine, BOB);

        bobGameEngine.setProtocolEngine(bobSVProtocolEngine);

        // Setup
        int port = this.getPortNumber();
        // this stream plays TCP server role during connection establishment
        TCPStream aliceSide = new TCPStream(port, true, "aliceSide");
        // this stream plays TCP client role during connection establishment
        TCPStream bobSide = new TCPStream(port, false, "bobSide");
        // start both stream
        aliceSide.start(); bobSide.start();
        // wait until TCP connection is established
        aliceSide.waitForConnection(); bobSide.waitForConnection();

        aliceSVProtocolEngine.handleConnection(aliceSide.getInputStream(), aliceSide.getOutputStream());
        bobSVProtocolEngine.handleConnection(bobSide.getInputStream(), bobSide.getOutputStream());

        Thread.sleep(TEST_THREAD_SLEEP_DURATION);

        Assert.assertTrue(aliceGameEngine.getStatus() == bobGameEngine.getStatus());
        aliceSVProtocolEngine.close();
        bobSVProtocolEngine.close();

    }

    @Test
    public void placeShipsTest()throws GameException, StatusException, IOException,InterruptedException{

        // alices`s game engine tester
        SVToReadTester aliceGameEngineTester = new SVToReadTester();
        //real protocol engine on alice´s side
        SVProtocolEngine aliceSVProtocolEngine = new SVProtocolEngine(aliceGameEngineTester);

        //protocol engine
        ProtocolEngine aliceProtocolEngine = aliceSVProtocolEngine;
        SchiffeVersenken aliceGameEngineSide = aliceSVProtocolEngine;

        //bobs game engine Tester
        SVToReadTester bobGameEngineTester = new SVToReadTester();
        ProtocolEngine bobProtocolEngine = new SVProtocolEngine(bobGameEngineTester);

        // Setup
        int port = this.getPortNumber();
        // this stream plays TCP server role during connection establishment
        TCPStream aliceSide = new TCPStream(port, true, "aliceSide");
        // this stream plays TCP client role during connection establishment
        TCPStream bobSide = new TCPStream(port, false, "bobSide");
        // start both stream
        aliceSide.start(); bobSide.start();
        // wait until TCP connection is established
        aliceSide.waitForConnection(); bobSide.waitForConnection();

        // launch
        aliceProtocolEngine.handleConnection(aliceSide.getInputStream(), aliceSide.getOutputStream());
        bobProtocolEngine.handleConnection(bobSide.getInputStream(), bobSide.getOutputStream());

        Thread.sleep(1000);

        // run test scenario // todo
        ArrayList<BattleshipsBoardPosition> positions = aliceSVProtocolEngine.placeShips(ALICE, buildPositions(SHIPPOS1));
        ArrayList<BattleshipsBoardPosition> positions1 = aliceSVProtocolEngine.placeShips(BOB, buildPositions(SHIPPOS1));
        BattleshipsBoardPosition testPos = new BattleshipsBoardPosition("A", 0);
        Assert.assertEquals("F", aliceGameEngineSide.attackPos(BOB, testPos));

}

        private class SVToReadTester implements SchiffeVersenken{
        private boolean lastCallPlace = false;
        private boolean lastCallAttack = false;

        private String userName = null;
        private ArrayList<BattleshipsBoardPosition> positions;
        private BattleshipsBoardPosition position;
        private String result = null;

        @Override
        public ArrayList<BattleshipsBoardPosition> placeShips(String userName, ArrayList<BattleshipsBoardPosition> positions) throws GameException, StatusException {
            this.lastCallPlace = true;
            this.lastCallAttack = false;
            this.userName = userName;
            this.positions = positions;
            return this.positions;
        }

        @Override
        public String attackPos(String userName, BattleshipsBoardPosition position) throws GameException, StatusException {
            this.lastCallPlace = false;
            this.lastCallAttack = true;
            this.userName = userName;
            this.position = position;
            this.result = "F";
            return "F";
        }

            @Override
            public boolean setBoardSize(int xSize, int ySize) throws GameException, StatusException {
                return false;
            }
        }
}
