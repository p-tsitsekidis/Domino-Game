package gr.uop.GameEngine;

public class Tile {

    private Integer upperValue;
    private Integer bottomValue;

    public Tile(Integer upperValue, Integer bottomValue) {
        this.upperValue = upperValue;
        this.bottomValue = bottomValue;
    }

    public Integer getUpperValue() {
        return this.upperValue;
    }

    public Integer getBottomValue() {
        return this.bottomValue;
    }

    public Boolean fits(Tile otherTile) {

        if (this.upperValue == otherTile.upperValue || this.upperValue == otherTile.bottomValue ||
        this.bottomValue == otherTile.upperValue || this.bottomValue == otherTile.bottomValue) {

            return true;

        }
        return false;
    }

    public Boolean fits(int value) {
        if (this.upperValue == value || this.bottomValue == value) {
            return true;
        }
        return false;
    }

    public void invert() {
        int temp = this.upperValue;
        this.upperValue = this.bottomValue;
        this.bottomValue = temp;
    }

    @Override
    public String toString() {
        return "[" + upperValue + ":" + bottomValue + "]";
    }
}
