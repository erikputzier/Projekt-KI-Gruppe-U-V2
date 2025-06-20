import org.junit.Test;

import static org.junit.Assert.*;

public class TestEval {

    @Test
    public void testStartEval() {
        Board startBoard = new Board();
        assertEquals(0, Eval.evaluate(startBoard));
    }

    @Test
    public void testEvaluateSide() {
        Board startBoard = new Board();
        assertEquals(Eval.evaluateSide(startBoard, Player.BLUE), Eval.evaluateSide(startBoard, Player.RED));
    }


    @Test
    public void benchmarkEval() {

        //Benchmark Eval on Start Board
        Board startBoard = new Board();
        long endtime;
        long starttime = System.currentTimeMillis();
        for (int i = 0; i < 10000; i++) {
            Eval.evaluate(startBoard);
        }
        endtime = System.currentTimeMillis();
        System.out.println((endtime - starttime) + "ms");

        //Benchmark Eval on Midgame Position
        Board midgameBoard = new Board("7/6r3/1RG5/3b43/1r25/7/2BG3r1 r");
        starttime = System.currentTimeMillis();
        for (int i = 0; i < 10000; i++) {
            Eval.evaluate(midgameBoard);
        }
        endtime = System.currentTimeMillis();
        System.out.println("Evaluating the midgame position 10000 times took " + (endtime - starttime) + "ms");

        //Benchmark Eval on endgame Position
        Board endgameBoard = new Board("b36/3b12r3/7/7/1r2RG4/2BG4/6r1 b");
        starttime = System.currentTimeMillis();
        for (int i = 0; i < 10000; i++) {
            Eval.evaluate(endgameBoard);
        }
        endtime = System.currentTimeMillis();
        System.out.println("Evaluating the endgame position 10000 times took " + (endtime - starttime) + "ms");
    }
}