/**
 * Helper class that converts our custom FEN notation for Guard&Towers
 * positions into the bit‑board representation used by the engine and offers
 * a couple of convenience functions
 */
public final class FenUtils {
    /**
     * Pretty‑print the board in ASCII – handy for debugging.
     */
    public static void printBoard(String fen) {
        if (fen == null || fen.isBlank()) throw new IllegalArgumentException("FEN must not be null/empty");

        final int SIZE = Board.BOARD_SIZE;          // 7×7 board
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
}