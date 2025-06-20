import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;

public class ZobristHashingTest {
    @Test
    public void sideToMoveMatters() {
        Board a = new Board();                   // default = Red to move
        Board b = a.copy();
        b.setCurrentPlayer(Player.BLUE);
        assertNotEquals(ZobristHashing.computeHash(a), ZobristHashing.computeHash(b));
    }

    @Test
    public void identicalBoardsGiveIdenticalHashes() {
        String fen = "r1r11RG1r1r1/2r11r12/3r13/7/3b13/2b11b12/b1b11BG1b1b1 r";
        Board x = new Board(fen);
        Board y = new Board(fen);
        assertEquals(ZobristHashing.computeHash(x), ZobristHashing.computeHash(y));
    }

    @Test
    public void moveAndUndoRestoresHash() {
        Board pos = new Board();
        long start = ZobristHashing.computeHash(pos);

        MovePair m = new MovePair(45, 38, 1);          // RED moves its guard
        Board after = Board.makeMove(m, pos.copy());

        Board undoBase = after.copy();
        undoBase.setCurrentPlayer(Player.RED);          // ← restore the mover
        Board back = Board.makeMove(new MovePair(m.to(), m.from(), 1), undoBase);
        back.setCurrentPlayer(Player.RED);
        assertEquals(start, ZobristHashing.computeHash(back));
    }
}