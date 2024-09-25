package gr.uop;

import javafx.application.Application;
import javafx.stage.Stage;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.Scanner;


public class JavaFXClient extends Application {

    private Stage primaryStage;
    private Socket socket;
    private PrintWriter toServer;
    private Scanner fromServer;
    private String playerName;
    private String opponentName;

    @Override
    public void start(Stage primaryStage) {
        this.primaryStage = primaryStage;
        showInitializationScreen();
    }

    private void showInitializationScreen() {
        // Pass a callback function that gets called when initialization is complete
        new InitializationScreen(primaryStage, (result) -> {
            System.out.println("Initialization complete, transitioning to gameplay screen..."); // Debugging
            // Save the results from initialization
            this.playerName = result.playerName;
            this.opponentName = result.opponentName;
            this.socket = result.socket;
            this.toServer = result.toServer;
            this.fromServer = result.fromServer;

            // Now transition to the gameplay screen in the same stage
            showGameplayScreen();
        });
    }

    private void showGameplayScreen() {
        System.out.println("Transitioning to gameplay screen..."); // Debugging
        // Set up the gameplay screen using the same primary stage
        GameplayScreen gameScreen = new GameplayScreen(primaryStage, socket, playerName, opponentName, toServer, fromServer);
        gameScreen.start();
    }

    public static void main(String[] args) {
        launch(args);
    }
}

