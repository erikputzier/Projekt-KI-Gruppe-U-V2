import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class MoveOrdering {
    // Maximum depth for killer moves storage
    private static final int MAX_PLY = 64;

    // Array to store killer moves for each ply (depth)
    // We store two killer moves per ply
    private static final MovePair[][] killerMoves = new MovePair[MAX_PLY][2];

    /**
     * Orders moves based on their estimated value to improve alpha-beta pruning efficiency.
     * Better moves are placed earlier in the list to increase the likelihood of cutoffs.
     * Also considers killer moves that have caused beta cutoffs at the same depth.
     *
     * @param moves            List of legal moves to be ordered
     * @param board            Current board state
     * @param maximizingPlayer Whether the current player is maximizing
     * @param ply              Current search depth (0 = root)
     * @return Ordered list of moves
     */
    public static List<MovePair> orderMoves(List<MovePair> moves, Board board, boolean maximizingPlayer, int ply) {
        // Create a list to store moves with their scores
        List<MoveOrdering.ScoredMove> scoredMoves = new ArrayList<>();

        // Score each move by applying it and evaluating the resulting position
        for (MovePair move : moves) {
            int score = 0;

            // Check if this move is a killer move at the current ply
            if (isKillerMove(move, ply)) {
                // Killer moves get a bonus score but still less than captures
                score += 900000; // High value but less than a winning position
            }

            // Evaluate the position after the move
            Board newBoard = Board.makeMove(move, board.copy());
            score += Eval.evaluate(newBoard);

            scoredMoves.add(new MoveOrdering.ScoredMove(move, score));
        }

        // Sort moves based on their scores
        if (maximizingPlayer) {
            // For maximizing player, higher scores are better
            scoredMoves.sort(Comparator.comparing(MoveOrdering.ScoredMove::score).reversed());
        } else {
            // For minimizing player, lower scores are better
            scoredMoves.sort(Comparator.comparing(MoveOrdering.ScoredMove::score));
        }

        // Extract just the moves from the scored moves
        List<MovePair> orderedMoves = new ArrayList<>();
        for (MoveOrdering.ScoredMove scoredMove : scoredMoves) {
            orderedMoves.add(scoredMove.move());
        }

        return orderedMoves;
    }

    /**
     * Checks if a move is a killer move at the given ply
     *
     * @param move The move to check
     * @param ply  The current search depth
     * @return True if the move is a killer move, false otherwise
     */
    private static boolean isKillerMove(MovePair move, int ply) {
        if (ply >= MAX_PLY) return false;

        return move.equals(killerMoves[ply][0]) || move.equals(killerMoves[ply][1]);
    }

    /**
     * Updates the killer moves table when a beta cutoff occurs
     *
     * @param move The move that caused the cutoff
     * @param ply  The current search depth
     */
    public static void updateKillerMove(MovePair move, int ply) {
        if (ply >= MAX_PLY) return;

        // Don't store the same killer move twice
        if (!move.equals(killerMoves[ply][0])) {
            // Shift the existing killer move to the second slot
            killerMoves[ply][1] = killerMoves[ply][0];
            // Store the new killer move in the first slot
            killerMoves[ply][0] = move;
        }
    }

    /**
     * Resets all killer moves
     */
    public static void resetKillerMoves() {
        for (int i = 0; i < MAX_PLY; i++) {
            killerMoves[i][0] = null;
            killerMoves[i][1] = null;
        }
    }

    /**
     * Record to store a move with its evaluation score
     */
    private record ScoredMove(MovePair move, int score) {
    }
}