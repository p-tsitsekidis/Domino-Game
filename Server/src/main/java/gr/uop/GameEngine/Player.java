package gr.uop.GameEngine;

import java.util.ArrayList;

public class Player {

    private String name;
    private ArrayList<Tile> tiles = new ArrayList<>();
    private int score;

    public Player(String name) {
        this.name = name;
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