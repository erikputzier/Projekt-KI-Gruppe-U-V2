import java.sql.SQLOutput;
import java.util.ArrayList;
import java.util.List;

public class AI {
    private static int max_plies = 64;   // depth guard
    static long cutoffs = 0;
    static long ttHits = 0; // Counter for transposition table hits
    static double percent = 0.0;
    static int runs = 0;
    static int nodesVisited = 0;
    static int reSearches = 0;
    static int basicSearches = 1;
    static List<Integer> searchDepths = new ArrayList<>();
    static double average;

    private static final TranspositionTableArray transpositionTable = new TranspositionTableArray();

    public static MovePair pickMove(Board board) {
        // Reset counters for each move selection
        resetCounters();

        // Reset killer moves for a new search
        MoveOrdering.resetKillerMoves();

        List<MovePair> legalMoves = MoveGenerator.generateAllLegalMoves(board);
        boolean maximizingPlayer = board.getCurrentPlayer() != Player.BLUE;
        MovePair bestMove = null;
        int bestValue = maximizingPlayer ? Integer.MIN_VALUE : Integer.MAX_VALUE;

        // start global timer only ONCE
        long startTime = System.currentTimeMillis();
        int moveCounter = 0;
        long baseTimeLimit = 2000;
        long timeLimit = TimeManager.computeTimeBudget(board, legalMoves, baseTimeLimit);
        long branchLimit = (long) (timeLimit * 1.1 / legalMoves.size());
        System.out.println("Time Limit: " + timeLimit);

        /* ---------- order moves to improve alpha-beta efficiency --------------- */
        List<MovePair> orderedMoves = MoveOrdering.orderMoves(legalMoves, board, maximizingPlayer, 0);

        // Check if there's a best move in the transposition table
        long zobristHash = ZobristHashing.computeHash(board);
        TranspositionTableArray.TTEntry ttEntry = transpositionTable.retrieve(zobristHash);

        // If a best move was found in TT, try it first
        if (ttEntry != null && ttEntry.best != null) {
            orderedMoves.remove(ttEntry.best); // Remove if present to avoid duplicate
            orderedMoves.add(0, ttEntry.best); // Add to the front
        }

        while ((System.currentTimeMillis() - startTime) < timeLimit) {
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
        evaluate(moveCounter, legalMoves.size(), startTime);
        return bestMove;
    }

    public static MovePair pickMovePVS(Board board) {
        // Reset counters for each move selection
        resetCounters();

        // Reset killer moves for a new search
        MoveOrdering.resetKillerMoves();

        List<MovePair> legalMoves = MoveGenerator.generateAllLegalMoves(board);
        boolean maximizingPlayer = board.getCurrentPlayer() != Player.BLUE;
        MovePair bestMove = null;
        int bestValue = maximizingPlayer ? Integer.MIN_VALUE : Integer.MAX_VALUE;

        // start global timer only ONCE
        long startTime = System.currentTimeMillis();
        int moveCounter = 0;
        long baseTimeLimit = 3000;
        long timeLimit = TimeManager.computeTimeBudget(board, legalMoves, baseTimeLimit);
        long branchLimit = (long) (timeLimit * 1.1 / legalMoves.size());
        System.out.println("Time Limit: " + timeLimit);

        /* ---------- order moves to improve alpha-beta efficiency --------------- */
        List<MovePair> orderedMoves = MoveOrdering.orderMoves(legalMoves, board, maximizingPlayer, 0);

        // Check if there's a best move in the transposition table
        long zobristHash = ZobristHashing.computeHash(board);
        TranspositionTableArray.TTEntry ttEntry = transpositionTable.retrieve(zobristHash);

        // If a best move was found in TT, try it first
        if (ttEntry != null && ttEntry.best != null) {
            orderedMoves.remove(ttEntry.best); // Remove if present to avoid duplicate
            orderedMoves.add(0, ttEntry.best); // Add to the front
        }

        while ((System.currentTimeMillis() - startTime) < timeLimit) {
            for (MovePair move : orderedMoves) {
                Board newBoard = Board.makeMove(move, board.copy());

                int eval = AI.minimaxAlphaBetaPVS(newBoard, branchLimit);

                if (maximizingPlayer && eval > bestValue) {
                    bestValue = eval;
                    bestMove = move;
                } else if (!maximizingPlayer && eval < bestValue) {
                    bestValue = eval;
                    bestMove = move;
                }
                moveCounter++;
                // stop looping if we ran out of time
                //if (System.currentTimeMillis() - startTime > timeLimit) break;
            }
            max_plies++;
        }
        evaluate(moveCounter, legalMoves.size(), startTime);
        return bestMove;
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

    public static int minimaxAlphaBetaPVS(Board root, long timeLimitMs) {              // convenience
        boolean rootIsMax = (root.getCurrentPlayer() == Player.RED);
        long start = System.currentTimeMillis();
        return minimaxAlphaBetaPVS(root,                     /* board     */
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
        TranspositionTableArray.TTEntry ttEntry = transpositionTable.retrieve(zobristHash);
        int originalAlpha = alpha; // Store original alpha for TT storing
        int originalBeta = beta;   // Store original beta for TT storing

        if (ttEntry != null && ttEntry.depth >= (max_plies - ply)) { // Compare with remaining depth
            ttHits++;
            if (ttEntry.type == TranspositionTable.EXACT_SCORE) {
                return ttEntry.score;
            } else if (ttEntry.type == TranspositionTable.LOWER_BOUND) {
                alpha = Math.max(alpha, ttEntry.score);
            } else if (ttEntry.type == TranspositionTable.UPPER_BOUND) {
                beta = Math.min(beta, ttEntry.score);
            }
            if (alpha >= beta) {
                return ttEntry.score; // Or alpha/beta depending on bound type, but score should be fine for cutoffs
            }
        }

        /* ---------- hard stops: out of time OR too deep ------------------------ */
        if (System.currentTimeMillis() - startTime > timeLimitMs || ply >= max_plies) {
            return quiesce(board, alpha, beta, maximizingPlayer);
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
        List<MovePair> orderedMoves = MoveOrdering.orderMoves(moves, board, maximizingPlayer, ply);
        // If a best move was found in TT, try it first
        if (ttEntry != null && ttEntry.best != null) {
            orderedMoves.remove(ttEntry.best); // Remove if present to avoid duplicate
            orderedMoves.add(0, ttEntry.best); // Add to the front
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
                    // Store the move that caused the cutoff as a killer move
                    MoveOrdering.updateKillerMove(m, ply);
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
                    // Store the move that caused the cutoff as a killer move
                    MoveOrdering.updateKillerMove(m, ply);
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

        short effectiveDepth = (short) (max_plies - ply);
        if (ply >= max_plies)   // quiescence node
            effectiveDepth = 0;

        // Store the best move for this position in the TT
        transpositionTable.store(zobristHash, bestScore, effectiveDepth, (byte) entryType, bestMoveForTT);
        return bestScore;
    }

    private static int quiesce(Board node, int alpha, int beta, boolean maximizing) {
        int standPat = Eval.evaluate(node);
        if (maximizing) {
            if (standPat >= beta) return standPat;
            alpha = Math.max(alpha, standPat);
        } else {
            if (standPat <= alpha) return standPat;
            beta = Math.min(beta, standPat);
        }

        for (MovePair m : MoveGenerator.generateNoisyMoves(node)) {
            Board child = Board.makeMove(m, node.copy());
            int score = quiesce(child, alpha, beta, !maximizing);
            if (maximizing) {
                if (score > alpha) alpha = score;
            } else {
                if (score < beta) beta = score;
            }
            if (alpha >= beta) break;      // cutoff
        }
        return maximizing ? alpha : beta;
    }

    private static int minimaxAlphaBetaPVS(Board board, boolean maximizingPlayer, int alpha, int beta, long startTime, long timeLimitMs, int ply) {
        nodesVisited++;
        /* ---------- Zobrist Hashing and Transposition Table Lookup ------------- */
        long zobristHash = ZobristHashing.computeHash(board);
        TranspositionTableArray.TTEntry ttEntry = transpositionTable.retrieve(zobristHash);
        int originalAlpha = alpha; // Store original alpha for TT storing
        int originalBeta = beta;   // Store original beta for TT storing

        if (ttEntry != null && ttEntry.depth >= (max_plies - ply)) { // Compare with remaining depth
            ttHits++;
            if (ttEntry.type == TranspositionTable.EXACT_SCORE) {
                return ttEntry.score;
            } else if (ttEntry.type == TranspositionTable.LOWER_BOUND) {
                alpha = Math.max(alpha, ttEntry.score);
            } else if (ttEntry.type == TranspositionTable.UPPER_BOUND) {
                beta = Math.min(beta, ttEntry.score);
            }
            if (alpha >= beta) {
                return ttEntry.score; // Or alpha/beta depending on bound type, but score should be fine for cutoffs
            }
        }

        /* ---------- hard stops: out of time OR too deep ------------------------ */
        if (System.currentTimeMillis() - startTime > timeLimitMs || ply >= max_plies) {
            return quiesce(board, alpha, beta, maximizingPlayer);
        }

        /* ---------- game-ending positions -------------------------------------- */
        Player prev = (board.getCurrentPlayer() == Player.RED) ? Player.BLUE : Player.RED;
        if (Board.checkplayerWon(board, prev)) {         // last mover just won
            return Eval.naiveEvaluate(board);
        }

        /* ---------- enumerate legal moves -------------------------------------- */
        List<MovePair> moves = MoveGenerator.generateAllLegalMoves(board);
        if (moves.isEmpty()) {                            // stalemate or no moves
            return Eval.naiveEvaluate(board);
        }

        /* ---------- order moves to improve alpha-beta efficiency --------------- */
        List<MovePair> orderedMoves = MoveOrdering.orderMoves(moves, board, maximizingPlayer, ply);
        // If a best move was found in TT, try it first
        if (ttEntry != null && ttEntry.best != null) {
            orderedMoves.remove(ttEntry.best); // Remove if present to avoid duplicate
            orderedMoves.add(0, ttEntry.best);
        }

        /* ---------- standard alpha–beta recursion ------------------------------ */
        int bestScore;
        MovePair bestMoveForTT = null;

        if (maximizingPlayer) {
            bestScore = Integer.MIN_VALUE;

            //Besten Move mit Vollem Alpha Beta Fenster durchsuchen
            Board child = Board.makeMove(orderedMoves.getFirst(), board.copy());
            orderedMoves.removeFirst();
            bestScore = minimaxAlphaBetaPVS(child, false, alpha, beta, startTime, timeLimitMs, ply + 1);
            basicSearches++;

            for (MovePair m : orderedMoves) {
                child = Board.makeMove(m, board.copy());           // safe copy
                //alle anderen Moves mti Null Window durchsuchen
                int score = minimaxAlphaBeta(child, false, alpha, alpha+1, startTime, timeLimitMs, ply + 1);
                basicSearches++;
                //re-search falls score im Fenster liegt
                if(score > alpha && score < beta){
                    //re-search mit Fenster [alpha;beta]
                    reSearches++;
                    score = minimaxAlphaBetaPVS(child, false, alpha, beta, startTime, timeLimitMs, ply + 1);
                    if(score > alpha){
                        alpha = score;
                    }
                }

                if (score > bestScore) {
                    bestScore = score;
                    bestMoveForTT = m;
                }
                alpha = Math.max(alpha, bestScore);
                if (alpha >= beta) {
                    cutoffs++;
                    // Store the move that caused the cutoff as a killer move
                    MoveOrdering.updateKillerMove(m, ply);
                    break;
                }
            }
        } else { // minimizing player
            bestScore = Integer.MAX_VALUE;

            //Besten Move mit Vollem Alpha Beta Fenster durchsuchen
            Board child = Board.makeMove(orderedMoves.getFirst(), board.copy());
            orderedMoves.removeFirst();
            bestScore = minimaxAlphaBetaPVS(child, true, alpha, beta, startTime, timeLimitMs, ply + 1);
            basicSearches++;

            for (MovePair m : orderedMoves) {
                child = Board.makeMove(m, board.copy());
                int score = minimaxAlphaBeta(child, true, beta-1, beta, startTime, timeLimitMs, ply + 1);
                basicSearches++;

                //re-search falls score im Fenster liegt
                if(score > alpha && score < beta){
                    //re-search mit Fenster [alpha;beta]
                    reSearches++;
                    score = minimaxAlphaBetaPVS(child, true, alpha, beta, startTime, timeLimitMs, ply + 1);
                    if(score < beta){
                        beta = score;
                    }
                }

                if (score < bestScore) {
                    bestScore = score;
                    bestMoveForTT = m;
                }
                beta = Math.min(beta, bestScore);
                if (beta <= alpha) {
                    cutoffs++;
                    // Store the move that caused the cutoff as a killer move
                    MoveOrdering.updateKillerMove(m, ply);
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
        transpositionTable.store(zobristHash, bestScore, (short) (max_plies - ply), (byte) entryType, bestMoveForTT);
        return bestScore;
    }

    public static void evaluate(int moveCounter, int legalMovesSize, long startTime) {

        /* 1 — compute current ratio safely */
        double thisRatio = nodesVisited == 0 ? 0.0 : 100.0 * cutoffs / nodesVisited;

        /* 2 — update running average */
        percent += thisRatio;
        runs++;
        average = percent / runs;

        /* 3 — print everything once, after it is correct */
        System.out.println("\nEvaluating AI performance:");
        System.out.printf("Total cut-offs:                %d%n", cutoffs);
        System.out.printf("Transposition-table hits:      %d%n", ttHits);
        System.out.printf("αβ-cut ratio this search:      %.1f%%%n", thisRatio);
        System.out.printf("αβ-cut ratio running average:  %.1f%%%n", average);

        /* 4 — moves evaluated only in the last pass */
        int movesLastPass = moveCounter % legalMovesSize;
        System.out.printf("Moves searched in last pass:   %d / %d%n", movesLastPass == 0 ? legalMovesSize : movesLastPass, legalMovesSize);

        /* 5 — real search depth reached (last completed ply) */
        int reachedDepth = max_plies > 0 ? max_plies - 1 : 0;
        System.out.printf("Depth reached:                 %d plies%n", reachedDepth);

        double ttFillRate = (transpositionTable.size() / (double) TranspositionTableArray.TABLE_SIZE) * 100;
        System.out.printf("TT fill rate:                  %.2f%%%n", ttFillRate);

        System.out.printf("Time for pickMove():           %d ms%n", System.currentTimeMillis() - startTime);
        System.out.printf("Total nodes visited:           %d%n", nodesVisited);
        System.out.printf("Number of re-searches:         %d%n", reSearches);
        System.out.printf("Number of basicSearches:       %d%n", basicSearches);
        System.out.printf("Pct re-searches:               %d%n", reSearches * 100 / basicSearches);
    }


    public static void resetCounters() {
        max_plies = 1;
        nodesVisited = 0;
        cutoffs = 0;
        ttHits = 0;
        reSearches = 0;
    }

    public static void clearTT(){
        transpositionTable.clear();
    }
}