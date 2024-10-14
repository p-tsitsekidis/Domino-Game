package gr.uop;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.stage.Stage;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.Scanner;

/**
 * JavaFXClient is the main entry point for the JavaFX-based domino game client.
 * It handles the initialization and gameplay screens, and manages the connection
 * with the server.
 */
public class JavaFXClient extends Application {

    private Stage primaryStage;
    private Socket socket;
    private PrintWriter toServer;
    private Scanner fromServer;
    private String playerName;
    private String opponentName;

    /**
     * Starts the JavaFX application by displaying the initialization screen.
     *
     * @param primaryStage The primary stage for this application.
     */
    @Override
    public void start(Stage primaryStage) {
        this.primaryStage = primaryStage;
        showInitializationScreen();
    }

    /**
     * Displays the initialization screen where the player connects to the server
     * and enters their name.
     */
    private void showInitializationScreen() {
        // Pass a callback function that gets called when initialization is complete
        new InitializationScreen(primaryStage, (result) -> {
            // Save the results from initialization
            this.playerName = result.playerName;
            this.opponentName = result.opponentName;
            this.socket = result.socket;
            this.toServer = result.toServer;
            this.fromServer = result.fromServer;

            showGameplayScreen();
        });
    }

    /**
     * Displays the gameplay screen after the initialization is complete.
     * It continues the game from the same primary stage.
     */
    private void showGameplayScreen() {
        // Set up the gameplay screen using the same primary stage
        GameplayScreen gameScreen = new GameplayScreen(primaryStage, socket, playerName, opponentName, toServer, fromServer, this::shutdownGame);
        gameScreen.start();
    }

    /**
     * Shuts down the game and closes all connections. Exits the JavaFX application.
     */
    private void shutdownGame() {
        System.out.println("Shutting down game...");
        closeConnections();
        Platform.exit();
    }

    /**
     * Closes all network connections (socket, input, and output streams).
     */
    private void closeConnections() {
        try {
            if (toServer != null) toServer.close();
            if (fromServer != null) fromServer.close();
            if (socket != null) socket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * The main entry point for the client application. It launches the JavaFX application.
     *
     * @param args Command-line arguments.
     */
    public static void main(String[] args) {
        launch(args);
    }
}