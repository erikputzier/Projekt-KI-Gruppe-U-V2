import java.util.List;

/**
 * Helper class that converts our custom FEN notation for **Guard & Towers**
 * positions into the bit‑board representation used by the engine and offers
 * a couple of convenience functions (move generation, ASCII diagram).
 * <p>
 * <strong>Mini‑FEN grammar</strong> (rank 7 → 1, file a → g):
 * <ul>
 *   <li><code>rH</code> / <code>bH</code>  – red / blue tower of height <em>H</em> (one digit 1‑7)</li>
 *   <li><code>RG</code> / <code>BG</code>  – red / blue guard (height 1)</li>
 *   <li>empty squares       – a digit 1‑7 (multi‑digit allowed for convenience)</li>
 * </ul>
 * Rows are separated by ‘/’; a trailing space and <code>r</code> or <code>b</code>
 * indicate the side to move.
 */
public final class FenUtils {

    private FenUtils() {
    }

    /* ====================================================================== */
    /*  Public API                                                           */
    /* ====================================================================== */

    /**
     * Parse{@code fen}, build a {@link Board}, and run
     *
     * @param fen full FEN string (board+side to move)
     * @return list of legal moves for the side to move
     */
    public static List<BitBoardUtils.MovePair> generateLegalMovesFromFen(String fen) {
        if (fen == null || fen.isBlank()) throw new IllegalArgumentException("FEN must not be null/empty");

        String[] parts = fen.trim().split("\\s+");
        if (parts.length != 2)
            throw new IllegalArgumentException("FEN must contain a board description and the side to move");

        Board board = parseBoard(parts[0]);

        char side = parts[1].trim().toLowerCase().charAt(0);
        String player = switch (side) {
            case 'r' -> "R";
            case 'b' -> "B";
            default -> throw new IllegalArgumentException("Side to move must be 'r' or 'b'");
        };

        return new BitBoardUtils().generateAllLegalMoves(board);
    }

    /**
     * Pretty‑print the board in ASCII – handy for debugging.
     */
    public static void printBoard(String fen) {
        if (fen == null || fen.isBlank()) throw new IllegalArgumentException("FEN must not be null/empty");

        final int SIZE = BitBoardUtils.BOARD_SIZE;          // 7×7 board
        String[][] grid = new String[SIZE][SIZE];

        // decode board part (everything before the first whitespace)
        String boardPart = fen.trim().split("\\s+")[0];
        String[] ranks = boardPart.split("/");
        if (ranks.length != SIZE)
            throw new IllegalArgumentException("FEN must have " + SIZE + " ranks (found " + ranks.length + ")");

        for (int row = 0; row < SIZE; row++) {
            String rank = ranks[row];
            int col = 0;
            for (int i = 0; i < rank.length(); ) {
                char c = rank.charAt(i);

                /* ---------- empty squares ---------- */
                if (Character.isDigit(c)) {
                    int empties = 0;
                    while (i < rank.length() && Character.isDigit(rank.charAt(i))) {
                        empties = empties * 10 + (rank.charAt(i) - '0');
                        i++;
                    }
                    for (int k = 0; k < empties; k++) grid[row][col++] = "──";
                    continue;
                }

                /* ---------- guard tokens ---------- */
                if ((c == 'R' || c == 'B') && i + 1 < rank.length() && rank.charAt(i + 1) == 'G') {
                    grid[row][col++] = "" + c + 'G';
                    i += 2;
                    continue;
                }

                /* ---------- tower tokens ---------- */
                if ((c == 'r' || c == 'b') && i + 1 < rank.length() && Character.isDigit(rank.charAt(i + 1))) {
                    grid[row][col++] = "" + c + rank.charAt(i + 1);
                    i += 2;
                    continue;
                }

                throw new IllegalArgumentException("Invalid token at rank " + (SIZE - row) + ": '" + c + "'");
            }
        }

        /* ------------------------ render ------------------------ */
        for (int r = 0; r < SIZE; r++) {
            System.out.print((SIZE - r) + " │ ");
            for (int f = 0; f < SIZE; f++) System.out.printf("%-3s", grid[r][f]);
            System.out.println();
        }
        System.out.println("  └" + "─".repeat(SIZE * 3));
        System.out.print("    ");
        for (char file = 'a'; file < 'a' + SIZE; file++) System.out.print(file + "  ");
        System.out.println();
    }

    /* ====================================================================== */
    /*  Internal helpers                                                      */
    /* ====================================================================== */

    /**
     * Convert the board part of a FEN string into a {@link Board}.
     */
    public static Board parseBoard(String boardPart) {
        final int SIZE = BitBoardUtils.BOARD_SIZE;          // 7

        String[] ranks = boardPart.split("/");
        if (ranks.length != SIZE)
            throw new IllegalArgumentException("FEN must contain exactly " + SIZE + " ranks (found " + ranks.length + ")");

        long guards = 0L, blue = 0L, red = 0L;
        long[] stacks = new long[7];                       // height 1 … 7

        for (int row = 0; row < SIZE; row++) {
            String rank = ranks[row];
            int col = 0;
            for (int i = 0; i < rank.length(); ) {
                char c = rank.charAt(i);

                /* ---------- empty squares ---------- */
                if (Character.isDigit(c)) {
                    int empties = 0;
                    while (i < rank.length() && Character.isDigit(rank.charAt(i))) {
                        empties = empties * 10 + (rank.charAt(i) - '0');
                        i++;
                    }
                    col += empties;
                    continue;
                }

                /* ---------- piece token ---------- */
                boolean isRed;
                boolean isGuard = false;
                int height = 1;

                switch (c) {
                    case 'r' -> {
                        isRed = true;   // red tower – consume colour char
                        i++;
                    }
                    case 'b' -> {
                        isRed = false;  // blue tower – consume colour char
                        i++;
                    }
                    case 'R' -> {
                        isRed = true;   // red guard – expect 'G'
                        if (++i >= rank.length() || rank.charAt(i) != 'G')
                            throw new IllegalArgumentException("Expected 'G' after 'R' for a red guard");
                        isGuard = true;
                        i++;            // consume 'G'
                    }
                    case 'B' -> {
                        isRed = false;  // blue guard – expect 'G'
                        if (++i >= rank.length() || rank.charAt(i) != 'G')
                            throw new IllegalArgumentException("Expected 'G' after 'B' for a blue guard");
                        isGuard = true;
                        i++;            // consume 'G'
                    }
                    default -> throw new IllegalArgumentException("Unexpected character '" + c + "' in FEN");
                }

                if (!isGuard) { // tower – read exactly ONE height digit 1‑7
                    if (i >= rank.length() || !Character.isDigit(rank.charAt(i)))
                        throw new IllegalArgumentException("Missing height digit after '" + (isRed ? 'r' : 'b') + "' in FEN");
                    height = rank.charAt(i) - '0';
                    if (height < 1 || height > 7)
                        throw new IllegalArgumentException("Tower height must be 1‑7 (found " + height + ")");
                    i++; // consume height digit
                }

                /* ---------- store on bitboards ---------- */
                if (col >= SIZE) throw new IllegalArgumentException("Too many fields in rank " + (SIZE - row));

                long bit = 1L << (row * SIZE + col);

                // occupancy stacks
                for (int h = 0; h < height; h++) stacks[h] |= bit;

                if (isRed) red |= bit;
                else blue |= bit;
                if (isGuard) guards |= bit;

                col++; // advance to next column
            }

            if (col != SIZE)
                throw new IllegalArgumentException("Rank " + (SIZE - row) + " does not contain exactly " + SIZE + " fields (had " + col + ")");
        }

        return new Board(guards, blue, red, stacks, Player.BLUE);
    }

    /* ====================================================================== */
    /*  Demo                                                                  */
    /* ====================================================================== */

    public static void main(String[] args) {
        String fen = "7/6r3/1RG5/3b43/1r25/7/2BG3r1 r";

        // Pretty print the position
        System.out.println("Position:");
        printBoard(fen);

        // Generate and print all legal moves
        List<BitBoardUtils.MovePair> moves = generateLegalMovesFromFen(fen);
        System.out.println("\nLegal moves for '" + fen.charAt(fen.length() - 1) + "':");
        moves.forEach(mp -> System.out.println(mp.toMove() + " -> " + mp.toMove().toAlgebraic()));
        System.out.println(moves.size() + " moves in total");
    }
}