import org.junit.Test;

import java.util.concurrent.atomic.AtomicInteger;

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
}