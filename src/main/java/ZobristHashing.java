import java.util.Random;

public class ZobristHashing {
    private static final int NUM_SQUARES = 49; // 7x7 board
    // Piece types: 0=BlueGuard, 1=RedGuard, 2-8=BlueTowerH1-H7, 9-15=RedTowerH1-H7
    private static final int NUM_PIECE_TYPES = 16;

    private static final long[][] zobristKeys = new long[NUM_PIECE_TYPES][NUM_SQUARES];
    private static long blueToMoveKey;
    private static final Random random = new Random(12345L); // Seed for reproducibility

    static {
        initializeKeys();
    }

    private static void initializeKeys() {
        for (int i = 0; i < NUM_PIECE_TYPES; i++) {
            for (int j = 0; j < NUM_SQUARES; j++) {
                zobristKeys[i][j] = random.nextLong();
            }
        }
        blueToMoveKey = random.nextLong();
    }

    public static long computeHash(Board board) {
        long hash = 0L;

        for (int sq = 0; sq < NUM_SQUARES; sq++) {
            long bit = 1L << sq;
            int pieceType = -1;

            if ((board.getGuards() & bit) != 0) { // Is it a guard?
                if ((board.getBlue() & bit) != 0) {
                    pieceType = 0; // Blue Guard
                } else if ((board.getRed() & bit) != 0) {
                    pieceType = 1; // Red Guard
                }
            } else if ((board.getStack(0) & bit) != 0) { // Is it a regular piece?
                int height = 0; // Actual height 1-7
                for (int hIdx = 6; hIdx >= 0; hIdx--) {
                    if (((board.getStack(hIdx) >> sq) & 1L) != 0) {
                        height = hIdx + 1;
                        break;
                    }
                }

                if ((board.getBlue() & bit) != 0) {
                    pieceType = 1 + height; // Blue Pawn H1-H7 (indices 2-8)
                } else if ((board.getRed() & bit) != 0) {
                    pieceType = 1 + 7 + height; // Red Pawn H1-H7 (indices 9-15)
                }
            }

            if (pieceType != -1) {
                hash ^= zobristKeys[pieceType][sq];
            }
        }

        if (board.getCurrentPlayer() == Player.BLUE) { // Conventionally, "blue to move"
            hash ^= blueToMoveKey;
        }
        return hash;
    }
}