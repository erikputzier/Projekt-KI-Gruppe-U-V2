import org.junit.Test;
import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.List;

public class MoveGeneratorTest {
    public static List<Move> parseMoves(List<String> moveStrings) {
        List<Move> moves = new ArrayList<>();
        for (String moveStr : moveStrings) {
            moves.add(parseMove(moveStr));
        }
        return moves;
    }

    public static Move parseMove(String moveStr) {
        // Format: "G6-G7-1"
        char fromColChar = moveStr.charAt(0);
        int fromRowNum = Character.getNumericValue(moveStr.charAt(1));
        char toColChar = moveStr.charAt(3);
        int toRowNum = Character.getNumericValue(moveStr.charAt(4));
        int height = Character.getNumericValue(moveStr.charAt(6));

        int fromCol = fromColChar - 'A';
        int fromRow = 7 - fromRowNum; // Zeile 1 → Index 6, Zeile 7 → Index 0
        int toCol = toColChar - 'A';
        int toRow = 7 - toRowNum;

        return new Move(fromRow, fromCol, toRow, toCol, height);
    }

    @Test
    public void moveGeneratorStartTest() {
        Board board = new Board("r1r11RG1r1r1/2r11r12/3r13/7/3b13/2b11b12/b1b11BG1b1b1 r");
        BitBoardUtils utils = new BitBoardUtils();
        List<BitBoardUtils.MovePair> movePairs = utils.generateAllLegalMoves(board);
        List<Move> generatedMoves = new ArrayList<>();
        for (BitBoardUtils.MovePair pair : movePairs) {
            generatedMoves.add(pair.toMove());
        }
        List<String> actualMoves = List.of("A7-A6-1", "A7-B7-1", "B7-A7-1", "B7-B6-1", "B7-C7-1", "C6-B6-1", "C6-C5-1", "C6-C7-1", "C6-D6-1", "D7-C7-1", "D7-D6-1", "D7-E7-1", "D5-C5-1", "D5-D4-1", "D5-D6-1", "D5-E5-1", "E6-D6-1", "E6-E5-1", "E6-E7-1", "E6-F6-1", "F7-E7-1", "F7-F6-1", "F7-G7-1", "G7-F7-1", "G7-G6-1");
        List<Move> actual = parseMoves(actualMoves);
        assertTrue(generatedMoves.containsAll(actual) && actual.containsAll(generatedMoves));
    }

    @Test
    public void moveGeneratorMidgameTest() {
        Board board = new Board("3RG3/1r25/7/3r3b42/1b1BG4/4b12/7 r");
        BitBoardUtils utils = new BitBoardUtils();
        List<BitBoardUtils.MovePair> movePairs = utils.generateAllLegalMoves(board);
        List<Move> generatedMoves = new ArrayList<>();
        for (BitBoardUtils.MovePair pair : movePairs) {
            generatedMoves.add(pair.toMove());
        }
        List<String> actualMoves = List.of("D7-C7-1", "D7-E7-1", "D7-D6-1", "B6-B7-1", "B6-B5-1", "B6-A6-1", "B6-C6-1", "B6-B4-2", "B6-D6-2", "D4-D5-1", "D4-D3-1", "D4-C4-1", "D4-D6-2", "D4-D2-2", "D4-B4-2", "D4-D1-3", "D4-A4-3");
        List<Move> actual = parseMoves(actualMoves);
        assertTrue(generatedMoves.containsAll(actual) && actual.containsAll(generatedMoves));
    }

    @Test
    public void moveGeneratorJumpingTest() {
        Board board = new Board("7/3RG3/7/3r23/3b13/3BG3/7 r");
        BitBoardUtils utils = new BitBoardUtils();
        List<BitBoardUtils.MovePair> movePairs = utils.generateAllLegalMoves(board);
        List<Move> generatedMoves = new ArrayList<>();
        for (BitBoardUtils.MovePair pair : movePairs) {
            generatedMoves.add(pair.toMove());
        }
        List<String> actualMoves = List.of("D6-D7-1", "D6-C6-1", "D6-E6-1", "D6-D5-1", "D4-D5-1", "D4-C4-1", "D4-B4-2", "D4-E4-1", "D4-F4-2", "D4-D3-1");
        List<Move> actual = parseMoves(actualMoves);
        assertTrue(generatedMoves.containsAll(actual) && actual.containsAll(generatedMoves));
    }

    @Test
    public void moveGeneratorLateStartTest() {
        Board board = new Board("3RG3/2r11r12/1r21r11r21/7/3b33/2b11b12/1b21BG3 b");
        BitBoardUtils utils = new BitBoardUtils();
        List<BitBoardUtils.MovePair> movePairs = utils.generateAllLegalMoves(board);
        List<Move> generatedMoves = new ArrayList<>();
        for (BitBoardUtils.MovePair pair : movePairs) {
            generatedMoves.add(pair.toMove());
        }
        List<String> actualMoves = List.of("B1-C1-1", "B1-A1-1", "B1-B2-1", "B1-B3-2", "C2-C1-1", "C2-D2-1", "C2-B2-1", "C2-C3-1", "E2-E1-1", "E2-F2-1", "E2-D2-1", "E2-E3-1", "D3-D2-1", "D3-E3-1", "D3-D4-1", "D3-C3-1", "D3-F3-2", "D3-G3-3", "D3-D5-2", "D3-B3-2", "D3-A3-3", "D1-E1-1", "D1-C1-1", "D1-D2-1");
        List<Move> actual = parseMoves(actualMoves);
        assertTrue(generatedMoves.containsAll(actual) && actual.containsAll(generatedMoves));
    }

    @Test
    public void moveGeneratorEndgameTest() {
        Board board = new Board("3RG3/7/7/7/4b11b1/4r4r11/3BG1b11 b");
        BitBoardUtils utils = new BitBoardUtils();
        List<BitBoardUtils.MovePair> movePairs = utils.generateAllLegalMoves(board);
        List<Move> generatedMoves = new ArrayList<>();
        for (BitBoardUtils.MovePair pair : movePairs) {
            generatedMoves.add(pair.toMove());
        }
        List<String> actualMoves = List.of("D1-C1-1", "D1-D2-1", "D1-E1-1", "E3-D3-1", "E3-E4-1", "E3-F3-1", "F1-E1-1", "F1-F2-1", "F1-G1-1", "G3-G2-1", "G3-F3-1", "G3-G4-1");
        board.printBoard();
        List<Move> actual = parseMoves(actualMoves);
        assertTrue(generatedMoves.containsAll(actual) && actual.containsAll(generatedMoves));
    }
}