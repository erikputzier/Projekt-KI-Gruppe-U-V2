import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

public class TranspositionTableArray {
    /** Table size (power of two!) – e.g. 16 MiB = 2 000 000 entries. */
    private static final int TABLE_SIZE   = 1 << 22;
    private static final int INDEX_MASK   = TABLE_SIZE - 1;
    // Entry types for transposition table
    public static final int EXACT_SCORE = 0;
    public static final int LOWER_BOUND = 1;
    public static final int UPPER_BOUND = 2;
    private final TTEntry[] table = new TTEntry[TABLE_SIZE];
    static final class TTEntry {
        long  zobrist;          // 8 bytes  – full verification key
        int   score;            // 4
        short depth;            // 2
        byte  type;             // 1  (EXACT / LOWER / UPPER)
        MovePair best;          // 4…8  (reference)

        // immutable helper for an empty slot
        static final TTEntry EMPTY = new TTEntry();
    }

    private static int indexOf(long zobrist) {
        return (int) (zobrist & INDEX_MASK);   // low bits work fine – hash is random
    }
    public void store(long key,
                      int  score,
                      short depth,
                      byte type,
                      MovePair best) {

        int idx = indexOf(key);
        TTEntry cur = table[idx];

        if (cur == null || depth >= cur.depth) {   // keep deeper or empty
            TTEntry e = new TTEntry();
            e.zobrist = key;
            e.score   = score;
            e.depth   = depth;
            e.type    = type;
            e.best    = best;
            table[idx] = e;
        }
    }

    public TTEntry retrieve(long key) {
        TTEntry e = table[indexOf(key)];
        return (e != null && e.zobrist == key) ? e : null;  // guard vs. collision
    }

    public void clear() {
        Arrays.fill(table, null);  // cheap – reference array only
    }

    public int size() {
        int count = 0;
        for (TTEntry e : table) if (e != null) ++count;
        return count;
    }
}
