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
        if (lineOfPlay.isEmpty()) {
            lineOfPlay.add(tile);
            currentPlayer.removeTile(tile);
            switchPlayer();
            return true;
        }

        Tile lastTile = lineOfPlay.getLast();
        if (tile.fits(lastTile.getUpperValue()) || tile.fits(lastTile.getBottomValue())) {
            if (!tile.fits(lastTile.getUpperValue())) {
                tile.invert();
            }
            lineOfPlay.add(tile);
            currentPlayer.removeTile(tile);
            switchPlayer();
            return true;
        }

        return false;
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

    // public List getOpenEnds() {

    // }

    // public LinkedHashMap getLineOfPlay() {
        
    // }
}
