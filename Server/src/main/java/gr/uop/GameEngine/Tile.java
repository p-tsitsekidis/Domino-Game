package gr.uop.GameEngine;

/**
 * Represents a single domino tile with two values: upperValue and bottomValue.
 * A tile can be flipped, and its values can be compared with other tiles or a single value
 * to check for matches.
 */
public class Tile {

    private Integer upperValue;
    private Integer bottomValue;

    /**
     * Constructs a Tile with specified upper and bottom values.
     * 
     * @param upperValue the value on the upper side of the tile
     * @param bottomValue the value on the bottom side of the tile
     */
    public Tile(Integer upperValue, Integer bottomValue) {
        this.upperValue = upperValue;
        this.bottomValue = bottomValue;
    }

    /**
     * Gets the upper value of the tile.
     * 
     * @return the upper value of the tile
     */
    public Integer getUpperValue() {
        return this.upperValue;
    }

    /**
     * Gets the bottom value of the tile.
     * 
     * @return the bottom value of the tile
     */
    public Integer getBottomValue() {
        return this.bottomValue;
    }

    /**
     * Checks if this tile fits (matches) with another tile based on any of their values.
     * 
     * @param otherTile the other tile to compare
     * @return {@code true} if the tiles share at least one value, {@code false} otherwise
     */
    public Boolean fits(Tile otherTile) {

        if (this.upperValue == otherTile.upperValue || this.upperValue == otherTile.bottomValue ||
        this.bottomValue == otherTile.upperValue || this.bottomValue == otherTile.bottomValue) {

            return true;

        }
        return false;
    }

    /**
     * Checks if this tile matches a specific value on either side.
     * 
     * @param value the value to compare
     * @return {@code true} if either the upper or bottom value matches the given value, {@code false} otherwise
     */
    public Boolean fits(int value) {
        if (this.upperValue == value || this.bottomValue == value) {
            return true;
        }
        return false;
    }

    /**
     * Inverts the tile, swapping the upper and bottom values.
     */
    public void invert() {
        int temp = this.upperValue;
        this.upperValue = this.bottomValue;
        this.bottomValue = temp;
    }

    /**
     * Returns a string representation of the tile in the format "[upperValue:bottomValue]".
     * 
     * @return a string representation of the tile
     */
    @Override
    public String toString() {
        return "[" + upperValue + ":" + bottomValue + "]";
    }
}
