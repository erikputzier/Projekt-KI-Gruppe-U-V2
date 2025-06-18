import org.junit.Test;

import java.util.concurrent.atomic.AtomicInteger;

public class MinimaxTest {

    @Test
    public void minimaxTest() {
        Board board = new Board("3r13/2b12r21/b16/2RG1b22/b16/2b22BG1/3r23 r");
        AtomicInteger stateCounter = new AtomicInteger();
        long start = System.currentTimeMillis();
        System.out.println(AI.minimax(board, 3, false));
        long duration = System.currentTimeMillis() - start;
        System.out.println("Minimax beendet:");
        System.out.println("Dauer: " + duration + " ms");
        System.out.println("Bewertete Zustände: " + stateCounter.get());
    }

    @Test
    public void minimaxAlphaBetaTest() {
        Board board = new Board("3RG3/7/7/7/4b11b1/4r4r11/3BG1b11 b");
        System.out.println(AI.minimaxAlphaBeta(board, 1000));
    }

    void benchmarkEvaluate(){
        Board board = new Board("b36/3b12r3/7/7/1r2RG4/2BG4/6r1 b");
        long start = System.currentTimeMillis();
        for(int i = 0; i < 10000; i++){
            AI.evaluate(board);
        }
        System.out.println("Dauer: " + (System.currentTimeMillis() - start));
    }
}