import static org.junit.Assert.*;

import java.util.*;

import org.junit.Test;

/**
 * Verifies that a depth-0 search still “sees” a tactical win because the
 * quiescence routine keeps extending along the capture.
 * <p>
 * Preconditions:
 * – Quiet-search code from the earlier steps is already wired into
 * AI.minimaxAlphaBeta(..)  (or whatever public wrapper you expose).
 * – Eval.WIN_SCORE (or similar) is the constant returned when the
 * side-to-move has captured the last guard / achieved a forced win.
 */
public class AIQuiescenceTest {

    @Test
    public void testWinVisibleAtDepth0() {
        /* ---------- construct the one-move-win position (same as before) ---------- */
        Board b = new Board();
        long redTower = 1L << 10;   // D2
        long blueGuard = 1L << 3;    // D1
        b.setRed(redTower);
        b.setBlue(blueGuard);
        b.setGuards(blueGuard);
        b.setStack(0, redTower | blueGuard);   // height-1 pieces only

        /* ---------- run a depth-0 search (time-budget 1 ms = “instant”) ---------- */
        int score = AI.minimaxAlphaBeta(b, 1);

        /* ---------- ask the engine what move it would actually play ---------- */
        MovePair bestPair = AI.pickMove(b);          // returns D2-D1-1
        Move best = bestPair.toMove();

        /* check that quiescence found the capture */
        assertEquals("D2-D1-1", best.toAlgebraic());

        /* check that the returned score already equals the post-capture evaluation */
        Board after = Board.makeMove(bestPair, b.copy());
        int expected = Eval.evaluate(after);         // Eval already includes WIN value
        assertEquals(expected, score);
    }
}