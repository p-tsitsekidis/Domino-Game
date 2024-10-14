package gr.uop;

import gr.uop.GameEngine.GameEngine;
import gr.uop.GameEngine.Player;
import gr.uop.GameEngine.Tile;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Scanner;

/**
 * The DominoServer class manages a networked domino game between two players.
 * It waits for player connections, handles the game flow, and communicates game state and moves to the clients.
 */
public class DominoServer {

    private static final int PORT = 7777; // The port the server listens on for client connections
    private GameEngine gameEngine;

    /**
     * The main method to start the server.
     * 
     * @param args Command-line arguments (not used).
     */
    public static void main(String[] args) {
        new DominoServer().startServer();
    }

    /**
     * Starts the Domino game server, waits for player connections, and handles the game logic and communication.
     */
    public void startServer() {
        try (ServerSocket serverSocket = new ServerSocket(PORT)) {
            System.out.println("Domino Server is running on port " + PORT);

            // Initialize Player 1
            System.out.println("Waiting for Player 1 to connect...");
            Socket player1Socket = serverSocket.accept();
            System.out.println("Player 1 connected.");
            Scanner fromPlayer1 = new Scanner(player1Socket.getInputStream());
            PrintWriter toPlayer1 = new PrintWriter(player1Socket.getOutputStream(), true);
            toPlayer1.println("WAIT_CONNECT");

            // Initialize Player 2
            System.out.println("Waiting for Player 2 to connect...");
            Socket player2Socket = serverSocket.accept();
            System.out.println("Player 2 connected.");
            toPlayer1.println("CONNECTED");
            Scanner fromPlayer2 = new Scanner(player2Socket.getInputStream());
            PrintWriter toPlayer2 = new PrintWriter(player2Socket.getOutputStream(), true);
            toPlayer2.println("WAIT_PLAYER1_NAME");

            // Get player names from the clients
            toPlayer1.println("NAME_REQUEST");
            String player1Name = fromPlayer1.nextLine();
            toPlayer1.println("WAIT_PLAYER2_NAME");
            toPlayer2.println("NAME_REQUEST");
            String player2Name = fromPlayer2.nextLine();

            // Initialize GameEngine with player names
            gameEngine = new GameEngine(player1Name, player2Name);
            Player player1 = gameEngine.getPlayer1();

            // Send initialization completion messages to clients
            toPlayer1.println("END_INIT " + player2Name);
            toPlayer2.println("END_INIT " + player1Name);

            // Main game loop
            boolean gameOver = false;
            while (!gameOver) {
                Player currentPlayer = gameEngine.getCurrentPlayer();
                Player opponent = gameEngine.getOpponent();
                Scanner fromCurrentPlayer = (currentPlayer == player1) ? fromPlayer1 : fromPlayer2;
                PrintWriter toCurrentPlayer = (currentPlayer == player1) ? toPlayer1 : toPlayer2;
                PrintWriter toOpponent = (currentPlayer == player1) ? toPlayer2 : toPlayer1;

                // Communicate game state to the players
                toCurrentPlayer.println("OPPONENT_TILE_SIZE " + gameEngine.getPlayerTileCount(opponent));
                toCurrentPlayer.println("TURN");
                toCurrentPlayer.println("STOCK_SIZE " + gameEngine.getStockSize());
                toCurrentPlayer.println("TILES " + currentPlayer.getTiles());
                toCurrentPlayer.println("BOARD " + gameEngine.getLineOfPlay());

                toOpponent.println("OPPONENT_TILE_SIZE " + gameEngine.getPlayerTileCount(currentPlayer));
                toOpponent.println("STOCK_SIZE " + gameEngine.getStockSize());
                toOpponent.println("TILES " + opponent.getTiles());
                toOpponent.println("BOARD " + gameEngine.getLineOfPlay());
                toOpponent.println("WAIT_OPPONENT_MOVE");

                boolean validMove = false;

                // Handle player move or draw from stock
                while (!validMove && !gameEngine.isGameOver()) {
                    if (!gameEngine.canPlay()) {
                        // Automatically draw if no moves are possible
                        toCurrentPlayer.println("NO_AVAILABLE_MOVES");
                        while (!validMove && gameEngine.drawTile()) {
                            Tile drawnTile = currentPlayer.getTiles().get(currentPlayer.getTiles().size() - 1);
                            toCurrentPlayer.println("DRAW " + drawnTile);
                            toOpponent.println("OPPONENT_DRAW");

                            validMove = gameEngine.playTile(drawnTile);
                            if (validMove) {
                                toCurrentPlayer.println("PLAYED " + drawnTile);
                                toOpponent.println("OPP_PLAYED " + drawnTile);
                            }
                        }

                        if (!validMove) { // No valid moves and no tiles to draw from stock
                            toCurrentPlayer.println("PASS");
                            toOpponent.println("OPP_PASS");
                            gameEngine.passTurn();
                            break;
                        }
                    } else {
                        // Player plays a tile
                        toCurrentPlayer.println("INDEX");
                        String input = fromCurrentPlayer.nextLine();

                        try {
                            int tileIndex = Integer.parseInt(input);
                            Tile chosenTile = currentPlayer.getTiles().get(tileIndex);

                            validMove = gameEngine.playTile(chosenTile);
                            if (validMove) {
                                toCurrentPlayer.println("PLAYED " + chosenTile);
                                toOpponent.println("OPP_PLAYED " + chosenTile);
                            } else {
                                toCurrentPlayer.println("INVALID_MOVE");
                            }
                        } catch (NumberFormatException | IndexOutOfBoundsException e) {
                            toCurrentPlayer.println("INVALID_INPUT");
                        }
                    }
                }

                // Handle game over condition
                if (gameEngine.isGameOver()) {
                    gameOver = true;
                    Player winner = gameEngine.getWinner();
                    Player notWinner = gameEngine.getOpponent(winner);

                    toCurrentPlayer.println("GAME_OVER " + winner.getName());
                    toCurrentPlayer.println("SCORE " + currentPlayer.getScore());

                    toOpponent.println("GAME_OVER " + winner.getName());
                    toOpponent.println("SCORE " + notWinner.getScore());
                }
            }

            // Close player connections after game ends
            System.out.println("Game has ended. Closing connections.");
            fromPlayer1.close();
            fromPlayer2.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}