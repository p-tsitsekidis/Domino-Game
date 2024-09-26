package gr.uop;

import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;

public class GameplayScreen {

    // Network communication
    private Socket socket;
    private PrintWriter toServer;
    private Scanner fromServer;

    // Information from the server
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
    private Map<String, Runnable> gameCommands = new HashMap<>();

    private Stage primaryStage; // Primary stage
    private Runnable onGameShutdown; // Signals the end of the game

    // JavaFX items
    Label gameStatusLabel;

    public GameplayScreen(Stage primaryStage, Socket socket, String playerName, String opponentName, PrintWriter toServer, Scanner fromServer, Runnable onGameShutdown) {
        this.primaryStage = primaryStage;
        this.socket = socket;
        this.playerName = playerName;
        this.opponentName = opponentName;
        this.toServer = toServer;
        this.fromServer = fromServer;
        this.onGameShutdown = onGameShutdown;
    }

    public void start() {
        initializeCommandMaps();
        setupUI();
        new Thread(this::handleGameLoop).start();  // Start listening for game messages in a new thread
    }

    private void initializeCommandMaps() {
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

    private void setupUI() {
        BorderPane gameLayout = new BorderPane();
        gameStatusLabel = new Label("Waiting for opponent's move...");
        gameLayout.setTop(gameStatusLabel);
    
        Scene gameScene = new Scene(gameLayout, 800, 600);
    
        // Switch the scene on the same primary stage
        primaryStage.setScene(gameScene);
        primaryStage.setTitle("Domino Game - Playing");
        primaryStage.show();
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

    // ------------------------------------------ GAMEPLAY HANDLES ---------------------------------------
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
        // index = userInput.nextLine();
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
        onGameShutdown.run();
    }
}
