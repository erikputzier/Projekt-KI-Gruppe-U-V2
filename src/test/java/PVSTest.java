import org.junit.Test;

public class PVSTest {


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
}