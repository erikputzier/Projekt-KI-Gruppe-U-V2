import org.junit.Test;
import static org.junit.Assert.*;

import java.util.List;

public class PickMoveVersionTest {
    static int minimaxNodesVisited = 0;


    @Test
    public void comparePVStoAlphaBeta() {
        //Test ALpha Beta on Startposition
        Board board = new Board("7/6r3/1RG5/3b43/1r25/7/2BG3r1 r");
        long startTime = System.currentTimeMillis();
        MovePair bestMoveAlphaBeta = AI.pickMove(board);

        //alle Counter und Transposition table zurucksetzen
        AI.resetCounters();
        AI.clearTT();

        //Test PVS on Startposition
        startTime = System.currentTimeMillis();
        MovePair bestMovePVS = AI.pickMovePVS(board);
    }
    @Test
    public void comparePVStoMinimax(){
        int depth = 4;
        //start Board
        Board startBoard = new Board();

        System.out.println("----------------Minimax----------------");
        long startTime = System.currentTimeMillis();
        AI.clearTT();
        MovePair bestMoveMinimax = pickMoveMinimax(startBoard, depth-1);
        long minimaxTime = System.currentTimeMillis() - startTime;
        System.out.println("Minimax Nodes: " + minimaxNodesVisited);
        System.out.println("Minimax Time: " + minimaxTime);

        System.out.println("----------------PVS----------------");
        AI.clearTT();
        MovePair bestMovePVS = AI.pickMovePVSTestVersion(startBoard, depth);

        System.out.println("----------------Alpha Beta----------------");
        AI.clearTT();
        MovePair bestMoveAlphaBeta = AI.pickMoveTestVersion(startBoard, depth);

        System.out.println("------------Best Moves: ---------------");
        System.out.println("Minimax: " +  bestMoveMinimax);
        System.out.println("PVS: " +  bestMovePVS);
        System.out.println("Alpha beta: " +  bestMoveAlphaBeta);


    }



    public MovePair pickMoveMinimax(Board board, int depth) {
        boolean maximizingPlayer = board.getCurrentPlayer() != Player.BLUE;

        List<MovePair> moves = MoveGenerator.generateAllLegalMoves(board);
        MovePair bestMove = null;
        int bestScore;
        if (maximizingPlayer) {
            bestScore = Integer.MIN_VALUE;
        }else {
            bestScore = Integer.MAX_VALUE;
        }
        int score;
        for (MovePair move : moves){
            Board child = Board.makeMove(move, board.copy());
            score  = minimax(child, depth, maximizingPlayer);

            if(maximizingPlayer && score > bestScore){
                bestScore = score;
                bestMove = move;
            }else if (!maximizingPlayer && score < bestScore){
                bestScore = score;
                bestMove = move;
            }
        }

        return bestMove;
    }

    public static int minimax(Board board, int depth, boolean maximizingPlayer) {

        /* ---------- hard stop: search horizon reached ------------------------- */
        if (depth == 0) {
            return Eval.evaluate(board);
        }

        /* ---------- game end check (the side that just moved) ------------------ */
        Player prev = (board.getCurrentPlayer() == Player.RED) ? Player.BLUE : Player.RED;
        if (Board.checkplayerWon(board, prev)) {
            return Eval.evaluate(board);
        }

        /* ---------- generate legal moves --------------------------------------- */
        List<MovePair> moves = MoveGenerator.generateAllLegalMoves(board);
        if (moves.isEmpty())                           // stalemate or no moves
            return Eval.evaluate(board);

        /* ---------- order moves to improve search efficiency ------------------ */
        List<MovePair> orderedMoves = MoveOrdering.orderMoves(moves, board, maximizingPlayer, depth);

        /* ---------- recursive descent ------------------------------------------ */
        int best;
        if (maximizingPlayer) {
            best = Integer.MIN_VALUE;
            for (MovePair m : orderedMoves) {
                Board child = Board.makeMove(m, board.copy());  // safe copy
                int score = minimax(child, depth - 1, false);
                minimaxNodesVisited++;
                if (score > best) {
                    best = score;
                }
            }
        } else {                                       // minimizing player
            best = Integer.MAX_VALUE;
            for (MovePair m : orderedMoves) {
                Board child = Board.makeMove(m, board.copy());
                int score = minimax(child, depth - 1, true);
                minimaxNodesVisited++;
                if (score < best) {
                    best = score;
                }
            }
        }
        return best;
    }
}