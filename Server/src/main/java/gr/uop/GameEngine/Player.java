package gr.uop.GameEngine;

import java.util.ArrayList;
import java.util.List;

/**
 * The Player class represents a player in the game of dominoes. 
 * It holds the player's name, their tiles, and their score.
 */
public class Player {

    private String name;
    private ArrayList<Tile> tiles;
    private int score;

    /**
     * Constructs a Player with a name and an initial set of tiles.
     * The player's score is initialized to zero.
     *
     * @param name the name of the player
     * @param tiles the initial tiles for the player
     */
    public Player(String name, List<Tile> tiles) {
        this.name = name;
        this.tiles = new ArrayList<>(tiles);
        this.score = 0;
    }

    /**
     * Returns the player's name.
     *
     * @return the player's name
     */
    public String getName() {
        return this.name;
    }

    /**
     * Returns the player's tiles.
     *
     * @return the player's tiles as an ArrayList
     */
    public ArrayList<Tile> getTiles() {
        return this.tiles;
    }

    /**
     * Adds a tile to the player's hand.
     *
     * @param tile the tile to be added
     */
    public void addTile(Tile tile) {
        tiles.add(tile);
    }

    /**
     * Removes a tile from the player's hand.
     *
     * @param tile the tile to be removed
     */
    public void removeTile(Tile tile) {
        tiles.remove(tile);
    }

    /**
     * Returns the player's current score.
     *
     * @return the player's score
     */
    public int getScore() {
        return score;
    }

    /**
     * Updates the player's score by adding the specified points.
     *
     * @param points the points to add to the player's score
     */
    public void updateScore(int points) {
        this.score += points;
    }

    /**
     * Returns a string representation of the player, including their name, tiles, and score.
     *
     * @return a string representing the player's information
     */
    @Override
    public String toString() {
        return "Player{" +
                "name='" + name + '\'' +
                ", tiles=" + tiles +
                ", score=" + score +
                '}';
    }
}
