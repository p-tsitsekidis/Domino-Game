package gr.uop;

import java.util.Scanner;
import gr.uop.GameEngine.GameEngine;
import gr.uop.GameEngine.Player;
import gr.uop.GameEngine.Tile;

public class CommandLineGame {
    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);

        System.out.print("Enter name for Player 1: ");
        String player1Name = scanner.nextLine();

        System.out.print("Enter name for Player 2: ");
        String player2Name = scanner.nextLine();

        GameEngine gameEngine = new GameEngine(player1Name, player2Name);

        // Main game loop
        while (!gameEngine.isGameOver()) {
            Player currentPlayer = gameEngine.getCurrentPlayer();
            System.out.println("Current player: " + currentPlayer.getName());
            System.out.println("Your tiles: " + currentPlayer.getTiles());
            System.out.println("Line of play: " + gameEngine.getLineOfPlay());

            // Check if current player can play
            if (!gameEngine.canPlay()) {
                System.out.println("No playable tiles. Drawing a tile from the stock...");
                if (!gameEngine.drawTile()) {
                    System.out.println("No more tiles in the stock. Passing turn...");
                } else {
                    System.out.println("Drew a tile. Your new tiles: " + currentPlayer.getTiles());
                }
                continue; // Skip loop
            }

            // Choose Tile
            System.out.println("Choose a tile to play (index): ");
            int index = scanner.nextInt();
            scanner.nextLine(); // Consume newline
            if (index < 0 || index >= currentPlayer.getTiles().size()) {
                System.out.println("Invalid index. Try again.");
                continue;
            }
            Tile chosenTile = currentPlayer.getTiles().get(index);

            // Play tile
            if (gameEngine.playTile(chosenTile)) {
                System.out.println("Played tile: " + chosenTile);
            } else {
                System.out.println("Tile does not fit. Try again.");
            }
        }

        // Game over
        Player winner = gameEngine.getWinner();
        System.out.println("Game over! The winner is: " + winner.getName());
        System.out.println("Final scores:");
        System.out.println(player1Name + ": " + gameEngine.getPlayer1().getScore() + " points");
        System.out.println(player2Name + ": " + gameEngine.getPlayer2().getScore() + " points");
        scanner.close();
    }
}
