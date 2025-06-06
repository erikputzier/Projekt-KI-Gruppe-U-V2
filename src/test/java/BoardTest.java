import org.junit.Test;

import static org.junit.Assert.*;

public class BoardTest {
    @Test
    public void boardFromFenTest() {
        Board startBoard = new Board();
        Board fenBoard = new Board("r1r11RG1r1r1/2r11r12/3r13/7/3b13/2b11b12/b1b11BG1b1b1 r");
        System.out.println("StartBoard");
        startBoard.printBoard();
        System.out.println("FenBoard");
        fenBoard.printBoard();
        assertEquals(startBoard, fenBoard);
    }

    @Test
    public void numPiecesTest() {
        Board board = new Board("7/7/3r1BG2/4r1RG1/7/7/7 r");
        assertEquals(3, board.numPieces(Player.RED));
        assertEquals(1, board.numPieces(Player.BLUE));

        board = new Board("r1r11RG1r1r1/2r11r12/3r13/7/3b13/2b11b12/b1b11BG1b1b1 r");
        assertEquals(8, board.numPieces(Player.BLUE));
        assertEquals(8, board.numPieces(Player.RED));
    }
}