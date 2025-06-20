import org.junit.Test;

import static org.junit.Assert.*;

import java.util.List;
import java.lang.reflect.Method;

public class MoveOrderingTest {

    @Test
    public void testMoveOrderingImprovesCutoffs() {
        // Create a simple board position with FEN notation
        // Format: position player
        // R = red guard, B = blue guard, r1 = red tower height 1, b1 = blue tower height 1, etc.
        Board board = new Board("R1/7/7/7/7/7/B1 r");

        // Get the legal moves
        List<MovePair> legalMoves = MoveGenerator.generateAllLegalMoves(board);

        // Make sure we have some legal moves
        assertFalse("Should have legal moves", legalMoves.isEmpty());

        // Use reflection to access the private orderMoves method
        try {
            Method orderMovesMethod = MoveOrdering.class.getDeclaredMethod("orderMoves", List.class, Board.class, boolean.class);
            orderMovesMethod.setAccessible(true);

            // Call the orderMoves method
            boolean maximizingPlayer = board.getCurrentPlayer() == Player.RED;
            @SuppressWarnings("unchecked") List<MovePair> orderedMoves = (List<MovePair>) orderMovesMethod.invoke(null, legalMoves, board, maximizingPlayer);

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

            System.out.println("[DEBUG_LOG] Move ordering test passed successfully");

        } catch (Exception e) {
            fail("Exception during test: " + e.getMessage());
        }
    }
}