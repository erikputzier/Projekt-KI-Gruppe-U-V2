/**
 * @param moveHeight how many pieces are moved (only relevant for towers)
 */
public record Move(int fromRow, int fromCol, int toRow, int toCol, int moveHeight) {

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

    public Move copy() {
        return new Move(fromRow, fromCol, toRow, toCol, moveHeight);
    }
}