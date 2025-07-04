import org.junit.Test;

import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

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
        List<MovePair> movePairs = MoveGenerator.generateAllLegalMoves(board);
        List<Move> generatedMoves = new ArrayList<>();
        for (MovePair pair : movePairs) {
            generatedMoves.add(pair.toMove());
        }
        List<String> actualMoves = List.of("A7-A6-1", "A7-B7-1", "B7-A7-1", "B7-B6-1", "B7-C7-1", "C6-B6-1", "C6-C5-1", "C6-C7-1", "C6-D6-1", "D7-C7-1", "D7-D6-1", "D7-E7-1", "D5-C5-1", "D5-D4-1", "D5-D6-1", "D5-E5-1", "E6-D6-1", "E6-E5-1", "E6-E7-1", "E6-F6-1", "F7-E7-1", "F7-F6-1", "F7-G7-1", "G7-F7-1", "G7-G6-1");
        List<Move> actual = parseMoves(actualMoves);
        assertTrue(generatedMoves.containsAll(actual) && actual.containsAll(generatedMoves));
    }

    @Test
    public void moveGeneratorMidgameTest() {
        Board board = new Board("3RG3/1r25/7/3r3b42/1b1BG4/4b12/7 r");
        List<MovePair> movePairs = MoveGenerator.generateAllLegalMoves(board);
        List<Move> generatedMoves = new ArrayList<>();
        for (MovePair pair : movePairs) {
            generatedMoves.add(pair.toMove());
        }
        List<String> actualMoves = List.of("D7-C7-1", "D7-E7-1", "D7-D6-1", "B6-B7-1", "B6-B5-1", "B6-A6-1", "B6-C6-1", "B6-B4-2", "B6-D6-2", "D4-D5-1", "D4-D3-1", "D4-C4-1", "D4-D6-2", "D4-D2-2", "D4-B4-2", "D4-D1-3", "D4-A4-3");
        List<Move> actual = parseMoves(actualMoves);
        assertTrue(generatedMoves.containsAll(actual) && actual.containsAll(generatedMoves));
    }

    @Test
    public void moveGeneratorJumpingTest() {
        Board board = new Board("7/3RG3/7/3r23/3b13/3BG3/7 r");
        List<MovePair> movePairs = MoveGenerator.generateAllLegalMoves(board);
        List<Move> generatedMoves = new ArrayList<>();
        for (MovePair pair : movePairs) {
            generatedMoves.add(pair.toMove());
        }
        List<String> actualMoves = List.of("D6-D7-1", "D6-C6-1", "D6-E6-1", "D6-D5-1", "D4-D5-1", "D4-C4-1", "D4-B4-2", "D4-E4-1", "D4-F4-2", "D4-D3-1");
        List<Move> actual = parseMoves(actualMoves);
        assertTrue(generatedMoves.containsAll(actual) && actual.containsAll(generatedMoves));
    }

    @Test
    public void moveGeneratorLateStartTest() {
        Board board = new Board("3RG3/2r11r12/1r21r11r21/7/3b33/2b11b12/1b21BG3 b");
        List<MovePair> movePairs = MoveGenerator.generateAllLegalMoves(board);
        List<Move> generatedMoves = new ArrayList<>();
        for (MovePair pair : movePairs) {
            generatedMoves.add(pair.toMove());
        }
        List<String> actualMoves = List.of("B1-C1-1", "B1-A1-1", "B1-B2-1", "B1-B3-2", "C2-C1-1", "C2-D2-1", "C2-B2-1", "C2-C3-1", "E2-E1-1", "E2-F2-1", "E2-D2-1", "E2-E3-1", "D3-D2-1", "D3-E3-1", "D3-D4-1", "D3-C3-1", "D3-F3-2", "D3-G3-3", "D3-D5-2", "D3-B3-2", "D3-A3-3", "D1-E1-1", "D1-C1-1", "D1-D2-1");
        List<Move> actual = parseMoves(actualMoves);
        assertTrue(generatedMoves.containsAll(actual) && actual.containsAll(generatedMoves));
    }

    @Test
    public void moveGeneratorEndgameTest() {
        Board board = new Board("3RG3/7/7/7/4b11b1/4r4r11/3BG1b11 b");
        List<MovePair> movePairs = MoveGenerator.generateAllLegalMoves(board);
        List<Move> generatedMoves = new ArrayList<>();
        for (MovePair pair : movePairs) {
            generatedMoves.add(pair.toMove());
        }
        List<String> actualMoves = List.of("D1-C1-1", "D1-D2-1", "D1-E1-1", "E3-D3-1", "E3-E4-1", "E3-F3-1", "F1-E1-1", "F1-F2-1", "F1-G1-1", "G3-G2-1", "G3-F3-1", "G3-G4-1");
        List<Move> actual = parseMoves(actualMoves);
        assertTrue(generatedMoves.containsAll(actual) && actual.containsAll(generatedMoves));
    }

    @Test
    public void moveGeneratorTest6() {
        Board board = new Board("7/1b44b3/7/2BG4/3r13/2r1RG3/7 r");
        List<MovePair> movePairs = MoveGenerator.generateAllLegalMoves(board);
        List<Move> generatedMoves = new ArrayList<>();
        for (MovePair pair : movePairs) {
            generatedMoves.add(pair.toMove());
        }
        List<String> actualMoves = List.of("D3-D4-1", "D3-C3-1", "D3-E3-1", "C2-C3-1", "C2-B2-1", "C2-C1-1", "D2-E2-1", "D2-D1-1");
        List<Move> actual = parseMoves(actualMoves);
        assertTrue(generatedMoves.containsAll(actual) && actual.containsAll(generatedMoves));
    }

    @Test
    public void moveGeneratorTest7() {
        Board board = new Board("3RG1r21/7/3r22r3/7/3b53/7/1b21BG3 b");
        List<MovePair> movePairs = MoveGenerator.generateAllLegalMoves(board);
        List<Move> generatedMoves = new ArrayList<>();
        for (MovePair pair : movePairs) {
            generatedMoves.add(pair.toMove());
        }
        List<String> actualMoves = List.of("B1-A1-1", "B1-C1-1", "B1-B2-1", "B1-B3-2", "D1-D2-1", "D1-C1-1", "D1-E1-1", "D3-C3-1", "D3-E3-1", "D3-D4-1", "D3-D2-1", "D3-B3-2", "D3-F3-2", "D3-A3-3", "D3-G3-3", "D3-D5-2");
        List<Move> actual = parseMoves(actualMoves);
        assertTrue(generatedMoves.containsAll(actual) && actual.containsAll(generatedMoves));
    }

    @Test
    public void moveGeneratorTest8() {
        Board board = new Board("3RG1r21/7/3r53/7/3b53/7/1b21BG3 r");
        List<MovePair> movePairs = MoveGenerator.generateAllLegalMoves(board);
        List<Move> generatedMoves = new ArrayList<>();
        for (MovePair pair : movePairs) {
            generatedMoves.add(pair.toMove());
        }
        List<String> actualMoves = List.of("D7-C7-1", "D7-E7-1", "D7-D6-1", "F7-E7-1", "F7-G7-1", "F7-F6-1", "F7-F5-2", "D5-C5-1", "D5-E5-1", "D5-D6-1", "D5-D4-1", "D5-B5-2", "D5-F5-2", "D5-A5-3", "D5-G5-3");
        List<Move> actual = parseMoves(actualMoves);
        assertTrue(generatedMoves.containsAll(actual) && actual.containsAll(generatedMoves));
    }

    @Test
    public void moveGeneratorTest9() {
        Board board = new Board("7/3RG3/7/3r23/7/3BG3/7 r");
        List<MovePair> movePairs = MoveGenerator.generateAllLegalMoves(board);
        List<Move> generatedMoves = new ArrayList<>();
        for (MovePair pair : movePairs) {
            generatedMoves.add(pair.toMove());
        }
        List<String> actualMoves = List.of("D6-D5-1", "D6-D7-1", "D6-C6-1", "D6-E6-1", "D4-D3-1", "D4-D2-2", "D4-D5-1", "D4-C4-1", "D4-B4-2", "D4-E4-1", "D4-F4-2");
        List<Move> actual = parseMoves(actualMoves);
        assertTrue(generatedMoves.containsAll(actual) && actual.containsAll(generatedMoves));
    }

    @Test
    public void moveGeneratorTest10() {
        Board board = new Board("r1r11RG3/6r1/3r11r21/7/3b23/1b15/b12BG1b1b1 b");
        List<MovePair> movePairs = MoveGenerator.generateAllLegalMoves(board);
        List<Move> generatedMoves = new ArrayList<>();
        for (MovePair pair : movePairs) {
            generatedMoves.add(pair.toMove());
        }
        List<String> actualMoves = List.of("A1-A2-1", "A1-B1-1", "B2-B1-1", "B2-C2-1", "B2-A2-1", "B2-B3-1", "D1-C1-1", "D1-D2-1", "D1-E1-1", "F1-E1-1", "F1-F2-1", "F1-G1-1", "G1-G2-1", "G1-F1-1", "D3-D2-1", "D3-C3-1", "D3-D4-1", "D3-E3-1", "D3-D5-2", "D3-F3-2", "D3-B3-2");
        List<Move> actual = parseMoves(actualMoves);
        assertTrue(generatedMoves.containsAll(actual) && actual.containsAll(generatedMoves));
    }

    @Test
    public void testTowerHeightAt() {

        // Create a simple board with known positions
        // FEN format: position player
        // Position format:
        // - Digits represent empty squares
        // - 'r' followed by a digit represents a red tower of the specified height
        // - 'b' followed by a digit represents a blue tower of the specified height
        // - 'R' followed by 'G' represents a red guard
        // - 'B' followed by 'G' represents a blue guard
        // - '/' separates rows

        // Create a board programmatically
        Board board = new Board();

        // Clear the board
        board.setGuards(0L);
        board.setBlue(0L);
        board.setRed(0L);
        for (int i = 0; i < 7; i++) {
            board.setStack(i, 0L);
        }

        // Add a blue guard at D1 (index 3)
        long blueGuardBit = 1L << 3;
        board.setGuards(blueGuardBit);
        board.setBlue(blueGuardBit);

        // Add a red tower of height 2 at D2 (index 10)
        long redTowerBit = 1L << 10;
        board.setRed(board.getRed() | redTowerBit);

        // Add a blue tower of height 3 at D3 (index 17)
        long blueTowerBit = 1L << 17;
        board.setBlue(board.getBlue() | blueTowerBit);

        // Set up the stacks
        // Stack 0 contains all pieces (including guards)
        board.setStack(0, blueGuardBit | redTowerBit | blueTowerBit);
        // Stack 1 contains towers of height 2 or greater
        board.setStack(1, redTowerBit | blueTowerBit);
        // Stack 2 contains towers of height 3 or greater
        board.setStack(2, blueTowerBit);

        // The board is indexed from 48 (top-left) to 0 (bottom-right)
        // The FEN string is read from left to right, top to bottom

        // Test empty square (height 0)
        // A7 is at index 48 (top-left corner)
        long emptySquareBit = 1L << 48;
        int emptyHeight = MoveGenerator.towerHeightAt(emptySquareBit, board);
        assertEquals("Empty square should have height 0", 0, emptyHeight);

        // Test blue guard (height 1)
        // D1 is at index 3
        int blueGuardHeight = MoveGenerator.towerHeightAt(blueGuardBit, board);
        // According to the towerHeightAt method documentation, guards have a height of 1
        assertEquals("Blue guard should have height 1", 1, blueGuardHeight);

        // Test red tower of height 2
        // D2 is at index 10
        int redTowerHeight = MoveGenerator.towerHeightAt(redTowerBit, board);
        assertEquals("Red tower should have height 2", 2, redTowerHeight);

        // Test blue tower of height 3
        // D3 is at index 17
        int blueTowerHeight = MoveGenerator.towerHeightAt(blueTowerBit, board);
        assertEquals("Blue tower should have height 3", 3, blueTowerHeight);
    }

    @Test
    public void testGenerateNoisyMoves() {
        /*
         * A good unit-test for generateNoisyMoves should prove four things at once:
         * 1. Only "noisy" moves are produced (no quiet moves appear in the list)
         * 2. Guard-capture logic works (a guard can be taken)
         * 3. Tower-capture-height filter works:
         *    a. A legal capture by a tall-enough tower is returned
         *    b. An inadequate-height tower capture is not returned
         * 4. Nothing else slips through (count the moves and compare exact sets)
         */

        // Test 1: Verify that only noisy moves are produced (no quiet moves)
        // Board with both quiet and noisy moves: red tower at D5, blue tower at D4
        Board board = new Board("3RG3/7/3r13/3b13/7/7/3BG3 r");

        // Get all legal moves to verify quiet moves exist
        List<MovePair> allLegalMoves = MoveGenerator.generateAllLegalMoves(board);
        List<Move> allMoves = new ArrayList<>();
        for (MovePair pair : allLegalMoves) {
            allMoves.add(pair.toMove());
        }

        // Verify that quiet moves exist in all legal moves
        List<String> quietMoveStrings = List.of("D5-D6-1", "D5-C5-1", "D5-E5-1");
        List<Move> quietMoves = parseMoves(quietMoveStrings);
        assertTrue("Test board should have quiet moves", allMoves.containsAll(quietMoves));

        // Generate noisy moves
        List<MovePair> noisyMoves = MoveGenerator.generateNoisyMoves(board);
        List<Move> generatedNoisyMoves = new ArrayList<>();
        for (MovePair pair : noisyMoves) {
            generatedNoisyMoves.add(pair.toMove());
        }

        // 1. Verify no quiet moves are included
        for (Move quietMove : quietMoves) {
            assertFalse("Quiet move should not be in noisy moves", generatedNoisyMoves.contains(quietMove));
        }

        // Expected noisy move: Red tower at D5 captures blue tower at D4
        List<String> expectedNoisyMoveStrings = List.of("D5-D4-1");
        List<Move> expectedNoisyMoves = parseMoves(expectedNoisyMoveStrings);

        // 4. Verify exact set equality (nothing else slips through)
        assertEquals("Generated noisy moves should exactly match expected noisy moves", expectedNoisyMoves.size(), generatedNoisyMoves.size());
        assertTrue("Generated noisy moves should contain all expected noisy moves", generatedNoisyMoves.containsAll(expectedNoisyMoves));
        assertTrue("Expected noisy moves should contain all generated noisy moves", expectedNoisyMoves.containsAll(generatedNoisyMoves));

        // Test 2: Verify guard-capture logic works
        // Board with a red tower at D1 that can capture a blue guard at E1
        board = new Board("3RG3/7/7/7/7/7/3r1BG3 r");

        noisyMoves = MoveGenerator.generateNoisyMoves(board);
        generatedNoisyMoves = new ArrayList<>();
        for (MovePair pair : noisyMoves) {
            generatedNoisyMoves.add(pair.toMove());
        }

        // 2. Expected noisy move: Red tower at D1 captures blue guard at E1
        List<String> guardCaptureStrings = List.of("D1-E1-1");
        List<Move> guardCaptureMoves = parseMoves(guardCaptureStrings);

        // Verify guard capture works
        assertEquals("Generated guard capture moves should exactly match expected", guardCaptureMoves.size(), generatedNoisyMoves.size());
        assertTrue("Generated moves should contain guard capture", generatedNoisyMoves.containsAll(guardCaptureMoves));
        assertTrue("Expected guard capture should match generated moves", guardCaptureMoves.containsAll(generatedNoisyMoves));

        // Test 3: Verify tower-capture-height filter works
        // Board with:
        // - Red tower of height 1 at C2 adjacent to blue tower of height 2 at D2 (inadequate height)
        // - Red tower of height 3 at C1 adjacent to blue guard at E1 (adequate height)
        board = new Board("3RG3/7/7/7/7/2r1b23/2r31BG3 r");

        noisyMoves = MoveGenerator.generateNoisyMoves(board);
        generatedNoisyMoves = new ArrayList<>();
        for (MovePair pair : noisyMoves) {
            generatedNoisyMoves.add(pair.toMove());
        }

        // 3a & 3b. Verify tower-capture-height filter works
        // Only the tower with adequate height (height 3) should capture the blue guard
        // The tower with inadequate height should not capture the blue tower
        List<String> heightFilterMoveStrings = List.of("C1-E1-2");
        List<Move> heightFilterMoves = parseMoves(heightFilterMoveStrings);

        // Verify tower height filter works
        assertEquals("Generated height-filtered moves should exactly match expected", heightFilterMoves.size(), generatedNoisyMoves.size());
        assertTrue("Generated moves should contain adequate height tower capture", generatedNoisyMoves.containsAll(heightFilterMoves));
        assertTrue("Expected adequate height tower capture should match generated moves", heightFilterMoves.containsAll(generatedNoisyMoves));

        /* -----------------------------------------------------------------
         *  CASE 1  – Guard-capture must be returned, nothing else.
         * ----------------------------------------------------------------- */
        Board b1 = new Board();                         // fresh, empty board
        b1.setGuards(0L);
        b1.setBlue(0L);
        b1.setRed(0L);
        for (int i = 0; i < 7; i++) b1.setStack(i, 0L);

        long redTowerD2 = 1L << 10;   // D2  (height 1)
        long blueGuardD1 = 1L << 3;   // D1  (guard → height 1)

        b1.setRed(redTowerD2);
        b1.setBlue(blueGuardD1);
        b1.setGuards(blueGuardD1);
        b1.setStack(0, redTowerD2 | blueGuardD1);       // layer 0 contains both

        Set<String> expected1 = Set.of("D2-D1-1");
        Set<String> actual1 = new HashSet<>();
        for (MovePair mp : MoveGenerator.generateNoisyMoves(b1)) {
            actual1.add(mp.toMove().toAlgebraic());
        }
        assertEquals("Guard capture should be the only noisy move", expected1, actual1);

        /* -----------------------------------------------------------------
         *  CASE 2  – Tall red tower (h=3) captures a shorter blue tower.
         * ----------------------------------------------------------------- */
        Board b2 = new Board();
        b2.setGuards(0L);
        b2.setBlue(0L);
        b2.setRed(0L);
        for (int i = 0; i < 7; i++) b2.setStack(i, 0L);

        long tallRedD3 = 1L << 17;   // D3, height 3  → in layers 0/1/2
        long blueTowerD2 = 1L << 10;   // D2, height 1  → layer 0 only

        b2.setRed(tallRedD3);
        b2.setBlue(blueTowerD2);
        b2.setStack(0, tallRedD3 | blueTowerD2);
        b2.setStack(1, tallRedD3);
        b2.setStack(2, tallRedD3);

        Set<String> expected2 = Set.of("D3-D2-1");
        Set<String> actual2 = new HashSet<>();
        for (MovePair mp : MoveGenerator.generateNoisyMoves(b2)) {
            actual2.add(mp.toMove().toAlgebraic());
        }
        assertEquals("Tall tower capture should appear", expected2, actual2);

        /* -----------------------------------------------------------------
         *  CASE 3  – Short red tower (h=1) tries to capture taller blue (h=2);
         *           move must NOT be listed.
         * ----------------------------------------------------------------- */
        Board b3 = new Board();
        b3.setGuards(0L);
        b3.setBlue(0L);
        b3.setRed(0L);
        for (int i = 0; i < 7; i++) b3.setStack(i, 0L);

        long shortRedD3 = 1L << 17;   // D3, height 1  → layer 0
        long tallBlueD2 = 1L << 10;   // D2, height 2  → layers 0/1

        b3.setRed(shortRedD3);
        b3.setBlue(tallBlueD2);
        b3.setStack(0, shortRedD3 | tallBlueD2);
        b3.setStack(1, tallBlueD2);    // second layer only blue

        Set<String> actual3 = new HashSet<>();
        for (MovePair mp : MoveGenerator.generateNoisyMoves(b3)) {
            actual3.add(mp.toMove().toString());
        }
        assertTrue("Under-height capture must be rejected", actual3.isEmpty());
    }
}