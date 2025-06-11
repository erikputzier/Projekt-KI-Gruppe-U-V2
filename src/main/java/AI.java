import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class AI {
    private static final int MAX_PLIES = 64;   // depth guard
    static long nodesVisited = 0;
    static long cutoffs = 0;
    static double percent = 0.0;
    static int runs = 0;
    static double average;


    /**
     * Orders moves based on their estimated value to improve alpha-beta pruning efficiency.
     * Better moves are placed earlier in the list to increase the likelihood of cutoffs.
     *
     * @param moves            List of legal moves to be ordered
     * @param board            Current board state
     * @param maximizingPlayer Whether the current player is maximizing
     * @return Ordered list of moves
     */
    private static List<MovePair> orderMoves(List<MovePair> moves, Board board, boolean maximizingPlayer) {
        // Create a list to store moves with their scores
        List<ScoredMove> scoredMoves = new ArrayList<>();

        // Score each move by applying it and evaluating the resulting position
        for (MovePair move : moves) {
            Board newBoard = Board.makeMove(move, board.copy());
            int score = Eval.evaluate(newBoard);
            scoredMoves.add(new ScoredMove(move, score));
        }

        // Sort moves based on their scores
        if (maximizingPlayer) {
            // For maximizing player, higher scores are better
            scoredMoves.sort(Comparator.comparing(ScoredMove::score).reversed());
        } else {
            // For minimizing player, lower scores are better
            scoredMoves.sort(Comparator.comparing(ScoredMove::score));
        }

        // Extract just the moves from the scored moves
        List<MovePair> orderedMoves = new ArrayList<>();
        for (ScoredMove scoredMove : scoredMoves) {
            orderedMoves.add(scoredMove.move());
        }

        return orderedMoves;
    }

    /**
     * Record to store a move with its evaluation score
     */
    private record ScoredMove(MovePair move, int score) {
    }

    public static MovePair pickMove(Board board) {
        nodesVisited = 0;
        cutoffs = 0;
        List<MovePair> legalMoves = MoveGenerator.generateAllLegalMoves(board);
        boolean maximizingPlayer = board.getCurrentPlayer() != Player.BLUE;
        MovePair bestMove = null;
        int bestValue = maximizingPlayer ? Integer.MIN_VALUE : Integer.MAX_VALUE;

        // start global timer only ONCE
        long startTime = System.currentTimeMillis();
        int moveCounter = 0;
        long timeLimit = 2000;
        long branchLimit = (long) (timeLimit * 0.92 / legalMoves.size());

        // Order moves to evaluate better moves first
        List<MovePair> orderedMoves = orderMoves(legalMoves, board, maximizingPlayer);

        for (MovePair move : orderedMoves) {
            Board newBoard = Board.makeMove(move, board.copy());

            int eval = AI.minimaxAlphaBeta(newBoard, branchLimit);

            if (maximizingPlayer && eval > bestValue) {
                bestValue = eval;
                bestMove = move;
            } else if (!maximizingPlayer && eval < bestValue) {
                bestValue = eval;
                bestMove = move;
            }
            moveCounter++;
            // stop looping if we ran out of time
            if (System.currentTimeMillis() - startTime > 2000) break;
        }
        percent += 100.0 * cutoffs / nodesVisited;
        runs++;
        average = percent / runs;
        System.out.printf("Average alpha-beta cutoff ratio: %.1f%%%n", average);
        System.out.printf("αβ-cut ratio: %.1f%%%n", 100.0 * cutoffs / nodesVisited);
        System.out.println(moveCounter + " out of" + legalMoves.size() + " moves");
        System.out.println("Time: " + (System.currentTimeMillis() - startTime) + "ms");
        return bestMove;
    }

    public static int minimax(Board board, int depth, boolean maximizingPlayer) {

        /* ---------- hard stop: search horizon reached ------------------------- */
        if (depth == 0) return Eval.evaluate(board);

        /* ---------- game end check (the side that just moved) ------------------ */
        Player prev = (board.getCurrentPlayer() == Player.RED) ? Player.BLUE : Player.RED;
        if (Board.checkplayerWon(board, prev)) return Eval.evaluate(board);

        /* ---------- generate legal moves --------------------------------------- */
        List<MovePair> moves = MoveGenerator.generateAllLegalMoves(board);
        if (moves.isEmpty())                           // stalemate or no moves
            return Eval.evaluate(board);

        /* ---------- order moves to improve search efficiency ------------------ */
        List<MovePair> orderedMoves = orderMoves(moves, board, maximizingPlayer);

        /* ---------- recursive descent ------------------------------------------ */
        int best;
        if (maximizingPlayer) {
            best = Integer.MIN_VALUE;
            for (MovePair m : orderedMoves) {
                Board child = Board.makeMove(m, board.copy());  // safe copy
                int score = minimax(child, depth - 1, false);
                best = Math.max(best, score);
            }
        } else {                                       // minimizing player
            best = Integer.MAX_VALUE;
            for (MovePair m : orderedMoves) {
                Board child = Board.makeMove(m, board.copy());
                int score = minimax(child, depth - 1, true);
                best = Math.min(best, score);
            }
        }
        return best;
    }


    public static int minimaxAlphaBeta(Board root, long timeLimitMs) {              // convenience
        boolean rootIsMax = (root.getCurrentPlayer() == Player.RED);
        long start = System.currentTimeMillis();
        return minimaxAlphaBeta(root,                     /* board     */
                rootIsMax,                                /* max player*/
                Integer.MIN_VALUE, Integer.MAX_VALUE,     /* α, β      */
                start, timeLimitMs,                       /* timing    */
                0);                                       /* ply = 0   */
    }

    // -----------------------------------------------------------------------------
    //  Core recursive search
    // -----------------------------------------------------------------------------
    private static int minimaxAlphaBeta(Board board, boolean maximizingPlayer, int alpha, int beta, long startTime, long timeLimitMs, int ply) {

        /* ---------- hard stops: out of time OR too deep ------------------------ */
        if (System.currentTimeMillis() - startTime > timeLimitMs || ply >= MAX_PLIES) return Eval.evaluate(board);

        /* ---------- game-ending positions -------------------------------------- */
        Player prev = (board.getCurrentPlayer() == Player.RED) ? Player.BLUE : Player.RED;
        if (Board.checkplayerWon(board, prev))          // last mover just won
            return Eval.evaluate(board);

        /* ---------- enumerate legal moves -------------------------------------- */
        List<MovePair> moves = MoveGenerator.generateAllLegalMoves(board);
        if (moves.isEmpty())                            // stalemate or no moves
            return Eval.evaluate(board);

        /* ---------- order moves to improve alpha-beta efficiency --------------- */
        List<MovePair> orderedMoves = orderMoves(moves, board, maximizingPlayer);

        /* ---------- standard alpha–beta recursion ------------------------------ */
        int best;
        if (maximizingPlayer) {
            best = Integer.MIN_VALUE;
            for (MovePair m : orderedMoves) {
                Board child = Board.makeMove(m, board.copy());           // safe copy
                int score = minimaxAlphaBeta(child, false, alpha, beta, startTime, timeLimitMs, ply + 1);
                best = Math.max(best, score);
                alpha = Math.max(alpha, best);
                if (alpha >= beta) {
                    cutoffs++;
                    break;
                }                                 // cut-off
            }
        } else { // minimizing player
            best = Integer.MAX_VALUE;
            for (MovePair m : orderedMoves) {
                Board child = Board.makeMove(m, board.copy());
                int score = minimaxAlphaBeta(child, true, alpha, beta, startTime, timeLimitMs, ply + 1);
                best = Math.min(best, score);
                beta = Math.min(beta, best);
                if (beta <= alpha) {
                    cutoffs++;
                    break;
                }
            }
        }
        nodesVisited++;
        return best;
    }
}
