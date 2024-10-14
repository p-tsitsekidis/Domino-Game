package gr.uop;

import javafx.application.Platform;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextInputDialog;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Scanner;
import java.util.function.Consumer;

/**
 * The InitializationScreen class handles the UI and server connection setup
 * for the initialization phase of the domino game. It allows players to input
 * their names and connects to the server.
 */
public class InitializationScreen {

    private static final int PORT = 7777;

    private Socket socket;
    private PrintWriter toServer;
    private Scanner fromServer;
    private String serverAddress;

    private String playerName;
    private String opponentName;
    private String data;

    private Stage primaryStage;
    private Button startButton;
    private Label statusLabel;

    private Map<String, Runnable> initCommands = new HashMap<>();

    private Consumer<InitializationResult> onInitializationComplete; // Used to pass data to JavaFXClient.java

    /**
     * Constructor that takes the primary stage and a callback function to signal
     * when initialization is complete.
     *
     * @param primaryStage             The main stage for the JavaFX application.
     * @param onInitializationComplete A callback function to pass initialization data.
     */
    public InitializationScreen(Stage primaryStage, Consumer<InitializationResult> onInitializationComplete) {
        this.primaryStage = primaryStage;
        this.onInitializationComplete = onInitializationComplete;
        initializeCommandMaps();
        setupUI();
    }

    /**
     * Initializes a map of commands for the initialization phase of the game.
     * These commands are received from the server.
     */
    private void initializeCommandMaps() {
        initCommands.put("WAIT_CONNECT", this::handleWaitConnect);
        initCommands.put("CONNECTED", this::handlePlayerConnected);
        initCommands.put("WAIT_PLAYER1_NAME", this::handlePlayer1Name);
        initCommands.put("WAIT_PLAYER2_NAME", this::handlePlayer2Name);
        initCommands.put("NAME_REQUEST", this::handleNameRequest);
        initCommands.put("END_INIT", () -> handleEndInit(data));
    }

    /**
     * Sets up the UI for the initialization screen, including the welcome message,
     * status label, and start button.
     */
    private void setupUI() {
        BorderPane main = new BorderPane();

        Label welcomeLabel = new Label("Welcome to the Domino Game!");
        welcomeLabel.setStyle("-fx-font-size: 20px; -fx-padding: 20px;");

        statusLabel = new Label("Press start to begin...");

        startButton = new Button("Start Game");
        startButton.setOnAction(event -> startGame());

        VBox vbox = new VBox(20, welcomeLabel, startButton, statusLabel);
        vbox.setStyle("-fx-alignment: center; -fx-padding: 50px;");
        main.setCenter(vbox);

        Scene scene = new Scene(main, 600, 400);
        primaryStage.setTitle("Domino Game - Initialization");
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    /**
     * Starts the game initialization process, prompting the user to enter the server IP
     * and attempting to connect to the server.
     */
    private void startGame() {
        // IP Address Input
        TextInputDialog ipDialog = new TextInputDialog("localhost");
        ipDialog.setTitle("Enter Server IP");
        ipDialog.setHeaderText("Enter the IP address of the server:");
        ipDialog.setContentText("IP Address:");

        Optional<String> ipResult = ipDialog.showAndWait();
        ipResult.ifPresent(ip -> serverAddress = ip);

        if (serverAddress == null || serverAddress.isEmpty()) {
            showErrorMessage("Invalid IP address. Please try again.");
            return;
        }
        startButton.setDisable(true);

        // Start the client connection in a separate thread
        new Thread(this::startClient).start();
    }

    /**
     * Establishes a connection to the server and starts listening for server messages.
     */
    private void startClient() {
        try {
            socket = new Socket(serverAddress, PORT);
            toServer = new PrintWriter(socket.getOutputStream(), true);
            fromServer = new Scanner(socket.getInputStream());

            // Confirm connection
            Platform.runLater(() -> updateStatus("Connected to server. Waiting for Player 2..."));

            handleServerMessages();

        } catch (IOException e) {
            Platform.runLater(() -> showErrorMessage("Could not connect to the server. Please try again."));
        }
    }

    /**
     * Handles incoming messages from the server and processes them accordingly.
     */
    private void handleServerMessages() {
        while (fromServer.hasNextLine()) {
            String serverMessage = fromServer.nextLine();

            if (serverMessage.contains(" ")) {
                data = serverMessage.substring(serverMessage.indexOf(" ") + 1);
                serverMessage = serverMessage.substring(0, serverMessage.indexOf(" "));
            }

            final String finalServerMessage = serverMessage;

            Platform.runLater(() -> processServerMessage(finalServerMessage));

            if (serverMessage.equals("END_INIT")) {
                break;
            }
        }
    }

    /**
     * Processes a command received from the server.
     *
     * @param serverMessage The command sent by the server.
     */
    private void processServerMessage(String serverMessage) {
        // Look up the command in the initCommands map
        Runnable command = initCommands.get(serverMessage);
        if (command != null) {
            command.run();  // Execute the corresponding command
        }
    }

    /**
     * Updates the status label on the UI with the given message.
     *
     * @param message The message to display on the status label.
     */
    private void updateStatus(String message) {
        statusLabel.setText(message);
    }

    /**
     * Displays an error message in an alert dialog.
     *
     * @param message The error message to display.
     */
    private void showErrorMessage(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Error");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    // ------------------------------------------- HANDLES -------------------------------------------

    /**
     * Handle the "WAIT_CONNECT" command received from the server.
     */
    private void handleWaitConnect() {
        updateStatus("Waiting for Player 2 to connect...");
    }

    /**
     * Handle the "CONNECTED" command received from the server.
     */
    private void handlePlayerConnected() {
        updateStatus("Player 2 connected.");
    }

    /**
     * Handle the "WAIT_PLAYER1_NAME" command received from the server.
     */
    private void handlePlayer1Name() {
        updateStatus("Waiting for Player 1 to enter their name...");
    }

    /**
     * Handle the "WAIT_PLAYER2_NAME" command received from the server.
     */
    private void handlePlayer2Name() {
        updateStatus("Waiting for Player 2 to enter their name...");
    }

    /**
     * Handle the "NAME_REQUEST" command received from the server, prompting the player
     * to enter their name and sending it to the server.
     */
    private void handleNameRequest() {
        TextInputDialog nameDialog = new TextInputDialog();
        nameDialog.setTitle("Enter Player Name");
        nameDialog.setHeaderText("Enter your name:");
        nameDialog.setContentText("Name:");

        Optional<String> nameResult = nameDialog.showAndWait();
        nameResult.ifPresent(name -> this.playerName = name);

        if (playerName == null || playerName.isEmpty()) {
            showErrorMessage("Invalid name. Please try again.");
            return;
        }
        toServer.println(playerName);
    }

    /**
     * Handle the "END_INIT" command, completing the initialization and starting the game.
     *
     * @param data The opponent's name received from the server.
     */
    private void handleEndInit(String data) {
        this.opponentName = data;
        updateStatus("Hello " + this.playerName + "! Your opponent is: " + this.opponentName + "!\nThe game is starting...");

        // Notify JavaFXClient that the initialization is complete and pass necessary data
        if (onInitializationComplete != null) {
            onInitializationComplete.accept(new InitializationResult(playerName, opponentName, socket, toServer, fromServer));
        }
    }

    // Helper class to save initialization results

    /**
     * A helper class to store the result of the initialization phase.
     * Contains player names, socket connection, and communication streams.
     */
    public static class InitializationResult {
        public final String playerName;
        public final String opponentName;
        public final Socket socket;
        public final PrintWriter toServer;
        public final Scanner fromServer;

        /**
         * Constructor for InitializationResult.
         *
         * @param playerName   The name of the player.
         * @param opponentName The name of the opponent.
         * @param socket       The socket connection to the server.
         * @param toServer     The output stream to the server.
         * @param fromServer   The input stream from the server.
         */
        public InitializationResult(String playerName, String opponentName, Socket socket, PrintWriter toServer, Scanner fromServer) {
            this.playerName = playerName;
            this.opponentName = opponentName;
            this.socket = socket;
            this.toServer = toServer;
            this.fromServer = fromServer;
        }
    }
}