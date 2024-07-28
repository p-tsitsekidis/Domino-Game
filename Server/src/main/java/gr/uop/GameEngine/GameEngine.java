package gr.uop.GameEngine;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Random;

public class GameEngine {
    
    private List<Tile> stock;
    private Player player1;
    private Player player2;
    private Player currentPlayer;
    private List<Tile> lineOfPlay;

    public GameEngine(String player1Name, String player2Name) {
        stock = createDominoStock();
        Collections.shuffle(stock);

        List<Tile> player1Tiles = new ArrayList<>();
        List<Tile> player2Tiles = new ArrayList<>();

        for (int i = 0; i < 7; i++) {
            player1Tiles.add(stock.remove(stock.size() - 1));
            player2Tiles.add(stock.remove(stock.size() - 1));
        }

        player1 = new Player(player1Name, player1Tiles);
        player2 = new Player(player2Name, player2Tiles);
        currentPlayer = player1;

        lineOfPlay = new ArrayList<>();
    }

    private List<Tile> createDominoStock() {
        List<Tile> tiles = new ArrayList<>();
        for (int i = 0; i <= 6; i++) {
            for (int j = 0; j <= i; j++) {
                tiles.add(new Tile(i, j));
            }
        }
        return tiles;
    }

    // public List getOpenEnds() {

    // }

    // public LinkedHashMap getLineOfPlay() {
        
    // }
}
