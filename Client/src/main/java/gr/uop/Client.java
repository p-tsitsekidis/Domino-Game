package gr.uop;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.Scanner;

public class Client {

    private static final int PORT = 7777;
    private Socket socket;
    private PrintWriter toServer;
    private Scanner fromServer;
    private Scanner userInput;

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

            handleGameLoop();

        } catch (IOException e) {
            System.err.println("Could not connect to the server: " + e.getMessage());
        } finally {
            closeConnections();
        }
    }

    private void handleGameLoop() {
        while (fromServer.hasNextLine()) {
            String serverMessage = fromServer.nextLine();
            System.out.println("Server: " + serverMessage);

            //Send name to the sever
            if (serverMessage.contains("Please enter your name")) {
                String playerName = userInput.nextLine();
                toServer.println(playerName);
            }

            //Client move
            if (serverMessage.contains("Enter the index of the tile")) {
                String move = userInput.nextLine();
                toServer.println(move);
            }

            //Game over
            if (serverMessage.contains("scored")) {
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
}
