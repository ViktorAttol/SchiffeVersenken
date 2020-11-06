package schiffeversenken;
import org.junit.Assert;
import org.junit.Test;

import java.util.ArrayList;

public class UsageTests {
    public static final String ALICE = "Alice";
    public static final String BOB = "Bob";
    public static final int X = 6;
    public static final int Y = 6;
    public static final int SHIPPLACES = 8;
    public static final String[] SHIPPOS1 = {"A", "0", "A", "1", "C", "2", "C", "3", "D", "0", "C", "5", "E", "2", "E", "3"};
    public static final String[] SHIPPOS2 = {"A", "0", "A", "5", "F", "0", "F", "5", "D", "0", "C", "5", "E", "2", "E", "3"};
    public static final String[] SHIPPOS3 = {"A", "0", "A", "0", "C", "2", "C", "5", "D", "0", "C", "5", "E", "2", "E", "3"};
    public static final String[] SHIPPOS4 = {"A", "1", "B", "3"};
    public static final String[] SHIPPOS5 = {"0", "A", "A", "5", "F", "0", "5", "F", "D", "0", "C", "5", "E", "2", "E", "3"};
    public static final String[] SHIPPOS6 = {"K", "8", "J", "-3", "F", "15", "F", "5", "D", "0", "C", "5", "E", "2", "E", "3"};



    private SchiffeVersenken getSchiffeVersenken(){
        return new SchiffeVersenkenImpl();
    }

    private ArrayList<BattleshipsBoardPosition> buildPositions(String[] positions){
        ArrayList<BattleshipsBoardPosition> shipPositions = new ArrayList<>();
        for(int i = 0; i < positions.length; i = i + 2){
            shipPositions.add(new BattleshipsBoardPosition(positions[i], Integer.parseInt(positions[(i+1)])));
        }
        return shipPositions;
    }

    @Test (expected = NullPointerException.class)
    public void emptyPositionList() throws GameException, StatusException{
        SchiffeVersenken sv = getSchiffeVersenken();

        ArrayList<BattleshipsBoardPosition> positions = sv.placeShips(ALICE, null);
    }

    @Test
    public void goodPositionList1() throws GameException, StatusException{
        SchiffeVersenken sv = getSchiffeVersenken();

        ArrayList<BattleshipsBoardPosition> positions = sv.placeShips(ALICE, buildPositions(SHIPPOS1));
        Assert.assertEquals(SHIPPLACES, positions.size());
        BattleshipsBoardPosition pos0 = positions.get(0);
        BattleshipsBoardPosition posMax = positions.get(SHIPPLACES - 1);
        Assert.assertEquals(SHIPPOS1[0], pos0.getsCoordinate());
        Assert.assertEquals(Integer.parseInt(SHIPPOS1[1]), pos0.getiCoordinate());
        Assert.assertEquals(SHIPPOS1[SHIPPOS1.length - 2], posMax.getsCoordinate());
        Assert.assertEquals(Integer.parseInt(SHIPPOS1[SHIPPOS1.length - 1]), posMax.getiCoordinate());
    }

    //Alice bevore Bob
    @Test
    public void goodPositionList2() throws GameException, StatusException {
        SchiffeVersenken sv = getSchiffeVersenken();

        ArrayList<BattleshipsBoardPosition> positions = sv.placeShips(ALICE, buildPositions(SHIPPOS1));
        ArrayList<BattleshipsBoardPosition> positions1 = sv.placeShips(BOB, buildPositions(SHIPPOS2));

        BattleshipsBoardPosition posAlice = positions.get(0);
        Assert.assertEquals(SHIPPOS1[0], posAlice.getsCoordinate());
        Assert.assertEquals(Integer.parseInt(SHIPPOS1[1]), posAlice.getiCoordinate());
        BattleshipsBoardPosition posBob = positions1.get(0);
        Assert.assertEquals(SHIPPOS2[0], posBob.getsCoordinate());
        Assert.assertEquals(Integer.parseInt(SHIPPOS2[1]), posBob.getiCoordinate());
    }

    //Bob bevore Alice
    @Test
    public void goodPositionList3() throws GameException, StatusException {
        SchiffeVersenken sv = getSchiffeVersenken();

        ArrayList<BattleshipsBoardPosition> positions1 = sv.placeShips(BOB, buildPositions(SHIPPOS2));
        ArrayList<BattleshipsBoardPosition> positions = sv.placeShips(ALICE, buildPositions(SHIPPOS1));

        BattleshipsBoardPosition posBob = positions1.get(0);
        Assert.assertEquals(SHIPPOS2[0], posBob.getsCoordinate());
        Assert.assertEquals(Integer.parseInt(SHIPPOS2[1]), posBob.getiCoordinate());
        BattleshipsBoardPosition posAlice = positions.get(0);
        Assert.assertEquals(SHIPPOS1[0], posAlice.getsCoordinate());
        Assert.assertEquals(Integer.parseInt(SHIPPOS1[1]), posAlice.getiCoordinate());
    }

    //Randfälle
    @Test
    public void goodPositionList4() throws GameException, StatusException{
        SchiffeVersenken sv = getSchiffeVersenken();

        ArrayList<BattleshipsBoardPosition> positions = sv.placeShips(ALICE, buildPositions(SHIPPOS2));
        BattleshipsBoardPosition pos0 = positions.get(0);
        BattleshipsBoardPosition pos1 = positions.get(1);
        BattleshipsBoardPosition pos2 = positions.get(2);
        BattleshipsBoardPosition pos3 = positions.get(3);
        Assert.assertEquals(SHIPPOS2[0], pos0.getsCoordinate());
        Assert.assertEquals(Integer.parseInt(SHIPPOS2[1]), pos0.getiCoordinate());
        Assert.assertEquals(SHIPPOS2[2], pos1.getsCoordinate());
        Assert.assertEquals(Integer.parseInt(SHIPPOS2[3]), pos1.getiCoordinate());
        Assert.assertEquals(SHIPPOS2[4], pos2.getsCoordinate());
        Assert.assertEquals(Integer.parseInt(SHIPPOS2[5]), pos2.getiCoordinate());
        Assert.assertEquals(SHIPPOS2[6], pos3.getsCoordinate());
        Assert.assertEquals(Integer.parseInt(SHIPPOS2[7]), pos3.getiCoordinate());
    }

    //double position
    @Test (expected = GameException.class)
    public void failureSet1() throws GameException, StatusException {
        SchiffeVersenken sv = getSchiffeVersenken();

        ArrayList<BattleshipsBoardPosition> positions = sv.placeShips(ALICE, buildPositions(SHIPPOS3));
    }
    //to less ship positions
    @Test (expected = GameException.class)
    public void failureSet2() throws GameException, StatusException {
        SchiffeVersenken sv = getSchiffeVersenken();

        ArrayList<BattleshipsBoardPosition> positions = sv.placeShips(ALICE, buildPositions(SHIPPOS4));
    }

    //iCoordinate and sCoordinate swapped
    @Test (expected = NumberFormatException.class)
    public void failureSet3() throws GameException, StatusException {
        SchiffeVersenken sv = getSchiffeVersenken();

        ArrayList<BattleshipsBoardPosition> positions = sv.placeShips(ALICE, buildPositions(SHIPPOS5));
    }

    //koordinates out of bounds
    @Test (expected = GameException.class)
    public void failureSet4() throws GameException, StatusException {
        SchiffeVersenken sv = getSchiffeVersenken();

        ArrayList<BattleshipsBoardPosition> positions = sv.placeShips(ALICE, buildPositions(SHIPPOS6));
    }

    @Test
    public void goodAttackInput1() throws GameException, StatusException {
        SchiffeVersenken sv = getSchiffeVersenken();
        ArrayList<BattleshipsBoardPosition> positions1 = sv.placeShips(BOB, buildPositions(SHIPPOS2));
        ArrayList<BattleshipsBoardPosition> positions = sv.placeShips(ALICE, buildPositions(SHIPPOS1));

        BattleshipsBoardPosition newPos = new BattleshipsBoardPosition("A", 0);
        String retVal1 = sv.attackPos(ALICE, newPos);
        newPos = new BattleshipsBoardPosition("A", 0);
        String retVal2 = sv.attackPos(BOB, newPos);
        Assert.assertEquals("X", retVal1);
        Assert.assertEquals("X", retVal2);
    }

    @Test
    public void goodAttackInput2() throws GameException, StatusException {
        SchiffeVersenken sv = getSchiffeVersenken();
        ArrayList<BattleshipsBoardPosition> positions1 = sv.placeShips(BOB, buildPositions(SHIPPOS2));
        ArrayList<BattleshipsBoardPosition> positions = sv.placeShips(ALICE, buildPositions(SHIPPOS1));

        BattleshipsBoardPosition newPos = new BattleshipsBoardPosition("A", 0);
        String retVal2 = sv.attackPos(BOB, newPos);
        newPos = new BattleshipsBoardPosition("A", 0);
        String retVal1 = sv.attackPos(ALICE, newPos);
        Assert.assertEquals("X", retVal2);
        Assert.assertEquals("X", retVal1);
    }

    /*
    public static final String[] SHIPPOS1 = {"A", "0", "A", "1", "C", "2", "C", "3", "D", "0", "C", "5", "E", "2", "E", "3"};
    public static final String[] SHIPPOS2 = {"A", "0", "A", "5", "F", "0", "F", "5", "D", "0", "C", "5", "E", "2", "E", "3"};
     */

    //game won bei Alice
    @Test
    public void goodAttackInput3() throws GameException, StatusException {
        SchiffeVersenken sv = getSchiffeVersenken();
        ArrayList<BattleshipsBoardPosition> positions1 = sv.placeShips(BOB, buildPositions(SHIPPOS2));
        ArrayList<BattleshipsBoardPosition> positions = sv.placeShips(ALICE, buildPositions(SHIPPOS1));

        BattleshipsBoardPosition newPos = new BattleshipsBoardPosition("A", 0);
        String retVal2 = sv.attackPos(BOB, newPos);
        newPos = new BattleshipsBoardPosition("A", 0);
        String retVal1 = sv.attackPos(ALICE, newPos);

        newPos = new BattleshipsBoardPosition("A", 1);
        retVal2 = sv.attackPos(BOB, newPos);
        newPos = new BattleshipsBoardPosition("A", 5);
        retVal1 = sv.attackPos(ALICE, newPos);

        newPos = new BattleshipsBoardPosition("A", 2);
        retVal2 = sv.attackPos(BOB, newPos);
        newPos = new BattleshipsBoardPosition("F", 0);
        retVal1 = sv.attackPos(ALICE, newPos);

        newPos = new BattleshipsBoardPosition("A", 3);
        retVal2 = sv.attackPos(BOB, newPos);
        newPos = new BattleshipsBoardPosition("F", 5);
        retVal1 = sv.attackPos(ALICE, newPos);

        newPos = new BattleshipsBoardPosition("C", 2);
        retVal2 = sv.attackPos(BOB, newPos);
        newPos = new BattleshipsBoardPosition("D", 0);
        retVal1 = sv.attackPos(ALICE, newPos);

        newPos = new BattleshipsBoardPosition("C", 0);
        retVal2 = sv.attackPos(BOB, newPos);
        newPos = new BattleshipsBoardPosition("C", 5);
        retVal1 = sv.attackPos(ALICE, newPos);

        newPos = new BattleshipsBoardPosition("A", 4);
        retVal2 = sv.attackPos(BOB, newPos);
        newPos = new BattleshipsBoardPosition("E", 2);
        retVal1 = sv.attackPos(ALICE, newPos);

        newPos = new BattleshipsBoardPosition("A", 5);
        retVal2 = sv.attackPos(BOB, newPos);
        newPos = new BattleshipsBoardPosition("E", 3);
        retVal1 = sv.attackPos(ALICE, newPos);
        Assert.assertEquals("F", retVal2);
        Assert.assertEquals("W", retVal1);
    }


    @Test
    public void borderAttackInput1() throws GameException, StatusException {
        SchiffeVersenken sv = getSchiffeVersenken();
        ArrayList<BattleshipsBoardPosition> positions1 = sv.placeShips(BOB, buildPositions(SHIPPOS2));
        ArrayList<BattleshipsBoardPosition> positions = sv.placeShips(ALICE, buildPositions(SHIPPOS1));

        BattleshipsBoardPosition newPos = new BattleshipsBoardPosition("A", 5);
        String retVal2 = sv.attackPos(BOB, newPos);
        newPos = new BattleshipsBoardPosition("A", 5);
        String retVal1 = sv.attackPos(ALICE, newPos);
        Assert.assertEquals("F", retVal2);
        Assert.assertEquals("X", retVal1);
    }

    @Test
    public void borderAttackInput2() throws GameException, StatusException {
        SchiffeVersenken sv = getSchiffeVersenken();
        ArrayList<BattleshipsBoardPosition> positions1 = sv.placeShips(BOB, buildPositions(SHIPPOS2));
        ArrayList<BattleshipsBoardPosition> positions = sv.placeShips(ALICE, buildPositions(SHIPPOS1));

        BattleshipsBoardPosition newPos = new BattleshipsBoardPosition("F", 5);
        String retVal2 = sv.attackPos(BOB, newPos);
        newPos = new BattleshipsBoardPosition("F", 5);
        String retVal1 = sv.attackPos(ALICE, newPos);
        Assert.assertEquals("F", retVal2);
        Assert.assertEquals("X", retVal1);
    }

    @Test(expected = GameException.class)
    public void failureAttackInput1() throws GameException, StatusException {
        SchiffeVersenken sv = getSchiffeVersenken();
        ArrayList<BattleshipsBoardPosition> positions1 = sv.placeShips(BOB, buildPositions(SHIPPOS2));
        ArrayList<BattleshipsBoardPosition> positions = sv.placeShips(ALICE, buildPositions(SHIPPOS1));

        BattleshipsBoardPosition newPos = new BattleshipsBoardPosition("K", -5);
        String retVal2 = sv.attackPos(BOB, newPos);
        newPos = new BattleshipsBoardPosition("~", 33);
        String retVal1 = sv.attackPos(ALICE, newPos);
    }

    @Test(expected = Exception.class)
    public void failureAttackInput2() throws GameException, StatusException {
        SchiffeVersenken sv = getSchiffeVersenken();
        ArrayList<BattleshipsBoardPosition> positions1 = sv.placeShips(BOB, buildPositions(SHIPPOS2));
        ArrayList<BattleshipsBoardPosition> positions = sv.placeShips(ALICE, buildPositions(SHIPPOS1));

        BattleshipsBoardPosition newPos = new BattleshipsBoardPosition("B", Integer.MIN_VALUE - 1);
        String retVal2 = sv.attackPos(BOB, newPos);
        newPos = new BattleshipsBoardPosition("A", Integer.MAX_VALUE + 1);
        String retVal1 = sv.attackPos(ALICE, newPos);
    }

    //double same pos
    @Test(expected = GameException.class)
    public void failureAttackInput3() throws GameException, StatusException {
        SchiffeVersenken sv = getSchiffeVersenken();
        ArrayList<BattleshipsBoardPosition> positions1 = sv.placeShips(BOB, buildPositions(SHIPPOS2));
        ArrayList<BattleshipsBoardPosition> positions = sv.placeShips(ALICE, buildPositions(SHIPPOS1));

        BattleshipsBoardPosition newPos = new BattleshipsBoardPosition("A", 0);
        String retVal2 = sv.attackPos(ALICE, newPos);
        newPos = new BattleshipsBoardPosition("A", 0);
        String retVal1 = sv.attackPos(ALICE, newPos);
    }

    @Test (expected = StatusException.class)
    public void statusTests1() throws GameException, StatusException {
        SchiffeVersenken sv = getSchiffeVersenken();

        BattleshipsBoardPosition newPos = new BattleshipsBoardPosition("A", 0);
        String retVal1 = sv.attackPos(ALICE, newPos);
        newPos = new BattleshipsBoardPosition("A", 0);
        String retVal2 = sv.attackPos(BOB, newPos);

        ArrayList<BattleshipsBoardPosition> positions1 = sv.placeShips(BOB, buildPositions(SHIPPOS2));
        ArrayList<BattleshipsBoardPosition> positions = sv.placeShips(ALICE, buildPositions(SHIPPOS1));
    }

    @Test (expected = StatusException.class)
    public void statusTests2() throws GameException, StatusException {
        SchiffeVersenken sv = getSchiffeVersenken();

        ArrayList<BattleshipsBoardPosition> positions1 = sv.placeShips(BOB, buildPositions(SHIPPOS2));
        ArrayList<BattleshipsBoardPosition> positions = sv.placeShips(ALICE, buildPositions(SHIPPOS1));

        BattleshipsBoardPosition newPos = new BattleshipsBoardPosition("A", 0);
        String retVal1 = sv.attackPos(ALICE, newPos);
        newPos = new BattleshipsBoardPosition("A", 0);
        String retVal2 = sv.attackPos(BOB, newPos);

        ArrayList<BattleshipsBoardPosition> positions3 = sv.placeShips(BOB, buildPositions(SHIPPOS2));
        ArrayList<BattleshipsBoardPosition> positions4 = sv.placeShips(ALICE, buildPositions(SHIPPOS1));
    }
}
