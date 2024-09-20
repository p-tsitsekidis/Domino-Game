package gr.uop;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextInputDialog;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.Optional;
import java.util.Scanner;

public class JavaFXClient extends Application {

    private static final int PORT = 7777;
    private Socket socket;
    private PrintWriter toServer;
    private Scanner fromServer;

    private String playerName;
    private String serverAddress;

    private Label statusLabel;
    private Button startButton;
    private Button cancelButton;

    private Stage primaryStage;  // Hold the reference to the primary stage (for closing it later)
    private Stage gameStage;     // Separate stage for the game screen

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) {
        this.primaryStage = primaryStage;  // Store the primary stage reference
        setupUI(primaryStage);
    }

    private void setupUI(Stage primaryStage) {
        BorderPane main = new BorderPane();

        // Welcome message
        Label welcomeLabel = new Label("Welcome to the Domino Game!");
        welcomeLabel.setStyle("-fx-font-size: 20px; -fx-padding: 20px;");

        // Status message
        statusLabel = new Label("Press start to begin...");

        // Start button
        startButton = new Button("Start Game");
        startButton.setOnAction(event -> startGame());

        // Cancel button (for canceling the waiting process)
        cancelButton = new Button("Cancel");
        cancelButton.setVisible(false);  // Initially hidden
        cancelButton.setOnAction(event -> cancelConnection());

        VBox vbox = new VBox(20, welcomeLabel, startButton, cancelButton, statusLabel);
        vbox.setStyle("-fx-alignment: center; -fx-padding: 50px;");

        main.setCenter(vbox);

        Scene scene = new Scene(main, 600, 400);
        primaryStage.setTitle("Domino Game");
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    private void startGame() {
        // IP Address
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

        // Player's name
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

        // Start Connection
        new Thread(this::startClient).start();
    }

    private void startClient() {
        try {
            socket = new Socket(serverAddress, PORT);
            toServer = new PrintWriter(socket.getOutputStream(), true);
            fromServer = new Scanner(socket.getInputStream());

            toServer.println(playerName);

            // Updates happen on JavaFX Application Thread
            Platform.runLater(() -> updateStatus("Connected to server. Waiting for Player 2..."));

            // Show the cancel button while waiting
            Platform.runLater(() -> {
                cancelButton.setVisible(true);
                startButton.setDisable(true);  // Disable the start button during connection
            });

            // Handle server messages
            handleServerMessages();

        } catch (IOException e) {
            Platform.runLater(() -> showErrorMessage("Could not connect to the server. Please try again."));
        }
    }

    private void handleServerMessages() {
        while (fromServer.hasNextLine()) {
            String serverMessage = fromServer.nextLine();
            Platform.runLater(() -> processServerMessage(serverMessage));
        }
    }

    private void processServerMessage(String serverMessage) {
        if (serverMessage.contains("Waiting for Player 2")) {
            updateStatus("Waiting for Player 2 to join...");
        } else if (serverMessage.contains("Hello")) {
            updateStatus("Game is starting...");

            // Hide the cancel button, close the current screen and start the game screen
            Platform.runLater(() -> {
                cancelButton.setVisible(false);
                closeInitializationScreen();
                startGameScreen();
            });
        }
    }

    private void cancelConnection() {
        updateStatus("Connection canceled. You can restart the game.");
        closeConnections();
        Platform.runLater(() -> {
            startButton.setDisable(false);  // Re-enable start button for a new game
            cancelButton.setVisible(false);  // Hide cancel button
        });
    }

    private void updateStatus(String message) {
        Platform.runLater(() -> statusLabel.setText(message));
    }

    private void showErrorMessage(String message) {
        Platform.runLater(() -> {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Error");
            alert.setHeaderText(null);
            alert.setContentText(message);
            alert.showAndWait();
        });
    }

    private void closeConnections() {
        try {
            if (toServer != null) toServer.close();
            if (fromServer != null) fromServer.close();
            if (socket != null) socket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // Close the initial connection screen
    private void closeInitializationScreen() {
        primaryStage.close();  // Close the primary initialization screen
    }

    private void startGameScreen() {
        BorderPane gameLayout = new BorderPane();

        Scene gameScene = new Scene(gameLayout, 800, 600);

        gameStage = new Stage();
        gameStage.setTitle("Domino Game - Playing");
        gameStage.setScene(gameScene);
        gameStage.show();
    }
}
