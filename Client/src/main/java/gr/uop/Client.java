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
    private String playerName;

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
        initCommands.put("END_INIT", () -> handleEndInit(playerName));

        // Gameplay phase
    }

    private void handleInitialization() {
        while (fromServer.hasNextLine()) {
            String serverMessage = fromServer.nextLine();

            // Look up the command in the initCommands map
            Runnable command = initCommands.get(serverMessage);
            if (command != null) {
                command.run();  // Execute the corresponding command
            }

            // Break if needed after game start or other conditions
        }
    }

    private void handleGameLoop() {

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

    private void handleEndInit(String playerName) {
        System.out.println("Hello " + playerName + "! The game is starting...");
    }
}
