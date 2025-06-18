import java.util.HashMap;
import java.util.Map;

public class TranspositionTable {
    // Entry types for transposition table
    public static final int EXACT_SCORE = 0;
    public static final int LOWER_BOUND = 1;
    public static final int UPPER_BOUND = 2;

    public static class TTEntry {
        long zobristHash;
        int score;
        int depth;
        int entryType; // 0 for exact, 1 for lower bound (alpha), 2 for upper bound (beta)
        MovePair bestMove; // Optional: store best move for this position

        public TTEntry(long zobristHash, int score, int depth, int entryType, MovePair bestMove) {
            this.zobristHash = zobristHash;
            this.score = score;
            this.depth = depth;
            this.entryType = entryType;
            this.bestMove = bestMove;
        }
    }

    private final Map<Long, TTEntry> table;

    public TranspositionTable() {
        this.table = new HashMap<>();
    }

    public void store(long zobristHash, int score, int depth, int entryType, MovePair bestMove) {
        // Always replace or store if new. More sophisticated replacement strategies can be added (e.g. deeper entries).
        TTEntry currentEntry = table.get(zobristHash);
        if (currentEntry == null || depth >= currentEntry.depth) { // Store if new or deeper search
            table.put(zobristHash, new TTEntry(zobristHash, score, depth, entryType, bestMove));
        }
    }

    public TTEntry retrieve(long zobristHash) {
        return table.get(zobristHash);
    }

    public void clear() {
        table.clear();
    }

    public int size() {
        return table.size();
    }
}

