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

    @Test
    public void testBlueWinsByCaptureRedCastle() {
        // Blue has a guard on Red's castle (position 45)
        Board board = new Board("3BG3/7/7/7/7/7/5RG1 r");
        FenUtils.printBoard("3BG3/7/7/7/7/7/5RG1 r");
        assertTrue(Board.checkplayerWon(board, Player.BLUE));
    }

    @Test
    public void testRedWinsByCaptureBluesCastle() {
        // Red has a guard on Blue's castle (position 3)
        Board board = new Board("6BG/7/7/7/7/7/3RG3 b");
        FenUtils.printBoard("6BG/7/7/7/7/7/3RG3 b");
        assertTrue(Board.checkplayerWon(board, Player.RED));
    }

    @Test
    public void testBlueWinsBecauseRedHasNoGuards() {
        // Red has no guards left
        Board board = new Board("6BG/6r1/7/7/7/7/3b13 r");
        FenUtils.printBoard("6BG/6r1/7/7/7/7/3b13 r");
        assertTrue(Board.checkplayerWon(board, Player.BLUE));
    }

    @Test
    public void testRedWinsBecauseBlueHasNoGuards() {
        // Blue has no guards left
        Board board = new Board("6b1/7/7/7/7/7/4RG2 b");
        FenUtils.printBoard("6b1/7/7/7/7/7/4RG2 b");
        assertTrue(Board.checkplayerWon(board, Player.RED));
    }

    @Test
    public void testGameInProgress() {
        // Standard starting position, no one has won yet
        Board board = new Board();
        assertFalse(Board.checkplayerWon(board, Player.BLUE));
        assertFalse(Board.checkplayerWon(board, Player.RED));
    }
}