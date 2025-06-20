import java.util.ArrayList;
import java.util.List;

public class AI {
    private static int max_plies = 64;   // depth guard
    static long positionsSearched = 0;
    static long cutoffs = 0;
    static long ttHits = 0; // Counter for transposition table hits
    static double percent = 0.0;
    static int runs = 0;
    static int nodesVisited = 0;
    static List<Integer> searchDepths = new ArrayList<>();
    static double average;

    private static final TranspositionTable transpositionTable = new TranspositionTable();

    public static MovePair pickMove(Board board) {
        max_plies = 0;
        nodesVisited = 0;
        positionsSearched = 0;
        cutoffs = 0;
        ttHits = 0; // Reset TT hits counter
        //transpositionTable.clear(); // Clear TT before new move search

        List<MovePair> legalMoves = MoveGenerator.generateAllLegalMoves(board);
        boolean maximizingPlayer = board.getCurrentPlayer() != Player.BLUE;
        MovePair bestMove = null;
        int bestValue = maximizingPlayer ? Integer.MIN_VALUE : Integer.MAX_VALUE;

        // start global timer only ONCE
        long startTime = System.currentTimeMillis();
        int moveCounter = 0;
        long baseTimeLimit = 2000;
        long timeLimit = TimeManager.computeTimeBudget(board, legalMoves, baseTimeLimit);
        long branchLimit = (long) (timeLimit * 0.92 / legalMoves.size()); // war timeLimit * 0.92 / legalMoves.size()
        System.out.println("Time Limit: " + timeLimit);
        // Order moves to evaluate better moves first
        List<MovePair> orderedMoves = MoveOrdering.orderMoves(legalMoves, board, maximizingPlayer);

        while ((System.currentTimeMillis() - startTime) < timeLimit) {
            nodesVisited = 0;
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
                if (System.currentTimeMillis() - startTime > timeLimit) break;
            }
            max_plies++;
        }
        percent += 100.0 * cutoffs / nodesVisited;
        runs++;
        average = percent / runs;
        System.out.printf("Average alpha-beta cutoff ratio: %.1f%%%n", average);
        System.out.printf("αβ-cut ratio: %.1f%%%n", 100.0 * cutoffs / nodesVisited);
        System.out.printf("TT hits: %d%n", ttHits); // Print TT hits
        System.out.println(STR."\{moveCounter} out of\{legalMoves.size()} moves");
        System.out.println(STR."Time: \{System.currentTimeMillis() - startTime}ms");
        System.out.println(STR."Nodes Visited: \{nodesVisited}");
        System.out.println(STR."Suchtiefe:\{max_plies}");
        return bestMove;
    }

    public static int minimax(Board board, int depth, boolean maximizingPlayer) {

        /* ---------- hard stop: search horizon reached ------------------------- */
        if (depth == 0) return Eval.evaluate(board);

        long zobristHash = ZobristHashing.computeHash(board);
        TranspositionTable.TTEntry ttEntry = transpositionTable.retrieve(zobristHash);

        if (ttEntry != null && ttEntry.depth >= depth) {
            ttHits++;
            if (ttEntry.entryType == TranspositionTable.EXACT_SCORE) {
                return ttEntry.score;
            }
        }

        /* ---------- game end check (the side that just moved) ------------------ */
        Player prev = (board.getCurrentPlayer() == Player.RED) ? Player.BLUE : Player.RED;
        if (Board.checkplayerWon(board, prev)) return Eval.evaluate(board);

        /* ---------- generate legal moves --------------------------------------- */
        List<MovePair> moves = MoveGenerator.generateAllLegalMoves(board);
        if (moves.isEmpty())                           // stalemate or no moves
            return Eval.evaluate(board);

        /* ---------- order moves to improve search efficiency ------------------ */
        List<MovePair> orderedMoves = MoveOrdering.orderMoves(moves, board, maximizingPlayer);

        /* ---------- recursive descent ------------------------------------------ */
        int best;
        MovePair bestMoveForTT = null;
        if (maximizingPlayer) {
            best = Integer.MIN_VALUE;
            for (MovePair m : orderedMoves) {
                Board child = Board.makeMove(m, board.copy());  // safe copy
                int score = minimax(child, depth - 1, false);
                if (score > best) {
                    best = score;
                    bestMoveForTT = m;
                }
            }
        } else {                                       // minimizing player
            best = Integer.MAX_VALUE;
            for (MovePair m : orderedMoves) {
                Board child = Board.makeMove(m, board.copy());
                int score = minimax(child, depth - 1, true);
                if (score < best) {
                    best = score;
                    bestMoveForTT = m;
                }
            }
        }
        transpositionTable.store(zobristHash, best, depth, TranspositionTable.EXACT_SCORE, bestMoveForTT);
        return best;
    }


    public static int minimaxAlphaBeta(Board root, long timeLimitMs) {              // convenience
        boolean rootIsMax = (root.getCurrentPlayer() == Player.RED);
        long start = System.currentTimeMillis();
        return minimaxAlphaBetaNoTT(root,                     /* board     */
                rootIsMax,                                /* max player*/
                Integer.MIN_VALUE, Integer.MAX_VALUE,     /* α, β      */
                start, timeLimitMs,                       /* timing    */
                0);                                       /* ply = 0   */
    }

    // -----------------------------------------------------------------------------
    //  Core recursive search
    // -----------------------------------------------------------------------------
    private static int minimaxAlphaBeta(Board board, boolean maximizingPlayer, int alpha, int beta, long startTime, long timeLimitMs, int ply) {
        nodesVisited++;
        /* ---------- Zobrist Hashing and Transposition Table Lookup ------------- */
        long zobristHash = ZobristHashing.computeHash(board);
        TranspositionTable.TTEntry ttEntry = transpositionTable.retrieve(zobristHash);
        int originalAlpha = alpha; // Store original alpha for TT storing
        int originalBeta = beta;   // Store original beta for TT storing

        if (ttEntry != null && ttEntry.depth >= (max_plies - ply)) { // Compare with remaining depth
            ttHits++;
            if (ttEntry.entryType == TranspositionTable.EXACT_SCORE) {
                return ttEntry.score;
            } else if (ttEntry.entryType == TranspositionTable.LOWER_BOUND) {
                alpha = Math.max(alpha, ttEntry.score);
            } else if (ttEntry.entryType == TranspositionTable.UPPER_BOUND) {
                beta = Math.min(beta, ttEntry.score);
            }
            if (alpha >= beta) {
                return ttEntry.score; // Or alpha/beta depending on bound type, but score should be fine for cutoffs
            }
        }

        /* ---------- hard stops: out of time OR too deep ------------------------ */
        if (System.currentTimeMillis() - startTime > timeLimitMs || ply >= max_plies) {
            return Eval.evaluate(board);
        }

        /* ---------- game-ending positions -------------------------------------- */
        Player prev = (board.getCurrentPlayer() == Player.RED) ? Player.BLUE : Player.RED;
        if (Board.checkplayerWon(board, prev)) {         // last mover just won
            return Eval.evaluate(board);
        }

        /* ---------- enumerate legal moves -------------------------------------- */
        List<MovePair> moves = MoveGenerator.generateAllLegalMoves(board);
        if (moves.isEmpty()) {                            // stalemate or no moves
            return Eval.evaluate(board);
        }

        /* ---------- order moves to improve alpha-beta efficiency --------------- */
        List<MovePair> orderedMoves = MoveOrdering.orderMoves(moves, board, maximizingPlayer);
        // If a best move was found in TT, try it first
        if (ttEntry != null && ttEntry.bestMove != null) {
            orderedMoves.remove(ttEntry.bestMove); // Remove if present to avoid duplicate
            orderedMoves.add(0, ttEntry.bestMove); // Add to the front
        }


        /* ---------- standard alpha–beta recursion ------------------------------ */
        int bestScore;
        MovePair bestMoveForTT = null;

        if (maximizingPlayer) {
            bestScore = Integer.MIN_VALUE;
            for (MovePair m : orderedMoves) {
                Board child = Board.makeMove(m, board.copy());           // safe copy
                int score = minimaxAlphaBeta(child, false, alpha, beta, startTime, timeLimitMs, ply + 1);
                if (score > bestScore) {
                    bestScore = score;
                    bestMoveForTT = m;
                }
                alpha = Math.max(alpha, bestScore);
                if (alpha >= beta) {
                    cutoffs++;
                    break;
                }
            }
        } else { // minimizing player
            bestScore = Integer.MAX_VALUE;
            for (MovePair m : orderedMoves) {
                Board child = Board.makeMove(m, board.copy());
                int score = minimaxAlphaBeta(child, true, alpha, beta, startTime, timeLimitMs, ply + 1);
                if (score < bestScore) {
                    bestScore = score;
                    bestMoveForTT = m;
                }
                beta = Math.min(beta, bestScore);
                if (beta <= alpha) {
                    cutoffs++;
                    break;
                }
            }
        }
        searchDepths.add(ply);

        // Store result in Transposition Table
        int entryType;
        if (bestScore <= originalAlpha) { // Failed low (upper bound)
            entryType = TranspositionTable.UPPER_BOUND;
        } else if (bestScore >= originalBeta) { // Failed high (lower bound)
            entryType = TranspositionTable.LOWER_BOUND;
        } else { // Exact score
            entryType = TranspositionTable.EXACT_SCORE;
        }
        transpositionTable.store(zobristHash, bestScore, (max_plies - ply), entryType, bestMoveForTT);
        return bestScore;
    }

    private static int minimaxAlphaBetaNoTT(Board board, boolean maximizingPlayer, int alpha, int beta, long startTime, long timeLimitMs, int ply) {
        nodesVisited++;

        /* ---------- hard stops: out of time OR too deep ------------------------ */
        if (System.currentTimeMillis() - startTime > timeLimitMs || ply >= max_plies) return Eval.evaluate(board);

        /* ---------- game-ending positions -------------------------------------- */
        Player prev = (board.getCurrentPlayer() == Player.RED) ? Player.BLUE : Player.RED;
        if (Board.checkplayerWon(board, prev)) {         // last mover just won
            return Eval.evaluate(board);
        }

        /* ---------- enumerate legal moves -------------------------------------- */
        List<MovePair> moves = MoveGenerator.generateAllLegalMoves(board);
        if (moves.isEmpty())                            // stalemate or no moves
            return Eval.evaluate(board);

        /* ---------- order moves to improve alpha-beta efficiency --------------- */
        List<MovePair> orderedMoves = MoveOrdering.orderMoves(moves, board, maximizingPlayer);

        /* ---------- standard alpha–beta recursion ------------------------------ */
        int bestScore;

        if (maximizingPlayer) {
            bestScore = Integer.MIN_VALUE;
            for (MovePair m : orderedMoves) {
                Board child = Board.makeMove(m, board.copy());           // safe copy
                int score = minimaxAlphaBeta(child, false, alpha, beta, startTime, timeLimitMs, ply + 1);
                if (score > bestScore) {
                    bestScore = score;
                }
                alpha = Math.max(alpha, bestScore);
                if (alpha >= beta) {
                    cutoffs++;
                    break;
                }
            }
        } else { // minimizing player
            bestScore = Integer.MAX_VALUE;
            for (MovePair m : orderedMoves) {
                Board child = Board.makeMove(m, board.copy());
                int score = minimaxAlphaBeta(child, true, alpha, beta, startTime, timeLimitMs, ply + 1);
                if (score < bestScore) {
                    bestScore = score;
                }
                beta = Math.min(beta, bestScore);
                if (beta <= alpha) {
                    cutoffs++;
                    break;
                }
            }
        }
        positionsSearched++;

        return bestScore;
    }
}