import org.junit.Assume;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.List;

public class MoveOrderingTest {

    @Before
    public void setUp() {
        // Reset killer moves before each test
        MoveOrdering.resetKillerMoves();
    }

    @Test
    public void testBasicMoveOrdering() {
        // Create a simple board position with FEN notation
        // Format: position player
        // R = red guard, B = blue guard, r1 = red tower height 1, b1 = blue tower height 1, etc.
        Board board = new Board("R1/7/7/7/7/7/B1 r");

        // Get the legal moves
        List<MovePair> legalMoves = MoveGenerator.generateAllLegalMoves(board);

        // Make sure we have some legal moves
        assertFalse("Should have legal moves", legalMoves.isEmpty());

        // Call the orderMoves method directly (no need for reflection)
        boolean maximizingPlayer = board.getCurrentPlayer() == Player.RED;
        List<MovePair> orderedMoves = MoveOrdering.orderMoves(legalMoves, board, maximizingPlayer, 0);

        // Verify that we got back the same number of moves
        assertEquals("Should return same number of moves", legalMoves.size(), orderedMoves.size());

        // Verify that the ordered moves contain all the original moves
        for (MovePair move : legalMoves) {
            assertTrue("Ordered moves should contain all original moves", orderedMoves.contains(move));
        }

        // If maximizing, first move should have highest evaluation
        if (maximizingPlayer && orderedMoves.size() > 1) {
            MovePair firstMove = orderedMoves.getFirst();
            MovePair lastMove = orderedMoves.getLast();

            Board firstBoard = Board.makeMove(firstMove, board.copy());
            Board lastBoard = Board.makeMove(lastMove, board.copy());

            int firstEval = Eval.evaluate(firstBoard);
            int lastEval = Eval.evaluate(lastBoard);

            assertTrue("First move should have higher evaluation than last move for maximizing player", firstEval >= lastEval);
        }

        // If minimizing, first move should have lowest evaluation
        if (!maximizingPlayer && orderedMoves.size() > 1) {
            MovePair firstMove = orderedMoves.getFirst();
            MovePair lastMove = orderedMoves.getLast();

            Board firstBoard = Board.makeMove(firstMove, board.copy());
            Board lastBoard = Board.makeMove(lastMove, board.copy());

            int firstEval = Eval.evaluate(firstBoard);
            int lastEval = Eval.evaluate(lastBoard);

            assertTrue("First move should have lower evaluation than last move for minimizing player", firstEval <= lastEval);
        }

        System.out.println("[DEBUG_LOG] Basic move ordering test passed successfully");
    }

    @Test
    public void testKillerMoveOrdering() {
        // Create a simple board position
        Board board = new Board("3r13/7/7/7/7/7/3b13 r");

        // Get legal moves
        List<MovePair> legalMoves = MoveGenerator.generateAllLegalMoves(board);
        assertFalse("Should have legal moves", legalMoves.isEmpty());

        // Select a move to be the killer move
        MovePair killerMove = legalMoves.getFirst();

        // Update it as a killer move at ply 2
        int testPly = 2;
        MoveOrdering.updateKillerMove(killerMove, testPly);

        // Order the moves
        boolean maximizingPlayer = board.getCurrentPlayer() == Player.RED;
        List<MovePair> orderedMoves = MoveOrdering.orderMoves(legalMoves, board, maximizingPlayer, testPly);

        // The killer move should be ordered first
        assertEquals("Killer move should be ordered first", killerMove, orderedMoves.getFirst());

        System.out.println("[DEBUG_LOG] Killer move ordering test passed successfully");
    }

    @Test
    public void testResetKillerMoves() {
        // Create a simple board position
        Board board = new Board("3r13/7/7/7/7/7/3b13 r");

        // Get legal moves
        List<MovePair> legalMoves = MoveGenerator.generateAllLegalMoves(board);
        assertFalse("Should have legal moves", legalMoves.isEmpty());

        // Select a move to be the killer move
        MovePair killerMove = legalMoves.getFirst();

        // Update it as a killer move
        int testPly = 4;
        MoveOrdering.updateKillerMove(killerMove, testPly);

        // Order the moves with the killer move
        boolean maximizingPlayer = board.getCurrentPlayer() == Player.RED;
        List<MovePair> orderedMovesWithKiller = MoveOrdering.orderMoves(legalMoves, board, maximizingPlayer, testPly);

        // The killer move should be ordered first
        assertEquals("Killer move should be ordered first", killerMove, orderedMovesWithKiller.getFirst());

        // Reset killer moves
        MoveOrdering.resetKillerMoves();

        // Order the moves again
        List<MovePair> orderedMovesAfterReset = MoveOrdering.orderMoves(legalMoves, board, maximizingPlayer, testPly);

        // Now the ordering should be based only on evaluation
        // We'll compare the first moves of both orderings
        if (!orderedMovesAfterReset.getFirst().equals(killerMove)) {
            // If the first move is different, the killer move was successfully reset
            System.out.println("[DEBUG_LOG] Reset killer moves test passed successfully - first move changed");
        } else {
            // If the first move is still the killer move, we need to check if it's because of evaluation
            Board killerBoard = Board.makeMove(killerMove, board.copy());
            int killerEval = Eval.evaluate(killerBoard);

            // Check if any other move has a better evaluation
            boolean foundBetterMove = false;
            for (MovePair move : legalMoves) {
                if (!move.equals(killerMove)) {
                    Board moveBoard = Board.makeMove(move, board.copy());
                    int moveEval = Eval.evaluate(moveBoard);

                    if ((maximizingPlayer && moveEval > killerEval) || (!maximizingPlayer && moveEval < killerEval)) {
                        foundBetterMove = true;
                        break;
                    }
                }
            }

            // If we found a move with better evaluation but the killer move is still first,
            // then the reset didn't work
            assertFalse("Killer move should not be ordered first after reset if better moves exist", foundBetterMove);

            System.out.println("[DEBUG_LOG] Reset killer moves test passed successfully - killer move is best by evaluation");
        }
    }


    @Test
    public void testKillerMoveUpdateMechanism() {
        // 1) Set up
        Board board = new Board("3r13/7/7/7/7/7/3b13 r");
        List<MovePair> legalMoves = MoveGenerator.generateAllLegalMoves(board);
        assertFalse("Should have legal moves", legalMoves.isEmpty());

        int testPly = 5;
        boolean maximizing = (board.getCurrentPlayer() == Player.RED);

        // 2) Require at least 3 legal moves
        Assume.assumeTrue("Need at least 3 legal moves", legalMoves.size() >= 3);

        // 3) Build a baseline ordering so that the last entries are "quiet" moves
        List<MovePair> baseline = MoveOrdering.orderMoves(new ArrayList<>(legalMoves), board, maximizing, testPly);
        int n = baseline.size();
        Assume.assumeTrue("Baseline must have at least 3 moves", n >= 3);

        // 4) Pick the last three moves (quiet moves) as killers
        MovePair killer1 = baseline.get(n - 1);
        MovePair killer2 = baseline.get(n - 2);
        MovePair killer3 = baseline.get(n - 3);

        // 5) Record their starting positions
        int pos1_before = baseline.indexOf(killer1);
        int pos2_before = baseline.indexOf(killer2);
        int pos3_before = baseline.indexOf(killer3);
        assertTrue("killer1 should start after killer2", pos1_before > pos2_before);
        assertTrue("killer2 should start after killer3", pos2_before > pos3_before);

        // 6) Update killer1 and verify it moves forward
        MoveOrdering.updateKillerMove(killer1, testPly);
        List<MovePair> after1 = MoveOrdering.orderMoves(legalMoves, board, maximizing, testPly);
        int pos1_after1 = after1.indexOf(killer1);
        assertTrue("killer1 not promoted after first update", pos1_after1 < pos1_before);

        // 7) Update killer2: it should now outrank killer1
        MoveOrdering.updateKillerMove(killer2, testPly);
        List<MovePair> after2 = MoveOrdering.orderMoves(legalMoves, board, maximizing, testPly);
        int pos2_after2 = after2.indexOf(killer2);
        int pos1_after2 = after2.indexOf(killer1);
        assertTrue("killer2 not promoted over killer1 after second update", pos2_after2 < pos1_after2);

        // 8) Update killer3: it should outrank killer2, and killer1 should be evicted from the top two
        MoveOrdering.updateKillerMove(killer3, testPly);
        List<MovePair> after3 = MoveOrdering.orderMoves(legalMoves, board, maximizing, testPly);
        int pos3_after3 = after3.indexOf(killer3);
        int pos2_after3 = after3.indexOf(killer2);
        int pos1_after3 = after3.indexOf(killer1);

        assertTrue("killer3 not promoted over killer2 after third update", pos3_after3 < pos2_after3);
        assertTrue("killer1 should be evicted from the top two after third update", pos1_after3 > pos2_after3);
    }

    @Test
    public void testKillerMovesAtDifferentPlies() {
        // Create a simple board position
        Board board = new Board("3r13/7/7/7/7/7/3b13 r");

        // Get legal moves
        List<MovePair> legalMoves = MoveGenerator.generateAllLegalMoves(board);
        assertFalse("Should have legal moves", legalMoves.isEmpty());

        // Select a move to be the killer move at different plies
        MovePair killerMove1 = legalMoves.get(0);
        MovePair killerMove2 = legalMoves.size() > 1 ? legalMoves.get(1) : killerMove1;

        // Update killer moves at different plies
        int ply1 = 1;
        int ply2 = 2;
        MoveOrdering.updateKillerMove(killerMove1, ply1);
        MoveOrdering.updateKillerMove(killerMove2, ply2);

        // Order the moves at ply1
        boolean maximizingPlayer = board.getCurrentPlayer() == Player.RED;
        List<MovePair> orderedMovesAtPly1 = MoveOrdering.orderMoves(legalMoves, board, maximizingPlayer, ply1);

        // Order the moves at ply2
        List<MovePair> orderedMovesAtPly2 = MoveOrdering.orderMoves(legalMoves, board, maximizingPlayer, ply2);

        // The killer move at ply1 should be ordered first at ply1
        assertEquals("Killer move at ply1 should be ordered first at ply1", killerMove1, orderedMovesAtPly1.getFirst());

        // The killer move at ply2 should be ordered first at ply2
        assertEquals("Killer move at ply2 should be ordered first at ply2", killerMove2, orderedMovesAtPly2.getFirst());

        System.out.println("[DEBUG_LOG] Killer moves at different plies test passed successfully");
    }
}
