package gr.uop.GameEngine;

import java.util.ArrayList;
import java.util.List;

public class Player {

    private String name;
    private ArrayList<Tile> tiles;
    private int score;

    public Player(String name, List<Tile> tiles) {
        this.name = name;
        this.tiles = new ArrayList<>(tiles);
        this.score = 0;
    }

    public String getName() {
        return this.name;
    }

    public ArrayList<Tile> getTiles() {
        return this.tiles;
    }

    public void addTile(Tile tile) {
        tiles.add(tile);
    }

    public void removeTile(Tile tile) {
        tiles.remove(tile);
    }

    public int getScore() {
        return score;
    }

    public void updateScore(int points) {
        this.score += points;
    }

    @Override
    public String toString() {
        return "Player{" +
                "name='" + name + '\'' +
                ", tiles=" + tiles +
                ", score=" + score +
                '}';
    }
}
