import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;

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
        Board board = new Board("R1/7/7/7/7/7/B1 r");

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
    public void testMultipleKillerMoves() {
        // Create a simple board position
        Board board = new Board("R1/7/7/7/7/7/B1 r");

        // Get legal moves
        List<MovePair> legalMoves = MoveGenerator.generateAllLegalMoves(board);
        assertFalse("Should have legal moves", legalMoves.isEmpty());

        // Need at least 2 moves for this test
        if (legalMoves.size() >= 2) {
            // First, let's get the ordering without any killer moves
            boolean maximizingPlayer = board.getCurrentPlayer() == Player.RED;
            List<MovePair> orderedMovesNoKiller = MoveOrdering.orderMoves(legalMoves, board, maximizingPlayer, 0);

            // Now, let's select two moves that are not already at the top of the ordering
            // This ensures that the killer move bonus will actually change the ordering
            MovePair killerMove1 = null;
            MovePair killerMove2 = null;

            // Try to find moves that are not already at the top
            for (int i = legalMoves.size() - 1; i >= 0; i--) {
                if (killerMove1 == null) {
                    killerMove1 = legalMoves.get(i);
                } else if (killerMove2 == null && !legalMoves.get(i).equals(killerMove1)) {
                    killerMove2 = legalMoves.get(i);
                    break;
                }
            }

            // If we couldn't find two distinct moves, use the first two
            if (killerMove1 == null || killerMove2 == null) {
                killerMove1 = legalMoves.get(0);
                killerMove2 = legalMoves.size() > 1 ? legalMoves.get(1) : killerMove1;
            }

            System.out.println("[DEBUG_LOG] Selected killer moves: " + killerMove1 + " and " + killerMove2);

            int testPly = 3;

            // Update them as killer moves
            MoveOrdering.updateKillerMove(killerMove1, testPly);
            MoveOrdering.updateKillerMove(killerMove2, testPly);

            // Order the moves
            List<MovePair> orderedMoves = MoveOrdering.orderMoves(legalMoves, board, maximizingPlayer, testPly);

            // Print the ordered moves for debugging
            System.out.println("[DEBUG_LOG] Ordered moves: " + orderedMoves);

            // Check if the killer moves are in the correct order
            int killerMove1Index = orderedMoves.indexOf(killerMove1);
            int killerMove2Index = orderedMoves.indexOf(killerMove2);

            // The most recent killer move (killerMove2) should come before killerMove1
            assertTrue("Most recent killer move should come before the older killer move", 
                      killerMove2Index < killerMove1Index);

            // Both killer moves should be near the top of the ordering
            assertTrue("Most recent killer move should be near the top", killerMove2Index <= 1);
            assertTrue("Older killer move should be near the top", killerMove1Index <= 2);

            System.out.println("[DEBUG_LOG] Multiple killer moves test passed successfully");
        } else {
            System.out.println("[DEBUG_LOG] Skipping multiple killer moves test - not enough legal moves");
        }
    }

    @Test
    public void testResetKillerMoves() {
        // Create a simple board position
        Board board = new Board("R1/7/7/7/7/7/B1 r");

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

                    if ((maximizingPlayer && moveEval > killerEval) || 
                        (!maximizingPlayer && moveEval < killerEval)) {
                        foundBetterMove = true;
                        break;
                    }
                }
            }

            // If we found a move with better evaluation but the killer move is still first,
            // then the reset didn't work
            assertFalse("Killer move should not be ordered first after reset if better moves exist", 
                       foundBetterMove);

            System.out.println("[DEBUG_LOG] Reset killer moves test passed successfully - killer move is best by evaluation");
        }
    }

    @Test
    public void testKillerMoveUpdateMechanism() {
        // Create a simple board position
        Board board = new Board("R1/7/7/7/7/7/B1 r");

        // Get legal moves
        List<MovePair> legalMoves = MoveGenerator.generateAllLegalMoves(board);
        assertFalse("Should have legal moves", legalMoves.isEmpty());

        // Need at least 3 moves for this test
        if (legalMoves.size() >= 3) {
            int testPly = 5;

            // Update three killer moves in sequence
            MovePair move1 = legalMoves.get(0);
            MovePair move2 = legalMoves.get(1);
            MovePair move3 = legalMoves.get(2);

            MoveOrdering.updateKillerMove(move1, testPly);

            // After first update, move1 should be the first killer move
            boolean maximizingPlayer = board.getCurrentPlayer() == Player.RED;
            List<MovePair> orderedMoves1 = MoveOrdering.orderMoves(legalMoves, board, maximizingPlayer, testPly);
            assertEquals("First killer move should be ordered first", move1, orderedMoves1.getFirst());

            // Update with second move
            MoveOrdering.updateKillerMove(move2, testPly);

            // After second update, move2 should be first, move1 should be second
            List<MovePair> orderedMoves2 = MoveOrdering.orderMoves(legalMoves, board, maximizingPlayer, testPly);
            assertEquals("Second killer move should be ordered first", move2, orderedMoves2.getFirst());

            // Update with third move
            MoveOrdering.updateKillerMove(move3, testPly);

            // After third update, move3 should be first, move2 should be second, move1 should be out
            List<MovePair> orderedMoves3 = MoveOrdering.orderMoves(legalMoves, board, maximizingPlayer, testPly);
            assertEquals("Third killer move should be ordered first", move3, orderedMoves3.getFirst());

            System.out.println("[DEBUG_LOG] Killer move update mechanism test passed successfully");
        } else {
            System.out.println("[DEBUG_LOG] Skipping killer move update mechanism test - not enough legal moves");
        }
    }

    @Test
    public void testKillerMovesAtDifferentPlies() {
        // Create a simple board position
        Board board = new Board("R1/7/7/7/7/7/B1 r");

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
