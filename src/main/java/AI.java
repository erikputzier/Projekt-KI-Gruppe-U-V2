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
        resetMaxPlies();

        // Reset killer moves for a new search
        MoveOrdering.resetKillerMoves();

        List<MovePair> legalMoves = MoveGenerator.generateAllLegalMoves(board);
        boolean maximizingPlayer = board.getCurrentPlayer() != Player.BLUE;
        MovePair totalBestMove = null;
        int totalBestValue = maximizingPlayer ? Integer.MIN_VALUE : Integer.MAX_VALUE;

        // start global timer only ONCE
        long startTime = System.currentTimeMillis();
        int moveCounter = 0;
        long baseTimeLimit = 2000;
        long timeLimit = TimeManager.computeTimeBudget(board, legalMoves, baseTimeLimit);
        long branchLimit = timeLimit;//(long) (timeLimit * 1.1 / legalMoves.size());
        System.out.println("Time Limit: " + timeLimit);

        /* ---------- order moves to improve alpha-beta efficiency --------------- */
        List<MovePair> orderedMoves = MoveOrdering.orderMoves(legalMoves, board, maximizingPlayer, 0);

        // Check if there's a best move in the transposition table
        long zobristHash = ZobristHashing.computeHash(board);
        TranspositionTableArray.TTEntry ttEntry = transpositionTable.retrieve(zobristHash);

        // If a best move was found in TT, try it first
        if (ttEntry != null && ttEntry.best != null) {
            orderedMoves.remove(ttEntry.best); // Remove if present to avoid duplicate
            orderedMoves.addFirst(ttEntry.best); // Add to the front
        }

        while ((System.currentTimeMillis() - startTime) < timeLimit) {
            resetCounters();
            MovePair bestMove = totalBestMove;
            int bestValue = maximizingPlayer ? Integer.MIN_VALUE : Integer.MAX_VALUE;;
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
            if (System.currentTimeMillis() - startTime <= timeLimit) {
                max_plies++;
                totalBestValue = bestValue;
                totalBestMove = bestMove;
            }

        }
        evaluate(moveCounter, legalMoves.size(), startTime);
        return totalBestMove;
    }

    public static MovePair pickMoveTestVersion(Board board, int maxply) {
        // Reset counters for each move selection
        resetCounters();
        resetMaxPlies();

        // Reset killer moves for a new search
        MoveOrdering.resetKillerMoves();

        List<MovePair> legalMoves = MoveGenerator.generateAllLegalMoves(board);
        boolean maximizingPlayer = board.getCurrentPlayer() != Player.BLUE;
        MovePair totalBestMove = null;
        int totalBestValue = maximizingPlayer ? Integer.MIN_VALUE : Integer.MAX_VALUE;

        // start global timer only ONCE
        long startTime = System.currentTimeMillis();
        int moveCounter = 0;
        long baseTimeLimit = 2000;
        long timeLimit = TimeManager.computeTimeBudget(board, legalMoves, baseTimeLimit);
        long branchLimit = timeLimit;//(long) (timeLimit * 1.1 / legalMoves.size());
        System.out.println("Time Limit: " + timeLimit);

        /* ---------- order moves to improve alpha-beta efficiency --------------- */
        List<MovePair> orderedMoves = MoveOrdering.orderMoves(legalMoves, board, maximizingPlayer, 0);

        // Check if there's a best move in the transposition table
        long zobristHash = ZobristHashing.computeHash(board);
        TranspositionTableArray.TTEntry ttEntry = transpositionTable.retrieve(zobristHash);

        // If a best move was found in TT, try it first
        if (ttEntry != null && ttEntry.best != null) {
            orderedMoves.remove(ttEntry.best); // Remove if present to avoid duplicate
            orderedMoves.addFirst(ttEntry.best); // Add to the front
        }
        int ply = 0;
        while (ply <= maxply) {
            resetCounters();
            MovePair bestMove = totalBestMove;
            int bestValue = maximizingPlayer ? Integer.MIN_VALUE : Integer.MAX_VALUE;;
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
            if (System.currentTimeMillis() - startTime <= timeLimit) {
                max_plies++;
                totalBestValue = bestValue;
                totalBestMove = bestMove;
            }
            ply++;
        }
        evaluate(moveCounter, legalMoves.size(), startTime);
        return totalBestMove;
    }

    public static MovePair pickMovePVS(Board board) {
        // Reset counters for each move selection
        resetCounters();
        resetMaxPlies();

        // Reset killer moves for a new search
        MoveOrdering.resetKillerMoves();

        List<MovePair> legalMoves = MoveGenerator.generateAllLegalMoves(board);
        boolean maximizingPlayer = board.getCurrentPlayer() != Player.BLUE;
        MovePair totalBestMove = null;
        int totalBestValue = maximizingPlayer ? Integer.MIN_VALUE : Integer.MAX_VALUE;

        // start global timer only ONCE
        long startTime = System.currentTimeMillis();
        int moveCounter = 0;
        long baseTimeLimit = 2000;
        long timeLimit = TimeManager.computeTimeBudget(board, legalMoves, baseTimeLimit);
        long branchLimit = (long) (timeLimit * 1.1 / legalMoves.size());
        System.out.println("Time Limit: " + timeLimit);
        Integer alpha;
        Integer beta;

        /* ---------- order moves to improve alpha-beta efficiency --------------- */
        List<MovePair> orderedMoves = MoveOrdering.orderMoves(legalMoves, board, maximizingPlayer, 0);

        // Check if there's a best move in the transposition table
        long zobristHash = ZobristHashing.computeHash(board);
        TranspositionTableArray.TTEntry ttEntry = transpositionTable.retrieve(zobristHash);

        // If a best move was found in TT, try it first
        if (ttEntry != null && ttEntry.best != null) {
            orderedMoves.remove(ttEntry.best); // Remove if present to avoid duplicate
            orderedMoves.addFirst(ttEntry.best); // Add to the front
        }

        while ((System.currentTimeMillis() - startTime) < timeLimit) {
            resetCounters();
            alpha = Integer.MIN_VALUE;
            beta = Integer.MAX_VALUE;
            MovePair bestMove = null;
            int bestValue = maximizingPlayer ? Integer.MIN_VALUE : Integer.MAX_VALUE;

            //Besten Move mit vollem Alpha Beta Fenster durchsuchen
            Board newBoard = Board.makeMove(orderedMoves.getFirst(), board.copy());
            int eval = AI.minimaxAlphaBetaPVS(newBoard, branchLimit, alpha, beta);


            if (maximizingPlayer && eval > bestValue) {
                bestValue = eval;
                bestMove = orderedMoves.getFirst();
            } else if (!maximizingPlayer && eval < bestValue) {
                bestValue = eval;
                bestMove = orderedMoves.getFirst();
            }

            if(maximizingPlayer){
                alpha = Math.max(alpha, eval);
                if (alpha >= beta) {
                    // Store the move that caused the cutoff as a killer move
                    MoveOrdering.updateKillerMove(orderedMoves.getFirst(), 0);
                }
            } else {
                beta = Math.min(beta, eval);
                if (beta <= alpha) {
                    MoveOrdering.updateKillerMove(orderedMoves.getFirst(), 0);
                }
            }

            orderedMoves.removeFirst();
            moveCounter++;

            for (MovePair move : orderedMoves) {
                newBoard = Board.makeMove(move, board.copy());

                if(maximizingPlayer){
                    eval = AI.minimaxAlphaBetaPVS(newBoard, branchLimit, alpha, alpha+1);

                    if(eval > alpha && eval < beta){
                        //re-search mit Fenster [alpha;beta]
                        reSearches++;
                        eval = AI.minimaxAlphaBetaPVS(newBoard, branchLimit, alpha, beta);
                        if(eval < beta){
                            beta = eval;
                        }
                    }

                    alpha = Math.max(alpha, eval);
                    if (alpha >= beta) {
                        cutoffs++;
                        // Store the move that caused the cutoff as a killer move
                        MoveOrdering.updateKillerMove(move, 0);
                        break;
                    }
                } else {
                    eval = AI.minimaxAlphaBetaPVS(newBoard, branchLimit, beta-1, beta);

                    if(eval > alpha && eval < beta){
                        //re-search mit Fenster [alpha;beta]
                        reSearches++;
                        eval = AI.minimaxAlphaBetaPVS(newBoard, branchLimit, alpha, beta);
                        if(eval < beta){
                            beta = eval;
                        }
                    }

                    beta = Math.min(beta, eval);
                    if (beta <= alpha) {
                        MoveOrdering.updateKillerMove(move, 0);
                    }
                }

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
            if (System.currentTimeMillis() - startTime <= timeLimit) {
                totalBestMove = bestMove;
                totalBestValue = bestValue;
                max_plies++;
            }
        }
        evaluate(moveCounter, legalMoves.size(), startTime);
        return totalBestMove;
    }

    public static MovePair pickMovePVSTestVersion(Board board, int maxply) {
        // Reset counters for each move selection
        resetCounters();
        resetMaxPlies();

        // Reset killer moves for a new search
        MoveOrdering.resetKillerMoves();

        List<MovePair> legalMoves = MoveGenerator.generateAllLegalMoves(board);
        boolean maximizingPlayer = board.getCurrentPlayer() != Player.BLUE;
        MovePair totalBestMove = null;
        int totalBestValue = maximizingPlayer ? Integer.MIN_VALUE : Integer.MAX_VALUE;

        // start global timer only ONCE
        long startTime = System.currentTimeMillis();
        int moveCounter = 0;
        long baseTimeLimit = 2000;
        long timeLimit = TimeManager.computeTimeBudget(board, legalMoves, baseTimeLimit);
        long branchLimit = (long) (timeLimit * 1.1 / legalMoves.size());
        System.out.println("Time Limit: " + timeLimit);
        Integer alpha;
        Integer beta;

        /* ---------- order moves to improve alpha-beta efficiency --------------- */
        List<MovePair> orderedMoves = MoveOrdering.orderMoves(legalMoves, board, maximizingPlayer, 0);

        // Check if there's a best move in the transposition table
        long zobristHash = ZobristHashing.computeHash(board);
        TranspositionTableArray.TTEntry ttEntry = transpositionTable.retrieve(zobristHash);

        // If a best move was found in TT, try it first
        if (ttEntry != null && ttEntry.best != null) {
            orderedMoves.remove(ttEntry.best); // Remove if present to avoid duplicate
            orderedMoves.addFirst(ttEntry.best); // Add to the front
        }
        int ply = 0;
        while (ply <= maxply) {
            resetCounters();
            alpha = Integer.MIN_VALUE;
            beta = Integer.MAX_VALUE;
            MovePair bestMove = null;
            int bestValue = maximizingPlayer ? Integer.MIN_VALUE : Integer.MAX_VALUE;

            //Besten Move mit vollem Alpha Beta Fenster durchsuchen
            Board newBoard = Board.makeMove(orderedMoves.getFirst(), board.copy());
            int eval = AI.minimaxAlphaBetaPVS(newBoard, branchLimit, alpha, beta);


            if (maximizingPlayer && eval > bestValue) {
                bestValue = eval;
                bestMove = orderedMoves.getFirst();
            } else if (!maximizingPlayer && eval < bestValue) {
                bestValue = eval;
                bestMove = orderedMoves.getFirst();
            }

            if(maximizingPlayer){
                alpha = Math.max(alpha, eval);
                if (alpha >= beta) {
                    // Store the move that caused the cutoff as a killer move
                    MoveOrdering.updateKillerMove(orderedMoves.getFirst(), 0);
                }
            } else {
                beta = Math.min(beta, eval);
                if (beta <= alpha) {
                    MoveOrdering.updateKillerMove(orderedMoves.getFirst(), 0);
                }
            }

            orderedMoves.removeFirst();
            moveCounter++;

            for (MovePair move : orderedMoves) {
                newBoard = Board.makeMove(move, board.copy());

                if(maximizingPlayer){
                    eval = AI.minimaxAlphaBetaPVS(newBoard, branchLimit, alpha, alpha+1);

                    if(eval > alpha && eval < beta){
                        //re-search mit Fenster [alpha;beta]
                        reSearches++;
                        eval = AI.minimaxAlphaBetaPVS(newBoard, branchLimit, alpha, beta);
                        if(eval < beta){
                            beta = eval;
                        }
                    }

                    alpha = Math.max(alpha, eval);
                    if (alpha >= beta) {
                        cutoffs++;
                        // Store the move that caused the cutoff as a killer move
                        MoveOrdering.updateKillerMove(orderedMoves.getFirst(), 0);
                        break;
                    }
                } else {
                    eval = AI.minimaxAlphaBetaPVS(newBoard, branchLimit, beta-1, beta);

                    if(eval > alpha && eval < beta){
                        //re-search mit Fenster [alpha;beta]
                        reSearches++;
                        eval = AI.minimaxAlphaBetaPVS(newBoard, branchLimit, alpha, beta);
                        if(eval < beta){
                            beta = eval;
                        }
                    }

                    beta = Math.min(beta, eval);
                    if (beta <= alpha) {
                        MoveOrdering.updateKillerMove(orderedMoves.getFirst(), 0);
                    }
                }

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
            if (System.currentTimeMillis() - startTime <= timeLimit) {
                totalBestMove = bestMove;
                totalBestValue = bestValue;
                max_plies++;
            }
            ply++;
        }
        evaluate(moveCounter, legalMoves.size(), startTime);
        return totalBestMove;
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

    public static int minimaxAlphaBetaPVS(Board root, long timeLimitMs, Integer alpha, Integer beta) {              // convenience
        boolean rootIsMax = (root.getCurrentPlayer() == Player.RED);
        long start = System.currentTimeMillis();
        return minimaxAlphaBetaPVS(root,                     /* board     */
                rootIsMax,                                /* max player*/
                alpha, beta,                              /* α, β      */
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
            if (ttEntry.type == TranspositionTableArray.EXACT_SCORE) {
                return ttEntry.score;
            } else if (ttEntry.type == TranspositionTableArray.LOWER_BOUND) {
                alpha = Math.max(alpha, ttEntry.score);
            } else if (ttEntry.type == TranspositionTableArray.UPPER_BOUND) {
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
            orderedMoves.addFirst(ttEntry.best); // Add to the front
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
                alpha = Math.max(alpha, score);
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
                beta = Math.min(beta, score);
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
            entryType = TranspositionTableArray.UPPER_BOUND;
        } else if (bestScore >= originalBeta) { // Failed high (lower bound)
            entryType = TranspositionTableArray.LOWER_BOUND;
        } else { // Exact score
            entryType = TranspositionTableArray.EXACT_SCORE;
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
            if (ttEntry.type == TranspositionTableArray.EXACT_SCORE) {
                return ttEntry.score;
            } else if (ttEntry.type == TranspositionTableArray.LOWER_BOUND) {
                alpha = Math.max(alpha, ttEntry.score);
            } else if (ttEntry.type == TranspositionTableArray.UPPER_BOUND) {
                beta = Math.min(beta, ttEntry.score);
            }
            if (alpha >= beta) {
                return ttEntry.score; // Or alpha/beta depending on bound type, but score should be fine for cutoffs
            }
        }

        /* ---------- hard stops: out of time OR too deep ------------------------ */
        if ( ply >= max_plies) { //System.currentTimeMillis() - startTime > timeLimitMs ||
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
            orderedMoves.addFirst(ttEntry.best);
        }

        /* ---------- standard alpha–beta recursion ------------------------------ */
        int bestScore;
        MovePair bestMoveForTT = null;

        if (maximizingPlayer) {
            bestScore = Integer.MIN_VALUE;

            //Besten Move mit Vollem Alpha Beta Fenster durchsuchen
            Board child = Board.makeMove(orderedMoves.getFirst(), board.copy());
            bestScore = minimaxAlphaBetaPVS(child, false, alpha, beta, startTime, timeLimitMs, ply + 1);
            basicSearches++;

            bestMoveForTT = orderedMoves.getFirst();
            alpha = Math.max(alpha, bestScore);
            orderedMoves.removeFirst();


            for (MovePair m : orderedMoves) {
                child = Board.makeMove(m, board.copy());           // safe copy
                //alle anderen Moves mti Null Window durchsuchen
                int score = minimaxAlphaBetaPVS(child, false, alpha, alpha+1, startTime, timeLimitMs, ply + 1);
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
                alpha = Math.max(alpha, score);
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
            bestScore = minimaxAlphaBetaPVS(child, true, alpha, beta, startTime, timeLimitMs, ply + 1);
            basicSearches++;

            bestMoveForTT = orderedMoves.getFirst();
            beta = Math.min(beta, bestScore);
            orderedMoves.removeFirst();


            for (MovePair m : orderedMoves) {
                child = Board.makeMove(m, board.copy());
                int score = minimaxAlphaBetaPVS(child, true, beta-1, beta, startTime, timeLimitMs, ply + 1);
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
                beta = Math.min(beta, score);
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
            entryType = TranspositionTableArray.UPPER_BOUND;
        } else if (bestScore >= originalBeta) { // Failed high (lower bound)
            entryType = TranspositionTableArray.LOWER_BOUND;
        } else { // Exact score
            entryType = TranspositionTableArray.EXACT_SCORE;
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
        nodesVisited = 0;
        cutoffs = 0;
        ttHits = 0;
        reSearches = 0;
        basicSearches = 1;
    }

    public static void resetMaxPlies() {
        max_plies = 0;
    }

    public static void clearTT(){
        transpositionTable.clear();
    }
}