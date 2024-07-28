package gr.uop.GameEngine;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Deque;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;

public class GameEngine {
    
    private Deque<Tile> stock;
    private Player player1;
    private Player player2;
    private Player currentPlayer;
    private List<Tile> lineOfPlay;

    public GameEngine(String player1Name, String player2Name) {
        stock = createDominoStock();
        Collections.shuffle((List<?>) stock);

        List<Tile> player1Tiles = new ArrayList<>();
        List<Tile> player2Tiles = new ArrayList<>();

        for (int i = 0; i < 7; i++) {
            player1Tiles.add(stock.pop());
            player2Tiles.add(stock.pop());
        }

        player1 = new Player(player1Name, player1Tiles);
        player2 = new Player(player2Name, player2Tiles);
        currentPlayer = player1;

        lineOfPlay = new ArrayList<>();
    }

    private Deque<Tile> createDominoStock() {
        Deque<Tile> tiles = new LinkedList<>();
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

        Tile lastTile = lineOfPlay.get(lineOfPlay.size() - 1);
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

    public List<Tile> getLineOfPlay() {
        return lineOfPlay;
    }

    // public List getOpenEnds() {

    // }

    // public LinkedHashMap getLineOfPlay() {
        
    // }
}
