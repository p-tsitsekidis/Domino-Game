package gr.uop.GameEngine;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Deque;
import java.util.LinkedHashMap;
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
        currentPlayer = player1;

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
            if (!tile.fits(firstTile.getUpperValue())) {
                tile.invert();
            }
            lineOfPlay.addFirst(tile);
            currentPlayer.removeTile(tile);
            switchPlayer();
            return true;
        } else if (tile.fits(lastTile.getBottomValue())) { //Check if matched the last tile
            if (!tile.fits(lastTile.getBottomValue())) {
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
