import org.junit.Test;

import static org.junit.Assert.*;

/**
 * Unit tests for TranspositionTable.
 * Keeps each test focused on one observable behaviour.
 */
public class TranspositionTableTest {

    /**
     * Convenience dummy move (from A7 to A6, height 1).
     */
    private static final MovePair DUMMY_MOVE = new MovePair(0, 7, 1);

    @Test
    //store() followed by retrieve() returns identical entry
    public void storeAndRetrieveRoundTrip() {
        TranspositionTable tt = new TranspositionTable();

        long key = 0xCAFEBABEL;
        tt.store(key, /*score*/ 42, /*depth*/ 5, TranspositionTable.EXACT_SCORE, DUMMY_MOVE);

        TranspositionTable.TTEntry e = tt.retrieve(key);
        assertNotNull(e); //Entry must be present after storing
        assertEquals(42, e.score);
        assertEquals(5, e.depth);
        assertEquals(TranspositionTable.EXACT_SCORE, e.entryType);
        assertEquals(DUMMY_MOVE, e.bestMove);
    }

    @Test
    //Shallower entry must NOT replace deeper one (depth-prefer rule)
    public void depthPreferDoesNotReplaceWithShallower() {
        TranspositionTable tt = new TranspositionTable();
        long key = 0xDEADBEEFL;

        // First store a deep entry
        tt.store(key, 50, /*depth*/ 7, TranspositionTable.EXACT_SCORE, null);

        // Now try to overwrite with a shallower search result
        tt.store(key, -10, /*depth*/ 4, TranspositionTable.EXACT_SCORE, null);

        assertEquals(50, tt.retrieve(key).score); //Shallower entry must not overwrite deeper one
    }

    @Test
    //Deeper entry MUST replace shallower one (depth-prefer rule)
    public void depthPreferReplacesWithDeeper() {
        TranspositionTable tt = new TranspositionTable();
        long key = 0x12345678L;

        tt.store(key, 15, /*depth*/ 3, TranspositionTable.LOWER_BOUND, null);
        tt.store(key, 100, /*depth*/ 6,                 // deeper search
                TranspositionTable.UPPER_BOUND, DUMMY_MOVE);

        TranspositionTable.TTEntry e = tt.retrieve(key);
        assertEquals(100, e.score);
        assertEquals(6, e.depth);
        assertEquals(TranspositionTable.UPPER_BOUND, e.entryType);
    }

    @Test
    //clear() empties the table and resets size()"
    public void clearEmptiesTable() {
        TranspositionTable tt = new TranspositionTable();
        tt.store(0x1L, 0, 1, TranspositionTable.EXACT_SCORE, null);
        tt.store(0x2L, 0, 1, TranspositionTable.EXACT_SCORE, null);

        assertEquals(2, tt.size()); //Two entries expected before clear()
        tt.clear();
        assertEquals(0, tt.size()); //Table must be empty after clear()
        assertNull(tt.retrieve(0x1L)); //Entries must be gone after clear()
    }
}