import java.util.Objects;

public class Move {
    public final int fromRow, fromCol;
    public final int toRow, toCol;
    public final int moveHeight; // how many pieces are moved (only relevant for towers)

    public Move(int fromRow, int fromCol, int toRow, int toCol, int moveHeight) {
        this.fromRow = fromRow;
        this.fromCol = fromCol;
        this.toRow = toRow;
        this.toCol = toCol;
        this.moveHeight = moveHeight;
    }

    public boolean isSameSquare() {
        return fromRow == toRow && fromCol == toCol;
    }

    @Override
    public String toString() {
        return String.format("Move from (%d,%d) to (%d,%d) [%d piece%s]",
                fromRow, fromCol, toRow, toCol, moveHeight, moveHeight == 1 ? "" : "s");
    }

    public String toAlgebraic() {
        return "" + (char) ('A' + fromCol) + (7 - fromRow) + "-"
                + (char) ('A' + toCol) + (7 - toRow) + "-" + moveHeight;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        Move other = (Move) obj;
        return fromRow == other.fromRow &&
                fromCol == other.fromCol &&
                toRow == other.toRow &&
                toCol == other.toCol &&
                moveHeight == other.moveHeight;
    }

    @Override
    public int hashCode() {
        return Objects.hash(fromRow, fromCol, toRow, toCol, moveHeight);
    }

    public Move copy() {
        return new Move(fromRow, fromCol, toRow, toCol, moveHeight);
    }
}