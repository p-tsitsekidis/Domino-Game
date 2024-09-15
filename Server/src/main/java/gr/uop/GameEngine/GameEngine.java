package gr.uop.GameEngine;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Deque;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.Random;

public class GameEngine {
    
    private Queue<Tile> stock;
    private Player player1;
    private Player player2;
    private Player currentPlayer;
    private Deque<Tile> lineOfPlay;

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

    private Queue<Tile> createDominoStock() {
        Queue<Tile> tiles = new LinkedList<>();
        for (int i = 0; i <= 6; i++) {
            for (int j = 0; j <= i; j++) {
                tiles.add(new Tile(i, j));
            }
        }
        return tiles;
    }

    public boolean playTile(Tile tile) {
        if (lineOfPlay.isEmpty()) { //Empty line of play
            lineOfPlay.add(tile);
            currentPlayer.removeTile(tile);
            switchPlayer();
            return true;
        }

        Tile firstTile = lineOfPlay.getFirst();
        Tile lastTile = lineOfPlay.getLast();

        if (tile.fits(firstTile.getUpperValue())) { //Check if matches the first tile
            if (tile.getBottomValue() != firstTile.getUpperValue()) {
                tile.invert();
            }
            lineOfPlay.addFirst(tile);
            currentPlayer.removeTile(tile);
            switchPlayer();
            return true;
        } else if (tile.fits(lastTile.getBottomValue())) { //Check if matched the last tile
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

    public boolean drawTile() { //If a player cannot play
        if (stock.isEmpty()) {
            return false;
        }
    
        Tile drawnTile = stock.poll();
        currentPlayer.addTile(drawnTile);
        return true;
    }

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

    public boolean isGameOver() {
        return player1.getTiles().isEmpty() || player2.getTiles().isEmpty() || (!canPlay() && stock.isEmpty());
    }

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

            if (player1Sum < player2Sum) {
                player1.updateScore(player2Sum - player1Sum);
                return player1;
            } else {
                player2.updateScore(player1Sum - player2Sum);
                return player2;
            }
        }
    }

    private void calculateFinalScore(Player winner, Player loser) {
        int loserHandSum = calculateHandSum(loser);
        winner.updateScore(loserHandSum);
    }

    private int calculateHandSum(Player player) {
        return player.getTiles().stream().mapToInt(tile -> tile.getUpperValue() + tile.getBottomValue()).sum();
    }
    
    private void switchPlayer() {
        currentPlayer = (currentPlayer == player1) ? player2 : player1;
    }

    public Player getCurrentPlayer() {
        return currentPlayer;
    }

    public Deque<Tile> getLineOfPlay() {
        return lineOfPlay;
    }

    public int[] getOpenEnds() { //Array is extremely efficient since we only have 2 open ends (No spinners)
        int[] openEnds = new int[2];
        if (!lineOfPlay.isEmpty()) {
            openEnds[0] = lineOfPlay.getFirst().getUpperValue();
            openEnds[1] = lineOfPlay.getLast().getBottomValue();
        }
        return openEnds;
    }

    public Player getPlayer1() {
        return player1;
    }
    
    public Player getPlayer2() {
        return player2;
    }    

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
