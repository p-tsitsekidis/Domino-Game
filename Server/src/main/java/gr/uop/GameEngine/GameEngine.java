package gr.uop.GameEngine;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Deque;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.Random;

/**
 * GameEngine class is responsible for managing the game state, 
 * including players, tiles, and the logic of gameplay.
 */
public class GameEngine {

    private Player player1;
    private Player player2;
    private Player currentPlayer;

    private Queue<Tile> stock;
    private Deque<Tile> lineOfPlay; // The sequence of tiles currently played in the game
    
    /**
     * Constructor to initialize the GameEngine with two players.
     * It creates the stock, shuffles the tiles, and assigns 7 tiles to each player.
     * 
     * @param player1Name Name of the first player
     * @param player2Name Name of the second player
     */
    public GameEngine(String player1Name, String player2Name) {
        stock = createDominoStock();
        Collections.shuffle((List<?>) stock);

        List<Tile> player1Tiles = new ArrayList<>();
        List<Tile> player2Tiles = new ArrayList<>();

        for (int i = 0; i < 7; i++) {
            player1Tiles.add(stock.poll());
            player2Tiles.add(stock.poll());
        }

        player1 = new Player(player1Name, player1Tiles);
        player2 = new Player(player2Name, player2Tiles);
        
        Random random = new Random();
        currentPlayer = random.nextBoolean() ? player1 : player2;

        lineOfPlay = new LinkedList<>();
    }

    /**
     * Creates the initial domino stock (all tile combinations) and returns it.
     * 
     * @return A queue of all the domino tiles.
     */
    private Queue<Tile> createDominoStock() {
        Queue<Tile> tiles = new LinkedList<>();
        for (int i = 0; i <= 6; i++) {
            for (int j = 0; j <= i; j++) {
                tiles.add(new Tile(i, j));
            }
        }
        return tiles;
    }

    /**
     * Allows the current player to play a tile.
     * It checks whether the tile can be placed on either side of the line of play.
     * If the tile is valid, it is placed and the player removes it from their hand.
     * 
     * @param tile The tile to be played.
     * @return true if the tile was successfully played, false otherwise.
     */
    public boolean playTile(Tile tile) {
        if (lineOfPlay.isEmpty()) { //Empty line of play
            lineOfPlay.add(tile);
            currentPlayer.removeTile(tile);
            switchPlayer();
            return true;
        }

        Tile firstTile = lineOfPlay.getFirst();
        Tile lastTile = lineOfPlay.getLast();

        if (tile.fits(firstTile.getUpperValue())) { // Check if the tile matches the first tile
            if (tile.getBottomValue() != firstTile.getUpperValue()) {
                tile.invert();
            }
            lineOfPlay.addFirst(tile);
            currentPlayer.removeTile(tile);
            switchPlayer();
            return true;
        } else if (tile.fits(lastTile.getBottomValue())) { // Check if the tile matches the last tile
            if (tile.getUpperValue() != lastTile.getBottomValue()) {
                tile.invert();
            }
            lineOfPlay.addLast(tile);
            currentPlayer.removeTile(tile);
            switchPlayer();
            return true;
        }

        return false; //Doesn't fit
    }

    /**
     * Allows the current player to draw a tile from the stock when they have no valid moves.
     * 
     * @return true if a tile was drawn, false if the stock is empty.
     */
    public boolean drawTile() {
        if (stock.isEmpty()) {
            return false;
        }
    
        Tile drawnTile = stock.poll();
        currentPlayer.addTile(drawnTile);
        return true;
    }

    /**
     * Checks whether the current player has a valid move to play on the board.
     * 
     * @return true if the player has a valid move, false otherwise.
     */
    public boolean canPlay() {
        if (lineOfPlay.isEmpty()) {
            return true;
        }
        for (Tile tile : currentPlayer.getTiles()) {
            if (tile.fits(lineOfPlay.getFirst().getUpperValue()) || tile.fits(lineOfPlay.getLast().getBottomValue())) {
                return true;
            }
        }
        return false;
    }

    /**
     * Checks if the game is over. The game ends when a player has no tiles or 
     * when no valid moves can be made and the stock is empty.
     * 
     * @return true if the game is over, false otherwise.
     */
    public boolean isGameOver() {
        return player1.getTiles().isEmpty() || player2.getTiles().isEmpty() || (!canPlay() && stock.isEmpty());
    }

    /**
     * Determines the winner of the game.
     * The winner is the player who empties their hand first or has the fewest points 
     * when no valid moves can be made. Draws are handed as wins to player 2.
     * 
     * @return The winning player.
     */
    public Player getWinner() { //Returns the winning player
        if (player1.getTiles().isEmpty()) {
            calculateFinalScore(player1, player2);
            return player1;
        } else if (player2.getTiles().isEmpty()) {
            calculateFinalScore(player2, player1);
            return player2;
        } else {
            int player1Sum = calculateHandSum(player1);
            int player2Sum = calculateHandSum(player2);

            player1.updateScore(player2Sum);
            player2.updateScore(player1Sum);

            if (player1Sum < player2Sum) {
                player1.updateScore(player2Sum - player1Sum);
                return player1;
            } else {
                player2.updateScore(player1Sum - player2Sum);
                return player2;
            }
        }
    }

    /**
     * Calculates and updates the final score of the winner.
     * The score is based on the remaining tiles in the opponent's hand.
     * 
     * @param winner The player who won the game.
     * @param opponent The opponent of the winning player.
     */
    private void calculateFinalScore(Player winner, Player opponent) {
        int opponentHandSum = calculateHandSum(opponent);
        winner.updateScore(opponentHandSum);
    }

    /**
     * Calculates the sum of all tile values in a player's hand.
     * 
     * @param player The player whose hand is being calculated.
     * @return The total sum of the tile values.
     */
    private int calculateHandSum(Player player) {
        return player.getTiles().stream().mapToInt(tile -> tile.getUpperValue() + tile.getBottomValue()).sum();
    }

    /**
     * Switches the current player, alternating between player1 and player2.
     */
    private void switchPlayer() {
        currentPlayer = (currentPlayer == player1) ? player2 : player1;
    }

    // Getter Methods

    /**
     * Returns the current player who is about to make a move.
     * 
     * @return The current player.
     */
    public Player getCurrentPlayer() {
        return currentPlayer;
    }

    /**
     * Returns the opponent of the current player.
     * @return the opponent of the current player.
     */
    public Player getOpponent() {
        return (currentPlayer == player1) ? player2 : player1;
    }

    /**
     * Returns the opponent of the given player (used, for example, to determine the player who did not win).
     * @param player Any Player object.
     * @return The opponent of the given player.
     */
    public Player getOpponent(Player player) {
        return (player == player1) ? player2 : player1;
    }

    /**
     * Returns the current line of play, i.e., the tiles played on the board.
     * 
     * @return A deque containing the tiles in the line of play.
     */
    public Deque<Tile> getLineOfPlay() {
        return lineOfPlay;
    }

    /**
     * Returns the two open ends of the current line of play, which are used to validate tile placements.
     * 
     * @return An array containing the two open ends of the line of play.
     */
    public int[] getOpenEnds() { // Array is extremely efficient since we only have 2 open ends (No spinners)
        int[] openEnds = new int[2];
        if (!lineOfPlay.isEmpty()) {
            openEnds[0] = lineOfPlay.getFirst().getUpperValue();
            openEnds[1] = lineOfPlay.getLast().getBottomValue();
        }
        return openEnds;
    }

    /**
     * Returns Player 1 in the game.
     * 
     * @return Player 1 object.
     */
    public Player getPlayer1() {
        return player1;
    }

    /**
     * Returns Player 2 in the game.
     * 
     * @return Player 2 object.
     */
    public Player getPlayer2() {
        return player2;
    }

    /**
     * Returns the number of tiles remaining in the stock.
     * 
     * @return The size of the stock.
     */
    public int getStockSize() {
        return stock.size();
    }

    // toString Method

    /**
     * Returns a string representation of the current state of the game engine,
     * including the stock, line of play, players, and current player.
     * 
     * @return A string representing the game engine state.
     */
    @Override
    public String toString() {
        return "GameEngine{" +
                "stock=" + stock +
                ", lineOfPlay=" + lineOfPlay +
                ", player1=" + player1 +
                ", player2=" + player2 +
                ", currentPlayer=" + currentPlayer +
                '}';
    }
}
