/**
 * Hilfsklasse um Züge besser speichern zu können
 *
 * @param from   int-Repräsentation des Start-Feldes eines Zuges
 * @param to     int-Repräsentation des End-Feldes eines Zuges
 * @param height int-Repräsentation der Höhe eines Zuges
 */
public record MovePair(int from, int to, int height) {
    /**
     * Methode um MovePairs miteinander zu vergleichen
     *
     * @param o anderes MovePair, mit dem dieses vergleichen werden soll
     * @return Boolscher Wert der angibt ob die beiden MovePairs die Instanzvariablen der beiden MovePairs gleich sind
     */
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