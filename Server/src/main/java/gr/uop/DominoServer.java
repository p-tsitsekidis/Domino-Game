package gr.uop;

import gr.uop.GameEngine.GameEngine;
import gr.uop.GameEngine.Player;
import gr.uop.GameEngine.Tile;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Scanner;

public class DominoServer {
    private static final int PORT = 7777;
    private GameEngine gameEngine;

    public static void main(String[] args) {
        new DominoServer().startServer();
    }

    public void startServer() {
        try (ServerSocket serverSocket = new ServerSocket(PORT)) {
            System.out.println("Domino Server is running on port " + PORT);

            //Player 1 initialize
            System.out.println("Waiting for Player 1 to connect...");
            Socket player1Socket = serverSocket.accept();
            System.out.println("Player 1 connected.");
            Scanner fromPlayer1 = new Scanner(player1Socket.getInputStream());
            PrintWriter toPlayer1 = new PrintWriter(player1Socket.getOutputStream(), true);
            toPlayer1.println("Waiting for Player 2 to connect...");

            //Player 2 initialize
            System.out.println("Waiting for Player 2 to connect...");
            Socket player2Socket = serverSocket.accept();
            System.out.println("Player 2 connected.");
            toPlayer1.println("Player 2 connected");
            Scanner fromPlayer2 = new Scanner(player2Socket.getInputStream());
            PrintWriter toPlayer2 = new PrintWriter(player2Socket.getOutputStream(), true);
            toPlayer2.println("Waiting for Player 1 to enter their name...");

            //Get player names from the clients
            toPlayer1.println("Please enter your name:");
            String player1Name = fromPlayer1.nextLine();
            toPlayer1.println("Waiting for Player 2 to enter their name...");
            toPlayer2.println("Please enter your name:");
            String player2Name = fromPlayer2.nextLine();

            //Initialize GameEngine
            gameEngine = new GameEngine(player1Name, player2Name);
            Player player1 = gameEngine.getPlayer1();
            Player player2 = gameEngine.getPlayer2();

            toPlayer1.println("Hello " + player1Name + "! The game is starting...");
            toPlayer2.println("Hello " + player2Name + "! The game is starting...");

            //Main game loop
            boolean gameOver = false;
            while (!gameOver) {
                Player currentPlayer = gameEngine.getCurrentPlayer();
                Scanner fromCurrentPlayer = (currentPlayer == player1) ? fromPlayer1 : fromPlayer2;
                PrintWriter toCurrentPlayer = (currentPlayer == player1) ? toPlayer1 : toPlayer2;
                PrintWriter toOtherPlayer = (currentPlayer == player1) ? toPlayer2 : toPlayer1;

                //Game state
                toCurrentPlayer.println("\nIt's your turn, " + currentPlayer.getName() + "!");
                toCurrentPlayer.println("Your tiles: " + currentPlayer.getTiles());
                toCurrentPlayer.println("Current board: " + gameEngine.getLineOfPlay());

                toOtherPlayer.println("\nWaiting for " + currentPlayer.getName() + " to make a move...");
                toOtherPlayer.println("Current board: " + gameEngine.getLineOfPlay());

                boolean validMove = false;

                //Play or draw a tile
                while (!validMove && !gameEngine.isGameOver()) {
                    if (!gameEngine.canPlay()) {
                        //Automatically draw a tile if no valid moves exist
                        toCurrentPlayer.println("No available moves. Drawing from stock...");
                        while (!validMove && gameEngine.drawTile()) {
                            Tile drawnTile = currentPlayer.getTiles().get(currentPlayer.getTiles().size() - 1);
                            toCurrentPlayer.println("You drew: " + drawnTile);
                            toOtherPlayer.println(currentPlayer.getName() + " drew a tile.");

                            validMove = gameEngine.playTile(drawnTile);
                            if (validMove) {
                                toCurrentPlayer.println("You played: " + drawnTile);
                                toOtherPlayer.println(currentPlayer.getName() + " played: " + drawnTile);
                            }
                        }

                        //Stock empty and no valid move condition
                        if (!validMove) {
                            toCurrentPlayer.println("No valid tiles to draw and no more tiles in the stock. Passing turn.");
                            toOtherPlayer.println(currentPlayer.getName() + " has no valid tiles and no more tiles in the stock. Passing turn.");
                            break;
                        }
                    } else {
                        //Player moves
                        toCurrentPlayer.println("Enter the index of the tile you want to play:");
                        String input = fromCurrentPlayer.nextLine();

                        try {
                            int tileIndex = Integer.parseInt(input);
                            Tile chosenTile = currentPlayer.getTiles().get(tileIndex);

                            validMove = gameEngine.playTile(chosenTile);
                            if (validMove) {
                                toCurrentPlayer.println("You played: " + chosenTile);
                                toOtherPlayer.println(currentPlayer.getName() + " played: " + chosenTile);
                            } else {
                                toCurrentPlayer.println("Invalid move with selected tile. Try again.");
                            }
                        } catch (NumberFormatException | IndexOutOfBoundsException e) {
                            toCurrentPlayer.println("Invalid input or tile index. Try again.");
                        }
                    }
                }

                //Game over conditions
                if (gameEngine.isGameOver()) {
                    gameOver = true;
                    Player winner = gameEngine.getWinner();
                    toCurrentPlayer.println("\nGame over! The winner is: " + winner.getName());
                    toCurrentPlayer.println("\nYou scored: " + currentPlayer.getScore() + " points!");
                    toOtherPlayer.println("\nGame over! The winner is: " + winner.getName());
                    toOtherPlayer.println("\nYou scored: " + player2.getScore() + " points!");
                }
            }

            System.out.println("Game has ended. Closing connections.");
            fromPlayer1.close();
            fromPlayer2.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
