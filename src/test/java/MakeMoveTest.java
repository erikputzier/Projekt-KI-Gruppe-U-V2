import org.junit.Test;

import static org.junit.Assert.*;

public class MakeMoveTest {

    @Test
    public void makeMoveTestAddTowers() {
        Board before = new Board("3RG3/7/7/7/4b11b1/4r4r11/3BG1b11 r");
        Board after = new Board("3RG3/7/7/7/4b11b1/4r52/3BG1b11 b");
        MovePair move = new MovePair(8, 9, 1);
        assertEquals(BitBoardUtils.makeMove(move, before), after);
    }

    @Test
    public void makeMoveTestBeatStack() {
        Board before = new Board("3RG3/7/7/7/4b11b1/4r4r11/3BG1b11 r");
        Board after = new Board("3RG3/7/7/7/4r11b1/4r3r11/3BG1b11 b");
        MovePair move = new MovePair(9, 16, 1);
        assertEquals(BitBoardUtils.makeMove(move, before), after);
    }

    @Test
    public void makeMoveTestBeatGuard() {
        Board before = new Board("3RG3/7/7/7/4b11b1/3r41r11/3BG1b11 r");
        Board after = new Board("3RG3/7/7/7/4b11b1/3r31r11/3r11b11 b");
        MovePair move = new MovePair(10, 3, 1);
        Board outcome = BitBoardUtils.makeMove(move, before);
        assertEquals(outcome, after);

    }

    @Test
    public void makeMoveTestGuardBeatsGuard() {
        Board before = new Board("7/7/7/7/7/7/5RGBG b");
        Board after = new Board("7/7/7/7/7/7/5BG1 r");
        MovePair move = new MovePair(0, 1, 1);
        Board outcome = BitBoardUtils.makeMove(move, before);
        assertEquals(outcome, after);
    }

    @Test
    public void testCheckPlayerWon() {
        Board before = new Board("7/7/7/7/7/7/5BG1 r");
        assertTrue(BitBoardUtils.checkplayerWon(before, Player.BLUE));
    }
}