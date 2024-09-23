package gr.uop;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;

public class Client {

    private static final int PORT = 7777;
    private Socket socket;
    private PrintWriter toServer;
    private Scanner fromServer;
    private Scanner userInput;

    // Variables needed to store information from the server
    private String playerName;
    private String opponentName;
    private String winner;
    private String tiles;
    private String lineOfPlay;
    private String tile;
    private String score;
    private String index;

    private String data; // Any data received from the server

    // HashMaps for command handling
    private Map<String, Runnable> initCommands = new HashMap<>();
    private Map<String, Runnable> gameCommands = new HashMap<>();

    public static void main(String[] args) {
        new Client().startClient();
    }

    private void startClient() {
        userInput = new Scanner(System.in);

        System.out.print("Enter the server IP address: ");
        String serverAddress = userInput.nextLine();

        try {
            //Connection
            socket = new Socket(serverAddress, PORT);
            System.out.println("Connected to the server at " + serverAddress);

            //Input-Output streams
            toServer = new PrintWriter(socket.getOutputStream(), true);
            fromServer = new Scanner(socket.getInputStream());

            initializeCommandMaps();
            handleInitialization();
            handleGameLoop();

        } catch (IOException e) {
            System.err.println("Could not connect to the server: " + e.getMessage());
        } finally {
            closeConnections();
        }
    }

    private void initializeCommandMaps() {
        // Initialization phase
        initCommands.put("WAIT_CONNECT", this::handleWaitConnect);
        initCommands.put("CONNECTED", this::handlePlayerConnected);
        initCommands.put("WAIT_PLAYER1_NAME", this::handlePlayer1Name);
        initCommands.put("WAIT_PLAYER2_NAME", this::handlePlayer2Name);
        initCommands.put("NAME_REQUEST", this::handleNameRequest);
        initCommands.put("END_INIT", () -> handleEndInit(playerName, data));

        // Gameplay phase
        gameCommands.put("TURN", () -> handleTurn(playerName));
        gameCommands.put("TILES", () -> handleTiles(data));
        gameCommands.put("BOARD", () -> handleLineOfPlay(data));
        gameCommands.put("WAIT_OPPONENT_MOVE", this::handleWaitForMove);
        gameCommands.put("NO_AVAILABLE_MOVES", this::handleNoAvailableMoves);
        gameCommands.put("DRAW", () -> handleDraw(data));
        gameCommands.put("OPPONENT_DRAW", this::handleOppDraw);
        gameCommands.put("PLAYED", () -> handlePlayed(data));
        gameCommands.put("OPP_PLAYED", () -> handleOppPlayed(data));
        gameCommands.put("PASS", this::handlePass);
        gameCommands.put("OPP_PASS", this::handleOppPass);
        gameCommands.put("INDEX", this::handleIndex);
        gameCommands.put("INVALID_MOVE", this::handleInvalidMove);
        gameCommands.put("INVALID_INPUT", this::handleInvalidInput);
        gameCommands.put("GAME_OVER", () -> handleGameOver(data));
        gameCommands.put("SCORE", () -> handleScore(data));
    }

    private void handleInitialization() {
        while (fromServer.hasNextLine()) {
            String serverMessage = fromServer.nextLine();

            if(serverMessage.contains(" ")) {
                data = serverMessage.substring(serverMessage.indexOf(" ") + 1);
                serverMessage = serverMessage.substring(0, serverMessage.indexOf(" "));
            }

            // Look up the command in the initCommands map
            Runnable command = initCommands.get(serverMessage);
            if (command != null) {
                command.run();  // Execute the corresponding command
            }

            if (serverMessage.equals("END_INIT")) {
                break;
            }
        }
    }

    private void handleGameLoop() {
        while (fromServer.hasNextLine()) {
            String serverMessage = fromServer.nextLine();

            if(serverMessage.contains(" ")) {
                data = serverMessage.substring(serverMessage.indexOf(" ") + 1);
                serverMessage = serverMessage.substring(0, serverMessage.indexOf(" "));
            }

            // Look up the command in the gameCommands map
            Runnable command = gameCommands.get(serverMessage);
            if (command != null) {
                command.run();
            }

            if (serverMessage.equals("SCORE")) {
                break;
            }
        }
    }

    private void closeConnections() {
        try {
            if (toServer != null) toServer.close();
            if (fromServer != null) fromServer.close();
            if (socket != null) socket.close();
            if (userInput != null) userInput.close();
        } catch (IOException e) {
            System.err.println("Error closing connections: " + e.getMessage());
        }
    }

    // ------------------------------------ SERVER HANDLES -------------------------------------------
    // ------------------------------------ INIT HANDLES -------------------------------------------
    private void handleWaitConnect() {
        System.out.println("Waiting for Player 2 to connect...");
    }

    private void handlePlayerConnected() {
        System.out.println("Player 2 connected.");
    }

    private void handlePlayer1Name() {
        System.out.println("Waiting for Player 1 to enter their name...");
    }

    private void handlePlayer2Name() {
        System.out.println("Waiting for Player 2 to enter their name...");
    }

    private void handleNameRequest() {
        System.out.println("Please enter your name: ");
        playerName = userInput.nextLine();
        toServer.println(playerName);
    }

    private void handleEndInit(String playerName, String data) {
        opponentName = data;
        System.out.println("Hello " + playerName + "!");
        System.out.println("Your opponent is: " + opponentName + "!");
        System.out.println("The game is starting...");
    }

    // ------------------------------------ GAMEPLAY HANDLES -------------------------------------------
    private void handleTurn(String playerName) {
        System.out.println("It's your turn " + playerName + "!");
    }

    private void handleTiles(String data) {
        tiles = data;
        System.out.println("Your tiles: " + tiles);
    }

    private void handleLineOfPlay(String data) {
        lineOfPlay = data;
        System.out.println("Current Board: " + lineOfPlay);
    }

    private void handleWaitForMove() {
        System.out.println("Waiting for " + opponentName + " to make a move.");
    }

    private void handleNoAvailableMoves() {
        System.out.println("No available moves. Drawing from stock...");
    }

    private void handleDraw(String data) {
        tile = data;
        System.out.println("You drew: " + tile);
    }

    private void handleOppDraw() {
        System.out.println(opponentName + " drew a tile.");
    }

    private void handlePlayed(String data) {
        tile = data;
        System.out.println("You played: " + tile);
    }

    private void handleOppPlayed(String data) {
        tile = data;
        System.out.println(opponentName + " played: " + tile);
    }

    private void handlePass() {
        System.out.println("No valid tiles to play and no more tiles in the stock. Passing turn.");
    }

    private void handleOppPass() {
        System.out.println(opponentName + " has no valid tiles to play and the stock is empty.");
        System.out.println(opponentName + " passed the turn.");
    }

    private void handleIndex() {
        System.out.println("Enter the index of the tile you want to play: ");
        index = userInput.nextLine();
        toServer.println(index);
    }

    private void handleInvalidMove() {
        System.out.println("Invalid move. Choose a different tile.");
    }

    private void handleInvalidInput() {
        System.out.println("Invalid input or tile index. Try again.");
    }

    private void handleGameOver(String data) {
        winner = data;
        System.out.println("\nGame over! The winner is: " + winner + "!");
    }

    private void handleScore(String data) {
        score = data;
        System.out.println("You scored: " + score + " points!");
    }
}
