import java.util.List;

public class AI {
    private static final int MAX_PLIES = 64;   // depth guard
    private static long startTime;

    public static MovePair pickMove(Board board) {
        List<MovePair> legalMoves = MoveGenerator.generateAllLegalMoves(board);
        boolean maximizingPlayer = board.getCurrentPlayer() != Player.BLUE;
        MovePair bestMove = null;
        int bestValue = maximizingPlayer ? Integer.MIN_VALUE : Integer.MAX_VALUE;

        // start global timer only ONCE
        startTime = System.currentTimeMillis();
        int moveCounter = 0;
        long timeLimit = 2000;
        long branchLimit = (long) (timeLimit * 0.92 / legalMoves.size());
        for (MovePair move : legalMoves) {
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

        /* ---------- recursive descent ------------------------------------------ */
        int best;
        if (maximizingPlayer) {
            best = Integer.MIN_VALUE;
            for (MovePair m : moves) {
                Board child = Board.makeMove(m, board.copy());  // safe copy
                int score = minimax(child, depth - 1, false);
                best = Math.max(best, score);
            }
        } else {                                       // minimizing player
            best = Integer.MAX_VALUE;
            for (MovePair m : moves) {
                Board child = Board.makeMove(m, board.copy());
                int score = minimax(child, depth - 1, true);
                best = Math.min(best, score);
            }
        }
        return best;
    }


    public static int minimaxAlphaBeta(Board root, long timeLimitMs) {              // convenience
        boolean rootIsMax = (root.getCurrentPlayer() == Player.RED);
        return minimaxAlphaBeta(root,                     /* board     */
                rootIsMax,                                /* max player*/
                Integer.MIN_VALUE, Integer.MAX_VALUE,     /* α, β      */
                startTime, timeLimitMs,                       /* timing    */
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

        /* ---------- standard alpha–beta recursion ------------------------------ */
        int best;
        if (maximizingPlayer) {
            best = Integer.MIN_VALUE;
            for (MovePair m : moves) {
                Board child = Board.makeMove(m, board.copy());           // safe copy
                int score = minimaxAlphaBeta(child, false, alpha, beta, startTime, timeLimitMs, ply + 1);
                best = Math.max(best, score);
                alpha = Math.max(alpha, best);
                if (alpha >= beta) break;                                // cut-off
            }
        } else { // minimizing player
            best = Integer.MAX_VALUE;
            for (MovePair m : moves) {
                Board child = Board.makeMove(m, board.copy());
                int score = minimaxAlphaBeta(child, true, alpha, beta, startTime, timeLimitMs, ply + 1);
                best = Math.min(best, score);
                beta = Math.min(beta, best);
                if (beta <= alpha) break;
            }
        }
        return best;
    }
}
