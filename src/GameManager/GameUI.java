package GameManager;

import dataExchanger.*;
import schiffeversenken.*;
import tcp.Client;
import tcp.Connection;
import tcp.Server;
import tcp.TCPConnector;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Scanner;

public class GameUI {

    public static void main(String[] args) throws IOException, InterruptedException, GameException, StatusException {
        if(args.length < 1 || args.length > 3){
            System.err.println("False argument input!");
            System.err.println("For server: port startNumber");
            System.err.println("For client: hostname port startNumber");
            return;
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

        //todo find better solution
        //server
        if(args.length == 2){
            isActivePlayer = true;
            port = Integer.parseInt(args[0]);
            startNumber = Integer.parseInt(args[1]);
            //System.out.println("Please enter board size!");
            //dimX = Integer.parseInt(initializingScanner.nextLine());

            if(dimX < 4) dimX = 4;
            if(dimX > 16) dimX = 16;
            if(dimY < 3) dimX = 3;
            if(dimX > 15) dimX = 15;
            Server server = new TCPConnector();
            tcpConnection = server.acceptConnection(port);
            gameState = new GameStateImpl(dimX, dimY);
            GameDataSender gameDataSender = new GameDataExchanger();
            gameDataSender.sendGameLayout(gameState, tcpConnection.getOutputStream());

        }
        //client
        if(args.length == 3){
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
        SchiffeVersenkenImpl sv = new SchiffeVersenkenImpl();
        ArrayList<BattleshipsBoardPosition> inputBoardPositions = inputShipPositions(numberOfShipPlaces);
        SVProtocolEngine svProtocolEngine = new SVProtocolEngine(sv, userName);
        sv.setProtocolEngine(svProtocolEngine);
        // input shipPositions
        svProtocolEngine.handleConnection(tcpConnection.getInputStream(), tcpConnection.getOutputStream());
        //share ship positions
        sv.placeShips(userName, inputBoardPositions);
        ArrayList<BattleshipsBoardPosition> placePositionsResult = svProtocolEngine.placeShips(userName, inputBoardPositions);


        //todo
        //pre gameloop
        printArray(gameState.getGameState());
        String returnValue = "F";

        //gameloop
        while(!returnValue.equals("W")){
            //for user
            //todo
            if(isActivePlayer == true){
                BattleshipsBoardPosition attackPosition = getAttackPositionInput();
                String returnValueSynchronTest = sv.attackPos(userName, attackPosition);
                returnValue = svProtocolEngine.attackPos(userName, attackPosition);
                if(!returnValueSynchronTest.equals(returnValue)){
                    throw new GameException("out of sync!");
                }

            }
            if(returnValue.equals("W")){
                if(isActivePlayer == true) {
                    System.out.println("You won the game!");
                } else {
                    System.out.println("You loose the game!");
                }

            }
            swapActivePlayer(isActivePlayer);
        }



    }

    private static void printArray(int[][] array){
        for (int[] i: array) {
            for(int j: i){
                System.out.print(j + " ");
            }
            System.out.println("");
        }
        System.out.println("");
    }
//todo überarbeiten



    private static String changePlayer(String player){
        if(player == player){
            return "";
        } else{
            return "";
        }
    }

    private static BattleshipsBoardPosition getAttackPositionInput(){
        System.out.println("Please Enter the Position of your next attack!");
        Scanner scanner = new Scanner(System.in);
        String input = scanner.nextLine();
        BattleshipsBoardPosition position = new BattleshipsBoardPosition(input);
        return position;
    }

   private static boolean swapActivePlayer(boolean isActive){
        if(isActive) return false;
        return true;
   }

    private static ArrayList<BattleshipsBoardPosition> inputShipPositions(int numberOfShipPlaces){
        // input shipPositions
        System.out.println("Please Enter " + numberOfShipPlaces + " positions for your ships. Bsb.: A0 B1 C5 ..."); //todo give information about borders
        ArrayList<BattleshipsBoardPosition> inputBoardPositions = new ArrayList<>();
        Scanner scanner = new Scanner(System.in);
        String inputShipPositions = null;
        inputShipPositions = scanner.nextLine();
        //String inputShipPositions = "A1 A0 A3 B4 C5 B0 D1 D2";
        String[] inputStringArray = inputShipPositions.split(" ");
        for (String input: inputStringArray) {
            inputBoardPositions.add(new BattleshipsBoardPosition(input));
        }
        return inputBoardPositions;
    }

    private static String inputUsername(){
        System.out.println("Please enter player name!");
        Scanner scanner = new Scanner(System.in);
        String userName = scanner.nextLine();
        return userName;
    }
}
