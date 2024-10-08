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
            toPlayer1.println("WAIT_CONNECT");

            //Player 2 initialize
            System.out.println("Waiting for Player 2 to connect...");
            Socket player2Socket = serverSocket.accept();
            System.out.println("Player 2 connected.");
            toPlayer1.println("CONNECTED");
            Scanner fromPlayer2 = new Scanner(player2Socket.getInputStream());
            PrintWriter toPlayer2 = new PrintWriter(player2Socket.getOutputStream(), true);
            toPlayer2.println("WAIT_PLAYER1_NAME");

            //Get player names from the clients
            toPlayer1.println("NAME_REQUEST");
            String player1Name = fromPlayer1.nextLine();
            toPlayer1.println("WAIT_PLAYER2_NAME");
            toPlayer2.println("NAME_REQUEST");
            String player2Name = fromPlayer2.nextLine();

            //Initialize GameEngine
            gameEngine = new GameEngine(player1Name, player2Name);
            Player player1 = gameEngine.getPlayer1();
            Player player2 = gameEngine.getPlayer2();

            toPlayer1.println("END_INIT " + player2Name);
            toPlayer2.println("END_INIT " + player1Name);

            //Main game loop
            boolean gameOver = false;
            while (!gameOver) {
                Player currentPlayer = gameEngine.getCurrentPlayer();

                Scanner fromCurrentPlayer = (currentPlayer == player1) ? fromPlayer1 : fromPlayer2;
                PrintWriter toCurrentPlayer = (currentPlayer == player1) ? toPlayer1 : toPlayer2;
                PrintWriter toOtherPlayer = (currentPlayer == player1) ? toPlayer2 : toPlayer1;

                //Game state
                toCurrentPlayer.println("TURN");
                
                toCurrentPlayer.println("STOCK_SIZE " + gameEngine.getStockSize());
                toCurrentPlayer.println("TILES " + currentPlayer.getTiles());
                toCurrentPlayer.println("BOARD " + gameEngine.getLineOfPlay());
                
                toOtherPlayer.println("STOCK_SIZE " + gameEngine.getStockSize());
                if (currentPlayer.equals(player1)) {
                    toOtherPlayer.println("TILES " + player2.getTiles());
                } else {
                    toOtherPlayer.println("TILES " + player1.getTiles());
                }
                
                toOtherPlayer.println("BOARD " + gameEngine.getLineOfPlay());
                toOtherPlayer.println("WAIT_OPPONENT_MOVE");

                boolean validMove = false;

                //Play or draw a tile
                while (!validMove && !gameEngine.isGameOver()) {
                    if (!gameEngine.canPlay()) {
                        //Automatically draw a tile if no valid moves exist
                        toCurrentPlayer.println("NO_AVAILABLE_MOVES");
                        while (!validMove && gameEngine.drawTile()) {
                            Tile drawnTile = currentPlayer.getTiles().get(currentPlayer.getTiles().size() - 1);
                            toCurrentPlayer.println("DRAW " + drawnTile);
                            toOtherPlayer.println("OPPONENT_DRAW");

                            validMove = gameEngine.playTile(drawnTile);
                            if (validMove) {
                                toCurrentPlayer.println("PLAYED " + drawnTile);
                                toOtherPlayer.println("OPP_PLAYED " + drawnTile);
                            }
                        }

                        //Stock empty and no valid move condition
                        if (!validMove) {
                            toCurrentPlayer.println("PASS");
                            toOtherPlayer.println("OPP_PASS");
                            break;
                        }
                    } else {
                        //Player moves
                        toCurrentPlayer.println("INDEX");
                        int input = fromCurrentPlayer.nextInt();

                        try {
                            int tileIndex = input;
                            Tile chosenTile = currentPlayer.getTiles().get(tileIndex);

                            validMove = gameEngine.playTile(chosenTile);
                            if (validMove) {
                                toCurrentPlayer.println("PLAYED " + chosenTile);
                                toOtherPlayer.println("OPP_PLAYED " + chosenTile);
                            } else {
                                toCurrentPlayer.println("INVALID_MOVE");
                            }
                        } catch (NumberFormatException | IndexOutOfBoundsException e) {
                            toCurrentPlayer.println("INVALID_INPUT");
                        }
                    }
                }

                //Game over conditions
                if (gameEngine.isGameOver()) {
                    gameOver = true;
                    Player winner = gameEngine.getWinner();
                    Player opponent = (player1Name.equals(winner.getName()) ? player2 : player1);
                    toCurrentPlayer.println("GAME_OVER " + winner.getName());
                    toCurrentPlayer.println("SCORE " + currentPlayer.getScore());
                    toOtherPlayer.println("GAME_OVER " + winner.getName());
                    toOtherPlayer.println("SCORE " + opponent.getScore());
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
