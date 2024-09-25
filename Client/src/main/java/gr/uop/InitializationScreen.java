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

public class InitializationScreen {

    private static final int PORT = 7777;
    private Socket socket;
    private PrintWriter toServer;
    private Scanner fromServer;
    private String serverAddress;

    private String playerName;
    private String opponentName;
    private Label statusLabel;
    private String data;

    private Stage primaryStage;
    private Button startButton;

    private Map<String, Runnable> initCommands = new HashMap<>();

    private Consumer<InitializationResult> onInitializationComplete; // This is used to pass the data to JavaFXClient.java

    // Constructor takes the primaryStage for switching screens later
    public InitializationScreen(Stage primaryStage, Consumer<InitializationResult> onInitializationComplete) {
        this.primaryStage = primaryStage;
        this.onInitializationComplete = onInitializationComplete;
        initializeCommandMaps();
        setupUI();
    }

    private void initializeCommandMaps() {
        initCommands.put("WAIT_CONNECT", this::handleWaitConnect);
        initCommands.put("CONNECTED", this::handlePlayerConnected);
        initCommands.put("WAIT_PLAYER1_NAME", this::handlePlayer1Name);
        initCommands.put("WAIT_PLAYER2_NAME", this::handlePlayer2Name);
        initCommands.put("NAME_REQUEST", this::handleNameRequest);
        initCommands.put("END_INIT", () -> handleEndInit(playerName, data));
    }

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
        new Thread(() -> startClient()).start();
    }

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

    private void handleServerMessages() {
        while (fromServer.hasNextLine()) {
            String serverMessage = fromServer.nextLine();

            if(serverMessage.contains(" ")) {
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

    private void processServerMessage(String serverMessage) {
        // Look up the command in the initCommands map
        Runnable command = initCommands.get(serverMessage);
        if (command != null) {
            command.run();  // Execute the corresponding command
        }
    }

    private void updateStatus(String message) {
        statusLabel.setText(message);
    }

    private void showErrorMessage(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Error");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    // ------------------------------------------- HANDLES -------------------------------------------

    private void handleWaitConnect() {
       updateStatus("Waiting for Player 2 to connect...");
    }

    private void handlePlayerConnected() {
        updateStatus("Player 2 connected.");
    }

    private void handlePlayer1Name() {
        updateStatus("Waiting for Player 1 to enter their name...");
    }

    private void handlePlayer2Name() {
        updateStatus("Waiting for Player 2 to enter their name...");
    }

    private void handleNameRequest() {
        TextInputDialog nameDialog = new TextInputDialog();
        nameDialog.setTitle("Enter Player Name");
        nameDialog.setHeaderText("Enter your name:");
        nameDialog.setContentText("Name:");

        Optional<String> nameResult = nameDialog.showAndWait();
        nameResult.ifPresent(name -> playerName = name);

        if (playerName == null || playerName.isEmpty()) {
            showErrorMessage("Invalid name. Please try again.");
            return;
        }
        toServer.println(playerName);
    }

    private void handleEndInit(String playerName, String data) {
        opponentName = data;
        updateStatus("Hello " + playerName + "! Your opponent is: " + opponentName + "!\nThe game is starting...");

        // Notify JavaFXClient that the initialization is complete and pass necessary data
        if (onInitializationComplete != null) {
            onInitializationComplete.accept(new InitializationResult(playerName, opponentName, socket, toServer, fromServer));
        }
    }

    // Helper class to save initialization results
    public static class InitializationResult {
        public final String playerName;
        public final String opponentName;
        public final Socket socket;
        public final PrintWriter toServer;
        public final Scanner fromServer;

        public InitializationResult(String playerName, String opponentName, Socket socket, PrintWriter toServer, Scanner fromServer) {
            this.playerName = playerName;
            this.opponentName = opponentName;
            this.socket = socket;
            this.toServer = toServer;
            this.fromServer = fromServer;
        }
    }
}
