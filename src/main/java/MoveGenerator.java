import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MoveGenerator {
    public static final int BOARD_SIZE = 7;
    private static final Map<MovePair, Long> PATH_MASK_MAP = new HashMap<>();
    private static final long[] LEFT_MASKS = new long[BOARD_SIZE];
    private static final long[] RIGHT_MASKS = new long[BOARD_SIZE];
    private static final long FULL_MASK;

    static {
        FULL_MASK = (1L << 49) - 1;

        precomputePathMasks();                       // now a static helper

        long left = 1L << 6;
        long right = 1L;
        for (int i = 0; i < 6; i++) {
            left = (left << BOARD_SIZE) | left;
            right = (right << BOARD_SIZE) | right;
        }
        LEFT_MASKS[0] = left;
        RIGHT_MASKS[0] = right;

        for (int i = 1; i < BOARD_SIZE; i++) {
            LEFT_MASKS[i] = LEFT_MASKS[i - 1] | (LEFT_MASKS[i - 1] >>> 1);
            RIGHT_MASKS[i] = RIGHT_MASKS[i - 1] | (RIGHT_MASKS[i - 1] >>> 1);
        }
    }

    /**
     * Generates all Legal Moves in all Directions for a specific player. Boundary Conflicts and jumping violations are handled in generateMovesInDirection.
     *
     * @return List of MovePairs, giving all possible moves in all direction for the current state of the Game.
     */
    public static List<MovePair> generateAllLegalMoves(Board board) {
        long empty = ~board.getStack(0);
        List<MovePair> moves = new ArrayList<>();
        long playerMask = 0L;
        if (board.getCurrentPlayer() == Player.RED) {
            playerMask = board.getRed();
        } else if (board.getCurrentPlayer() == Player.BLUE) {
            playerMask = board.getBlue();
        }

        for (int i = 0; i < BOARD_SIZE; i++) {
            moves.addAll(generateMovesInDirection(board.getStack(i) & playerMask, empty, "N", i + 1, board)); // North
            moves.addAll(generateMovesInDirection(board.getStack(i) & playerMask, empty, "S", i + 1, board)); // South
            moves.addAll(generateMovesInDirection(board.getStack(i) & playerMask, empty, "E", i + 1, board)); // East
            moves.addAll(generateMovesInDirection(board.getStack(i) & playerMask, empty, "W", i + 1, board)); // West

        }

        return moves;
    }

    /**
     * Generates all Moves in a specific Direction. Does NOT check if pieces Jump over others for their move. DOES Check if Boundaries are violated.
     *
     * @param fromBits Bitboard containing starting positions of all relevant pieces
     * @param empty    Bitboard containing position of empty fields
     * @param dir      String giving the Direction for which the moves should be calculated (North-> "N",East-> "E", South-> "S", West-> "W" )
     * @param height   int specifying the Minimum height of the Stacks for which the Moves should be calculated. Also determines the Number of steps one Move has.
     * @return List of MovePairs, giving all possible moves which do not violate Boundary's for the specified Direction.
     */
    private static List<MovePair> generateMovesInDirection(long fromBits, long empty, String dir, int height, Board board) {
        List<MovePair> moves = new ArrayList<>();
        long shifted;
        int shift;
        long friendly;
        long enemy;
        if (board.getCurrentPlayer() == Player.BLUE) {
            friendly = board.getBlue();
            enemy = board.getRed();
        } else {
            friendly = board.getRed();
            enemy = board.getBlue();
        }
        long guardMoves = board.getGuards() & (friendly);


        //check Direction and shift by required amount
        fromBits &= ~(board.getGuards() & friendly);
        switch (dir) {
            case "E" -> {
                shift = height;
                fromBits &= ~RIGHT_MASKS[height - 1];
                shifted = (fromBits >>> shift) & FULL_MASK;
                guardMoves = ((guardMoves & ~RIGHT_MASKS[height - 1]) >>> shift) & ~(board.getStack(0) & friendly) & FULL_MASK;
            }
            case "W" -> {
                shift = height;
                fromBits &= ~LEFT_MASKS[height - 1];
                guardMoves = ((guardMoves & ~LEFT_MASKS[height - 1]) << shift) & ~(board.getStack(0) & friendly) & FULL_MASK;
                shifted = (fromBits << shift) & FULL_MASK;
            }
            case "N" -> {
                shift = BOARD_SIZE * height;
                shifted = (fromBits << shift) & FULL_MASK;
                guardMoves = (guardMoves << shift) & ~(board.getStack(0) & friendly) & FULL_MASK;
            }
            default -> {
                shift = BOARD_SIZE * height;
                shifted = (fromBits >>> shift) & FULL_MASK;
                guardMoves = (guardMoves >>> shift) & ~(board.getStack(0) & friendly) & FULL_MASK;
            }
        }
        //shifted ohne züge bei denen der eigene Guard das Ziel ist
        shifted = (shifted & ~(board.getGuards() & friendly));
        //shifted ohne züge bei denen höhere Türme geschlagen werden
        if (height < BOARD_SIZE) {
            shifted &= ~(board.getStack(height) & enemy);
        }
        //shifted mit legalen zügen für den Guard
        if (height == 1) {
            shifted |= guardMoves;
        }
        //extract from -> to sequences from shifted Bitboard
        while (shifted != 0) {
            int to = Long.numberOfTrailingZeros(shifted);
            int from;
            if (dir.equals("S") || dir.equals("E")) {
                from = to + shift;
            } else {
                from = to - shift;
            }
            MovePair move = new MovePair(from, to, height);
            //Checking for jumping violations and out of bounds violations
            if (from >= 0 && from < 49 && moveDoesntJump(move, board)) { //&& moveDoesntJump(move, board)
                moves.add(move);
            }
            shifted &= shifted - 1; //niedrigstes Bit löschen
        }
        return moves;
    }

    private static boolean moveDoesntJump(MovePair move, Board board) {
        if (PATH_MASK_MAP.get(move) == null) {
            return false;
        }
        return (board.getStack(0) & PATH_MASK_MAP.get(move)) == 0;
    }

    public static void precomputePathMasks() {
        for (int from = 0; from < 49; from++) {
            int x1 = from % BOARD_SIZE;
            int y1 = from / BOARD_SIZE;
            int height = 0;

            for (int to = 0; to < 49; to++) {
                if (from == to) continue;

                int x2 = to % BOARD_SIZE;
                int y2 = to / BOARD_SIZE;

                // Nur orthogonal (N, S, E, W)
                if (x1 == x2 || y1 == y2) {
                    long mask = 0L;

                    // Vertikal
                    if (x1 == x2) {
                        int yStart = Math.min(y1, y2) + 1;
                        int yEnd = Math.max(y1, y2);
                        for (int y = yStart; y < yEnd; y++) {
                            int index = y * BOARD_SIZE + x1;
                            mask |= 1L << index;
                        }
                        height = yEnd - (yStart - 1);
                    }

                    // Horizontal
                    if (y1 == y2) {
                        int xStart = Math.min(x1, x2) + 1;
                        int xEnd = Math.max(x1, x2);
                        for (int x = xStart; x < xEnd; x++) {
                            int index = y1 * BOARD_SIZE + x;
                            mask |= 1L << index;
                        }
                        height = xEnd - (xStart - 1);

                    }

                    PATH_MASK_MAP.put(new MovePair(from, to, height), mask);
                }
            }
        }
    }

    public static List<MovePair> generateNoisyMoves(Board b) {
        List<MovePair> all = MoveGenerator.generateAllLegalMoves(b);
        List<MovePair> noisy = new ArrayList<>();
        long guards = b.getGuards();
        long enemy = (b.getCurrentPlayer() == Player.RED) ? b.getBlue() : b.getRed();

        for (MovePair m : all) {
            long dest = 1L << m.to();
            if ((dest & guards) != 0) {               // guard capture
                noisy.add(m);
            } else if ((dest & enemy) != 0 && m.height() >= towerHeightAt(dest, b)) {
                noisy.add(m);                         // tower capture
            }
        }
        return noisy;
    }

    /**
     * Returns the exact height (1-7) of the tower or guard that
     * occupies the square represented by {@code bit}.  A guard
     * counts as height 1.  If the square is empty the method
     * returns 0, which is handy for sanity checks.
     *
     * @param bit   single-bit mask for the target square, e.g. (1L << idx)
     * @param board current board
     */
    public static int towerHeightAt(long bit, Board board) {
        // Fast bail-out: empty square → height 0
        if ((board.getStack(0) & bit) == 0) return 0;

    /*  Every stack layer i (0 = bottom) contains 1-bits for
        *all* towers of height ≥ (i + 1).  So we scan from the
        top layer down until we hit the first set bit – the
        corresponding (index + 1) is the real height.                      */
        for (int h = 6; h >= 1; h--) {
            if ((board.getStack(h) & bit) != 0) {
                return h + 1;          // first layer that still contains the bit
            }
        }
        return 1;
    }
}