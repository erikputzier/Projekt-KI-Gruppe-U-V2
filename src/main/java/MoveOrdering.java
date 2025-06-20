import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class MoveOrdering {
    /**
     * Orders moves based on their estimated value to improve alpha-beta pruning efficiency.
     * Better moves are placed earlier in the list to increase the likelihood of cutoffs.
     *
     * @param moves            List of legal moves to be ordered
     * @param board            Current board state
     * @param maximizingPlayer Whether the current player is maximizing
     * @return Ordered list of moves
     */
    public static List<MovePair> orderMoves(List<MovePair> moves, Board board, boolean maximizingPlayer) {
        // Create a list to store moves with their scores
        List<MoveOrdering.ScoredMove> scoredMoves = new ArrayList<>();

        // Score each move by applying it and evaluating the resulting position
        for (MovePair move : moves) {
            Board newBoard = Board.makeMove(move, board.copy());
            int score = Eval.evaluate(newBoard);
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
     * Record to store a move with its evaluation score
     */
    private record ScoredMove(MovePair move, int score) {
    }
}