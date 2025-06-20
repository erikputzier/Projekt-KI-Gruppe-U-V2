/**
 * A record that represents a move pair consisting of two positions (from, to) and a height value.
 * This is used to represent a move on a bitboard and to compare moves for equality.
 */
public record MovePair(int from, int to, int height) {

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof MovePair(int from1, int v, int height1))) return false;
        return this.from == from1 && this.to == v && this.height == height1;
    }

    @Override
    public int hashCode() {
        int result = Integer.hashCode(from);
        result = 31 * result + Integer.hashCode(to);
        result = 31 * result + Integer.hashCode(height);
        return result;
    }

    public Move toMove() {
        int fromCol = 6 - (this.from % 7);
        int fromRow = 6 - (this.from / 7);
        int toCol = 6 - (this.to % 7);
        int toRow = 6 - (this.to / 7);

        return new Move(fromRow, fromCol, toRow, toCol, this.height);
    }

    public String toString() {
        return (from + ", " + to);
    }
}