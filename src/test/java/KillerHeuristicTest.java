import org.junit.Assume;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.*;

public class KillerHeuristicTest {

    @Before
    public void setUp() {
        // Reset killer moves before each test
        MoveOrdering.resetKillerMoves();
    }

    // Helper to build a simple, non-trivial position with both sides having legal moves
    private Board makeTestBoard(Player toMove) {
        // e.g. "3r13/7/7/7/7/7/3b13 r" or replace r/b
        return new Board("3r13/7/7/7/7/7/3b13 " + (toMove == Player.RED ? 'r' : 'b'));
    }

    @Test
    public void testResetClearsAllPlySlots() {
        Board b = makeTestBoard(Player.RED);
        List<MovePair> moves = MoveGenerator.generateAllLegalMoves(b);
        MovePair m = moves.getLast();
        MoveOrdering.updateKillerMove(m, 0);
        // now reset
        MoveOrdering.resetKillerMoves();
        List<MovePair> ordered = MoveOrdering.orderMoves(moves, b, true, 0);
        // m should not be first anymore
        assertNotEquals(m, ordered.getFirst());
    }

    @Test
    public void testKillerMovePromotion_BothSides() {
        int[] plies = {0, 1, 2, 3};
        for (int ply : plies) {
            for (Player side : Player.values()) {
                Board board = makeTestBoard(side);
                List<MovePair> moves = MoveGenerator.generateAllLegalMoves(board);

                // require at least two distinct moves
                Assume.assumeTrue("Need at least two moves for " + side + " at ply " + ply, moves.size() >= 2);

                boolean maximizing = (board.getCurrentPlayer() == Player.RED);

                // baseline ordering
                List<MovePair> beforeOrdering = MoveOrdering.orderMoves(new ArrayList<>(moves), board, maximizing, ply);
                int lastIndex = beforeOrdering.size() - 1;
                MovePair killer = beforeOrdering.get(lastIndex);

                // ensure killer isn't already first
                int idxBefore = beforeOrdering.indexOf(killer);
                assertNotEquals("Candidate killer must not already be first for " + side + " at ply " + ply, 0, idxBefore);

                // install killer and re-order
                MoveOrdering.updateKillerMove(killer, ply);
                List<MovePair> afterOrdering = MoveOrdering.orderMoves(moves, board, maximizing, ply);
                int idxAfter = afterOrdering.indexOf(killer);

                // core assertion
                assertTrue("Killer not promoted for " + side + " at ply " + ply, idxAfter < idxBefore);
            }
        }
    }

    @Test
    public void testKillerMoveOnlyAffectsSamePly() {
        // Choose: one plyA = 1, another plyB = 2
        int plyA = 1;
        int plyB = 2;

        // Setup board, moves, choose killer
        Board board = makeTestBoard(Player.RED);
        List<MovePair> moves = MoveGenerator.generateAllLegalMoves(board);
        MovePair killer = moves.removeLast();

        // Add killer back to the list
        List<MovePair> movesWithKiller = new ArrayList<>(moves);
        movesWithKiller.add(killer);

        // Record positions before update
        boolean maximizing = board.getCurrentPlayer() == Player.RED;
        List<MovePair> beforeA = MoveOrdering.orderMoves(movesWithKiller, board, maximizing, plyA);
        List<MovePair> beforeB = MoveOrdering.orderMoves(movesWithKiller, board, maximizing, plyB);

        int posA_before = beforeA.indexOf(killer);
        int posB_before = beforeB.indexOf(killer);

        // Update killer move at plyA only
        MoveOrdering.updateKillerMove(killer, plyA);

        // Record positions after update
        List<MovePair> afterA = MoveOrdering.orderMoves(movesWithKiller, board, maximizing, plyA);
        List<MovePair> afterB = MoveOrdering.orderMoves(movesWithKiller, board, maximizing, plyB);

        int posA_after = afterA.indexOf(killer);
        int posB_after = afterB.indexOf(killer);

        // Asserts
        assertTrue("Should promote at plyA", posA_after < posA_before);
        assertEquals("Should not affect plyB", posB_before, posB_after);
    }

    @Test
    public void testSecondKillerSlotOverwritesFirst() {
        // Procedure for a single ply = 0 and one side (e.g. RED)
        int ply = 0;
        Board board = makeTestBoard(Player.RED);

        List<MovePair> moves = MoveGenerator.generateAllLegalMoves(board);

        // Choose killer1 = moves.getLast(), then killer2 = moves.get(moves.size()-2)
        MovePair killer1 = moves.getLast();
        MovePair killer2 = moves.get(moves.size() - 2);

        // Update killer1 as a killer move
        MoveOrdering.updateKillerMove(killer1, ply);

        // Verify killer1 is in first slot by ordering
        boolean maximizing = board.getCurrentPlayer() == Player.RED;
        List<MovePair> firstOrdering = MoveOrdering.orderMoves(moves, board, maximizing, ply);
        assertEquals("Killer1 should be first after update", killer1, firstOrdering.getFirst());

        // Update killer2 as a killer move
        MoveOrdering.updateKillerMove(killer2, ply);

        // Now killer2 should occupy slot 0, killer1 should demote to slot 1
        List<MovePair> secondOrdering = MoveOrdering.orderMoves(moves, board, maximizing, ply);
        assertEquals("Killer2 should be first after update", killer2, secondOrdering.getFirst());

        // Remove killer2 from the list and order again
        List<MovePair> movesWithoutKiller2 = new ArrayList<>(moves);
        movesWithoutKiller2.remove(killer2);

        List<MovePair> thirdOrdering = MoveOrdering.orderMoves(movesWithoutKiller2, board, maximizing, ply);
        assertEquals("Killer1 should be promoted when killer2 is removed", killer1, thirdOrdering.getFirst());
    }

    @Test
    public void testNonKillerRelativeOrderUnchanged() {
        // Setup moves, pick killer
        Board board = makeTestBoard(Player.RED);
        List<MovePair> moves = MoveGenerator.generateAllLegalMoves(board);
        MovePair killer = moves.getLast();

        // Record the sequence of all non-killer moves in beforeList
        boolean maximizing = board.getCurrentPlayer() == Player.RED;
        List<MovePair> beforeOrdering = MoveOrdering.orderMoves(moves, board, maximizing, 0);

        List<MovePair> beforeList = new ArrayList<>(beforeOrdering);
        beforeList.remove(killer);

        // Update killer move
        MoveOrdering.updateKillerMove(killer, 0);

        // After ordering, extract non-killer moves in afterList
        List<MovePair> afterOrdering = MoveOrdering.orderMoves(moves, board, maximizing, 0);
        List<MovePair> afterList = new ArrayList<>(afterOrdering);
        afterList.remove(killer);

        // Assert
        assertEquals("Non-killer moves should keep relative order", beforeList, afterList);
    }

    @Test
    public void testDefaultOrderingWithoutKiller() {
        // Setup board, moves
        Board board = makeTestBoard(Player.RED);
        List<MovePair> moves = MoveGenerator.generateAllLegalMoves(board);

        // Call orderMoves(moves, board, maximizing, ply) once → firstOrder
        boolean maximizing = board.getCurrentPlayer() == Player.RED;
        List<MovePair> firstOrder = MoveOrdering.orderMoves(moves, board, maximizing, 0);

        // Call it again without any update → secondOrder
        List<MovePair> secondOrder = MoveOrdering.orderMoves(moves, board, maximizing, 0);

        // Assert
        assertEquals("Ordering should be stable without killers", firstOrder, secondOrder);
    }
}