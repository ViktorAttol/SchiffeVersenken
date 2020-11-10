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
        System.out.println("Please enter player name!");
        Scanner initializingScanner = new Scanner(System.in);
        String userName = initializingScanner.nextLine();
        String enemyUserName = null;
        int startNumber = 0;
        int numberOfShipPlaces = 8;
        GameState gameState = null;

        //todo find better solution
        //server
        if(args.length == 3){
            port = Integer.parseInt(args[0]);
            startNumber = Integer.parseInt(args[1]);
            System.out.println("Please enter board size!");
            dimX = Integer.parseInt(initializingScanner.nextLine());

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
        if(args.length == 2){
            hostname = args[0];
            port = Integer.parseInt(args[1]);
            startNumber = Integer.parseInt(args[2]);
            Client client = new TCPConnector();
            tcpConnection = client.connect(hostname, port);
            GameDataReceiver gameDataReceiver = new GameDataExchanger();
            gameState = gameDataReceiver.receiveGameLayout(tcpConnection.getInputStream());
            dimX = gameState.getGameLayoutHeight();
        }
        initializingScanner.close();

        //initialize schiffeversenken und serealization
        SchiffeVersenken sv = new SchiffeVersenkenImpl();
        SchiffeVersenken svProtocolEngine = new SVProtocolEngine(tcpConnection.getInputStream(), tcpConnection.getOutputStream(), sv);

        // input shipPositions
        ArrayList<BattleshipsBoardPosition> inputBoardPositions = inputShipPositions(numberOfShipPlaces);

        //share ship positions
        sv.placeShips(userName, inputBoardPositions);
        svProtocolEngine.placeShips(userName, inputBoardPositions);
        //svProtocolEngine.read();



        //pre gameloop
        printArray(gameState.getGameState());
        String currentPlayer = null;
        int returnValue = 0;    //0 no winner and game continues, 1 game is won, 2 game over with draw, -1 for false input
        int round = 0;
        Scanner scanner = new Scanner(System.in);

        //gameloop
        while(returnValue == 0 || returnValue == -1){
            //for user
            if(currentPlayer == userName){
                String input;
                do {
                    input = scanner.nextLine();
                    returnValue = 1;
//                    returnValue = turn.setInput(input, currentPlayer);
                    if(returnValue == -1)System.out.println("Invalid input!");
                }while(returnValue == -1);
                GameData gameData = new GameDataImpl(input,userName);
                GameDataSender gameDataSender = new GameDataExchanger();
                gameDataSender.sendGameData(gameData, tcpConnection.getOutputStream());
            //for opponent
            } else if(currentPlayer != userName){
                GameDataReceiver gameDataReceiver = new GameDataExchanger();
                GameData gameData = gameDataReceiver.receiveGameData(tcpConnection.getInputStream());
//                returnValue = turn.setInput(gameData.getInputIndex(), currentPlayer);
                returnValue = 1;

            }

            printArray(gameState.getGameState());

            if(returnValue == 1) System.out.println("Player " + currentPlayer + " winns the game!");
            if(returnValue == 2) System.out.println("Game over without winner O_o...");

            currentPlayer = changePlayer(currentPlayer);
        }


        scanner.close();
        tcpConnection.getSocket().close();
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

    private static ArrayList<BattleshipsBoardPosition> inputShipPositions(int numberOfShipPlaces){
        // input shipPositions
        System.out.println("Please Enter " + numberOfShipPlaces + " positions for your ships. Bsb.: A0 B1 C5 ..."); //todo give information about borders
        ArrayList<BattleshipsBoardPosition> inputBoardPositions = new ArrayList<>();
        Scanner scanner = new Scanner(System.in);
        String inputShipPositions = scanner.nextLine();
        String[] inputStringArray = inputShipPositions.split(" ");
        for (String input: inputStringArray) {
            inputBoardPositions.add(new BattleshipsBoardPosition(input));
        }
        scanner.close();
        return inputBoardPositions;
    }
}
