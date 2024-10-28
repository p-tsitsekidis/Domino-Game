package gr.uop;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;

/**
 * The Client class represents the networked client that communicates with the DominoServer.
 * It handles receiving game state information from the server and sending user input back to the server.
 */
public class CommandLineClient {

    private static final int PORT = 7777; // The port used for communication with the server
    private Socket socket;
    private PrintWriter toServer;
    private Scanner fromServer;
    private Scanner userInput;

    // Variables to store information received from the server
    private String playerName;
    private String opponentName;
    private String winner;
    private String tiles;
    private String lineOfPlay;
    private String tile;
    private String score;
    private String index;
    private String stockSize;
    private String opponentTiles;

    private String data; // Holds temporary data received from the server

    // HashMaps to store command mappings for initialization and game phases
    private Map<String, Runnable> initCommands = new HashMap<>();
    private Map<String, Runnable> gameCommands = new HashMap<>();

    /**
     * The main method to start the Client application.
     * 
     * @param args Command-line arguments (not used).
     */
    public static void main(String[] args) {
        new CommandLineClient().startClient();
    }

    /**
     * Starts the client, establishes a connection to the server, and manages communication.
     */
    private void startClient() {
        userInput = new Scanner(System.in);

        System.out.print("Enter the server IP address: ");
        String serverAddress = userInput.nextLine();

        // Connection
        try {
            socket = new Socket(serverAddress, PORT);
            System.out.println("Connected to the server at " + serverAddress);

            toServer = new PrintWriter(socket.getOutputStream(), true);
            fromServer = new Scanner(socket.getInputStream());

            // Initialize command maps and begin game loop
            initializeCommandMaps();
            handleInitialization();
            handleGameLoop();

        } catch (IOException e) {
            System.err.println("Could not connect to the server: " + e.getMessage());
        } finally {
            closeConnections();
        }
    }

    /**
     * Initializes the command maps for both the initialization and gameplay phases.
     */
    private void initializeCommandMaps() {
        // Initialization commands
        initCommands.put("WAIT_CONNECT", this::handleWaitConnect);
        initCommands.put("CONNECTED", this::handlePlayerConnected);
        initCommands.put("WAIT_PLAYER1_NAME", this::handlePlayer1Name);
        initCommands.put("WAIT_PLAYER2_NAME", this::handlePlayer2Name);
        initCommands.put("NAME_REQUEST", this::handleNameRequest);
        initCommands.put("END_INIT", () -> handleEndInit(data));

        // Gameplay commands
        gameCommands.put("TURN", this::handleTurn);
        gameCommands.put("TILES", () -> handleTiles(data));
        gameCommands.put("BOARD", () -> handleLineOfPlay(data));
        gameCommands.put("STOCK_SIZE", () -> handleStock(data));
        gameCommands.put("OPPONENT_TILE_SIZE", () -> handleTileSize(data));
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

    /**
     * Handles the initialization phase of the game, processing initial server messages.
     */
    private void handleInitialization() {
        while (fromServer.hasNextLine()) {
            String serverMessage = fromServer.nextLine();

            if(serverMessage.contains(" ")) {
                data = serverMessage.substring(serverMessage.indexOf(" ") + 1);
                serverMessage = serverMessage.substring(0, serverMessage.indexOf(" "));
            }

            // Look up and execute the initialization command
            Runnable command = initCommands.get(serverMessage);
            if (command != null) {
                command.run();
            }

            if (serverMessage.equals("END_INIT")) {
                break;
            }
        }
    }

    /**
     * Handles the main game loop, processing gameplay commands from the server.
     */
    private void handleGameLoop() {
        while (fromServer.hasNextLine()) {
            String serverMessage = fromServer.nextLine();

            if(serverMessage.contains(" ")) {
                data = serverMessage.substring(serverMessage.indexOf(" ") + 1);
                serverMessage = serverMessage.substring(0, serverMessage.indexOf(" "));
            }

            // Look up and execute the gameplay command
            Runnable command = gameCommands.get(serverMessage);
            if (command != null) {
                command.run();
            }

            if (serverMessage.equals("SCORE")) {
                break;
            }
        }
    }

    /**
     * Closes the connection and streams after the game ends.
     */
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
    /**
     * Handles the "WAIT_CONNECT" message from the server during initialization.
     * Notifies the player that they are waiting for Player 2 to connect.
     */
    private void handleWaitConnect() {
        System.out.println("Waiting for Player 2 to connect...");
    }

    /**
     * Handles the "CONNECTED" message from the server, indicating that Player 2 has connected.
     */
    private void handlePlayerConnected() {
        System.out.println("Player 2 connected.");
    }

    /**
     * Handles the message to wait for Player 1 to enter their name.
     */
    private void handlePlayer1Name() {
        System.out.println("Waiting for Player 1 to enter their name...");
    }

    /**
     * Handles the message to wait for Player 2 to enter their name.
     */
    private void handlePlayer2Name() {
        System.out.println("Waiting for Player 2 to enter their name...");
    }

    /**
     * Handles the request for the player to enter their name and sends it to the server.
     */
    private void handleNameRequest() {
        System.out.println("Please enter your name: ");
        this.playerName = userInput.nextLine();
        toServer.println(playerName);
    }

    /**
     * Handles the end of the initialization phase by setting the opponent's name.
     * 
     * @param data The name of the opponent.
     */
    private void handleEndInit(String data) {
        this.opponentName = data;
        System.out.println("Hello " + playerName + "!");
        System.out.println("Your opponent is: " + opponentName + "!");
        System.out.println("The game is starting...");
    }

    // ------------------------------------ GAMEPLAY HANDLES -------------------------------------------
    /**
     * Handles the message indicating it's the player's turn.
     */
    private void handleTurn() {
        System.out.println("It's your turn " + playerName + "!");
    }

    /**
     * Handles the message for the current stock size and displays it.
     * 
     * @param data The stock size.
     */
    private void handleStock(String data) {
        this.stockSize = data;
        System.out.println("The stock has " + this.stockSize + " tiles.");
    }

    /**
     * Handles the message for the opponent's tile count.
     * 
     * @param data The number of tiles the opponent has.
     */
    private void handleTileSize(String data) {
        this.opponentTiles = data;
        System.out.println("Your opponent has " + this.opponentTiles + " tiles.");
    }

    /**
     * Handles the message containing the player's tiles and displays them.
     * 
     * @param data The tiles in the player's hand.
     */
    private void handleTiles(String data) {
        this.tiles = data;
        System.out.println("Your tiles: " + this.tiles);
    }

    /**
     * Handles the message containing the current board state and displays it.
     * 
     * @param data The current board state.
     */
    private void handleLineOfPlay(String data) {
        this.lineOfPlay = data;
        System.out.println("Current Board: " + this.lineOfPlay);
    }

    /**
     * Handles the message that the opponent is making a move.
     */
    private void handleWaitForMove() {
        System.out.println("Waiting for " + this.opponentName + " to make a move.");
    }

    /**
     * Handles the message indicating that no available moves are left and a tile will be drawn from the stock.
     */
    private void handleNoAvailableMoves() {
        System.out.println("No available moves. Drawing from stock...");
    }

    /**
     * Handles the message that the player drew a tile.
     * 
     * @param data The tile drawn by the player.
     */
    private void handleDraw(String data) {
        this.tile = data;
        System.out.println("You drew: " + this.tile);
    }

    /**
     * Handles the message that the opponent drew a tile.
     */
    private void handleOppDraw() {
        System.out.println(this.opponentName + " drew a tile.");
    }

    /**
     * Handles the message that the player played a tile.
     * 
     * @param data The tile played by the player.
     */
    private void handlePlayed(String data) {
        this.tile = data;
        System.out.println("You played: " + this.tile);
    }

    /**
     * Handles the message that the opponent played a tile.
     * 
     * @param data The tile played by the opponent.
     */
    private void handleOppPlayed(String data) {
        this.tile = data;
        System.out.println(opponentName + " played: " + this.tile);
    }

    /**
     * Handles the message that the player passed their turn.
     */
    private void handlePass() {
        System.out.println("No valid tiles to play and no more tiles in the stock. Passing turn.");
    }

    /**
     * Handles the message that the opponent passed their turn.
     */
    private void handleOppPass() {
        System.out.println(this.opponentName + " has no valid tiles to play and the stock is empty.");
        System.out.println(this.opponentName + " passed the turn.");
    }

    /**
     * Handles the message requesting the player to provide the index of the tile to play.
     */
    private void handleIndex() {
        System.out.println("Enter the index of the tile you want to play: ");
        this.index = userInput.nextLine();
        toServer.println(this.index);
    }

    /**
     * Handles the message indicating an invalid move was made.
     */
    private void handleInvalidMove() {
        System.out.println("Invalid move. Choose a different tile.");
    }

    /**
     * Handles the message indicating invalid input or an incorrect tile index.
     */
    private void handleInvalidInput() {
        System.out.println("Invalid input or tile index. Try again.");
    }

    /**
     * Handles the game over message and announces the winner.
     * 
     * @param data The name of the winning player.
     */
    private void handleGameOver(String data) {
        this.winner = data;
        System.out.println("\nGame over! The winner is: " + this.winner + "!");
    }

    /**
     * Handles the message displaying the final score.
     * 
     * @param data The player's score.
     */
    private void handleScore(String data) {
        this.score = data;
        System.out.println("You scored: " + this.score + " points!");
    }
}