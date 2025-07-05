import org.junit.Test;

import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.Assert.assertTrue;

public class MinimaxTest {

    @Test
    public void minimaxAlphaBetaTest() {
        Board board = new Board("3RG3/7/7/7/4b11b1/4r4r11/3BG1b11 b");
        System.out.println(AI.minimaxAlphaBeta(board, 1000));
    }

    @Test
    public void benchmarkEvaluate() {
        Board board = new Board("b36/3b12r3/7/7/1r2RG4/2BG4/6r1 b");
        long start = System.currentTimeMillis();
        for (int i = 0; i < 10000; i++) {
            Eval.evaluate(board);
        }
        System.out.println("Dauer: " + (System.currentTimeMillis() - start));
    }

    @Test
    public void benchmarkMinimaxAlphaBeta() {
        System.out.println("---------Benchmark Start Board---------");
        Board startBoard = new Board();
        AI.pickMove(startBoard);

        System.out.println("---------Benchmark Midgame Board---------");
        Board midgameBoard = new Board("7/6r3/1RG5/3b43/1r25/7/2BG3r1 r");
        AI.pickMove(midgameBoard);

        System.out.println("---------Benchmark Endgame Board---------");
        Board endgameBoard = new Board("b36/3b12r3/7/7/1r2RG4/2BG4/6r1 b");
        AI.pickMove(endgameBoard);
    }

    @Test
    public void testA1(){
        Board board = new Board("7/6r3/1RG5/3b43/1r25/7/2BG3r1 r");
        AI.resetCounters();
        AI.minimaxAlphaBeta(board, 1000);
        System.out.println(AI.nodesVisited);
    }



    @Test
    public void testTerminalWinningPosition(){
        //Board in dem Rot gewonnen hat aber blau am zug ist
        Board board  = new Board("1RG5/7/7/7/7/7/7 b");

        int eval = AI.minimaxAlphaBeta(board, 1000);
        System.out.println(eval);
    }
    @Test
    public void testTranspositionTableUsage() {
        Board board = new Board();
        AI.resetCounters();

        AI.minimaxAlphaBeta(board, 1000); // erster Durchlauf füllt TT
        long firstTTHits = AI.ttHits;

        AI.minimaxAlphaBeta(board, 1000); // zweiter Durchlauf sollte TT nutzen
        long secondTTHits = AI.ttHits;

        assertTrue("Expected more TT hits in second run", secondTTHits > firstTTHits);
    }






}