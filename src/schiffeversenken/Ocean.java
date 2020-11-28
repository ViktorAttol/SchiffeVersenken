package schiffeversenken;

import dataExchanger.*;
import tcp.Client;
import tcp.Connection;
import tcp.Server;
import tcp.TCPConnector;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Scanner;

public class Ocean{

    boolean isActivePlayer = false;
    String  enemyAttackResult = "";
    boolean outputCanBeProcessed = false;
    UtilData utilData;
    SchiffeVersenkenImpl sv;
    public boolean runGame(String[] args) throws GameException, StatusException, IOException{

        if (args.length < 1 || args.length > 3) {
            System.err.println("False argument input!");
            System.err.println("For server: port startNumber");
            System.err.println("For client: hostname port startNumber");
        }
        //tcp relevant
        String hostname = null;
        int port = -1;
        int dimX = 6;
        int dimY = 0;
        Connection tcpConnection = null;

        //game relevant
        String userName = null;
        String enemyUserName = null;
        int startNumber = 0;
        boolean isActivePlayer = false;
        int numberOfShipPlaces = 8;
        GameState gameState = null;
        boolean isServer = false;

        //todo find better solution
        //server
        if (args.length == 2) {
            isServer = true;
            isActivePlayer = true;
            port = Integer.parseInt(args[0]);
            startNumber = Integer.parseInt(args[1]);
            //System.out.println("Please enter board size!");
            //dimX = Integer.parseInt(initializingScanner.nextLine());

            if (dimX < 4) dimX = 4;
            if (dimX > 16) dimX = 16;
            if (dimY < 3) dimX = 3;
            if (dimX > 15) dimX = 15;
            Server server = new TCPConnector();
            tcpConnection = server.acceptConnection(port);
            gameState = new GameStateImpl(dimX, dimY);
            GameDataSender gameDataSender = new GameDataExchanger();
            gameDataSender.sendGameLayout(gameState, tcpConnection.getOutputStream());

        }
        //client
        if (args.length == 3) {
            hostname = args[0];
            port = Integer.parseInt(args[1]);
            startNumber = Integer.parseInt(args[2]);
            Client client = new TCPConnector();
            tcpConnection = client.connect(hostname, port);
            GameDataReceiver gameDataReceiver = new GameDataExchanger();
            gameState = gameDataReceiver.receiveGameLayout(tcpConnection.getInputStream());
            dimX = gameState.getGameLayoutHeight();
        }

        userName = inputUsername();

        //initialize schiffeversenken und serealization
        sv = new SchiffeVersenkenImpl();
        ArrayList<BattleshipsBoardPosition> inputBoardPositions = inputShipPositions(numberOfShipPlaces);
        SVProtocolEngine svProtocolEngine = new SVProtocolEngine(sv, userName);
        sv.setProtocolEngine(svProtocolEngine);
        // input shipPositions
        svProtocolEngine.handleConnection(tcpConnection.getInputStream(), tcpConnection.getOutputStream());
        //share ship positions
        sv.placeShips(userName, inputBoardPositions);
        ArrayList<BattleshipsBoardPosition> placePositionsResult = svProtocolEngine.placeShips(userName, inputBoardPositions);


        utilData = new UtilData(svProtocolEngine);

        //todo
        //pre gameloop
        printArray(gameState.getGameState());
        String returnValue = "F";
        if(isServer){
            //sv.status = Status.ACTIVE_Player1;
        } else {
            //sv.status = Status.ACTIVE_Player2;
        }
        //gameloop
        System.out.println("bevore GameLoop");
        while (!returnValue.equals("W")) {
            returnValue = "F";
            //System.out.print("/");
            //for user
            //todo
            /*
            if (sv.status == Status.ACTIVE_Player1) {
//              if (isActivePlayer == true) {
                System.out.println("local is active player");
                BattleshipsBoardPosition attackPosition = getAttackPositionInput();
                String returnValueSynchronTest = sv.attackPos(userName, attackPosition);
                returnValue = svProtocolEngine.attackPos(userName, attackPosition);
                if (!returnValueSynchronTest.equals(returnValue)) {
                    throw new GameException("out of sync!");
                }
                checkForWin(returnValue);
                sv.status = Status.ACTIVE_Player2;
                //swapActivePlayer();
                System.out.println("End of activeplayer1 cycle");
            }

            else if (sv.status == Status.ACTIVE_Player2) {

                System.out.println("local is not active player, porcessing: " + outputCanBeProcessed);
                if (utilData.processable) {
                    System.out.println("remote is active player");
                    returnValue = utilData.result;
                    checkForWin(returnValue);
                    //swapActivePlayer();
                    utilData.result = "";
                    sv.status = Status.ACTIVE_Player1;
                    utilData.processable = false;
                }
            }

             */


            if (returnValue.equals("W")) {
                break;
            }
            //System.out.print("");
        }
        tcpConnection.getSocket().close();
        return true;
    }

        private void printArray(int[][] array){
            for (int[] i: array) {
                for(int j: i){
                    System.out.print(j + " ");
                }
                System.out.println("");
            }
            System.out.println("");
        }
//todo überarbeiten



        private String changePlayer(String player){
            if(player == player){
                return "";
            } else{
                return "";
            }
        }

        private BattleshipsBoardPosition getAttackPositionInput(){
            System.out.println("Please Enter the Position of your next attack!");
            Scanner scanner = new Scanner(System.in);
            String input = scanner.nextLine();
            BattleshipsBoardPosition position = new BattleshipsBoardPosition(input);
            return position;
        }

        private void swapActivePlayer(){
            if(isActivePlayer) isActivePlayer = false;
            else isActivePlayer = true;
        }

        private ArrayList<BattleshipsBoardPosition> inputShipPositions(int numberOfShipPlaces){
            // input shipPositions
            System.out.println("Please Enter " + numberOfShipPlaces + " positions for your ships. Bsb.: A0 B1 C5 ..."); //todo give information about borders
            ArrayList<BattleshipsBoardPosition> inputBoardPositions = new ArrayList<>();
            //Scanner scanner = new Scanner(System.in);
            //String inputShipPositions = null;
            //inputShipPositions = scanner.nextLine();
            String inputShipPositions = "A0 A1 A2 A3 B0 B1 B2 B3";
            String[] inputStringArray = inputShipPositions.split(" ");
            for (String input: inputStringArray) {
                inputBoardPositions.add(new BattleshipsBoardPosition(input));
            }
            return inputBoardPositions;
        }

        private String inputUsername(){
            System.out.println("Please enter player name!");
            Scanner scanner = new Scanner(System.in);
            String userName = scanner.nextLine();
            return userName;
        }

        //callback funktion
        public void attackResult(String attackResult) {
           // sv.status = Status.ACTIVE_Player1;
            utilData.result = attackResult;
            utilData.processable = true;
            System.out.println("callback in ocean is called");
        }
        private void checkForWin(String result){
            if(result.equals("W")){
                if(isActivePlayer == true) {
                    System.out.println("You won the game!");
                } else {
                    System.out.println("You loose the game!");
                }
            }
        }
    }

