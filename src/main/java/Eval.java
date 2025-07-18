import java.util.List;

/*
 * Revised evaluation function for Guard & Towers AI.
 * Each feature block is preceded by a short comment that
 *  ❱ names the heuristic
 *  ❱ tells why it matters
 *  ❱ shows how strongly it is weighted in the final score.
 *
 * The score is always computed as (Red – Blue).  Positive ⇒ good for Red.
 */
public final class Eval {

    /*
     * ----------------------- Tunable weights -----------------------
     *  ‣ Large jumps between groups (1000 ≫ 100 ≫ 10) make sure that
     *    minor factors never override winning / losing a guard, while
     *    still letting them break ties when material is equal.
     */


    private static final int WIN_LOSS_WEIGHT = 45252;
    private static final int MATERIAL_PER_PIECE = 477;
    private static final int TOWER_EXTRA_PER_LEVEL = 27;
    private static final int CENTER_CONTROL_BONUS = 3;
    private static final int FILE_ALIGNED_GUARD_BONUS = 26;
    private static final int GUARD_PROGRESS_BONUS = 92;
    private static final int MOBILITY_PER_MOVE = 13;
    private static final int BLOCKED_TOWER_PENALTY = -21;
    private static final int GUARD_SAFETY_PER_FRIEND = 15;
    private static final int GUARD_THREAT_PER_ENEMY = -21;

    /*
     * Main entry – returns (score for RED – score for BLUE).
     */
    public static int evaluate(Board b) {
        int red = evaluateSide(b, Player.RED);
        int blue = evaluateSide(b, Player.BLUE);
        return red - blue;
    }

    public static int naiveEvaluate(Board b) {
        int red = naiveEvaluateSide(b, Player.RED);
        int blue = naiveEvaluateSide(b, Player.BLUE);
        return red - blue;
    }



    /*
     * Per‑side breakdown so that features stay readable.
     */
    public static int evaluateSide(Board board, Player side) {
        int score = 0;

        /* 1️⃣ Win/Loss detection – overrides everything else. */
        if (Board.checkplayerWon(board, side)) return WIN_LOSS_WEIGHT;
        if (Board.checkplayerWon(board, opposite(side))) return -WIN_LOSS_WEIGHT;

        /* 2️⃣ Material – each piece is worth 100 points. */
        int pieces = board.numPieces(side);
        score += pieces * MATERIAL_PER_PIECE;   // Simple but stable.
        //System.out.println("Material: " + score);

        /* 3️⃣ Tower height – add 15 pts for every extra stone above 1. */
        score += totalExtraTowerLevels(board, side) * TOWER_EXTRA_PER_LEVEL;
        //System.out.println("Tower Levels: " + score);
        /* 4️⃣ Center control – reward pieces in the 3×3 middle. */
        score += countInCenter(board, side) * CENTER_CONTROL_BONUS;
        //System.out.println("count in center: " + score);

        /* 5️⃣ Aligned attack – towers on same file/rank as enemy guard. */
        score += alignedWithEnemyGuard(board, side) * FILE_ALIGNED_GUARD_BONUS;
        //System.out.println("aligned with enemy guard: " + score);

        /* 6️⃣ Guard progress – Manhattan distance towards enemy castle. */
        score += (MAX_DISTANCE - guardDistanceToTarget(board, side)) * GUARD_PROGRESS_BONUS;
        //System.out.println("guard Distance to Target: " + score);

        /* 7️⃣ Mobility – each legal move gives +2 (cheap tie‑breaker). */
        score += MoveGenerator.generateAllLegalMoves(board).size() * MOBILITY_PER_MOVE;
        //System.out.println("Mobility per move: " + score);

        /* 8️⃣ Blocked towers – small penalty per own tower with no moves. */
        score += countBlockedTowers(board, side) * BLOCKED_TOWER_PENALTY;
        //System.out.println("Blocked Tower penalty: " + score);

        /* 9️⃣ Guard safety / threat within 2 squares. */
        score += friendsNearGuard(board, side) * GUARD_SAFETY_PER_FRIEND;
        //System.out.println("Friends near Guard: " + score);

        score += enemiesNearOurGuard(board, side) * GUARD_THREAT_PER_ENEMY;
        //System.out.println("Guard Threat per enemy: " + score);

        return score;
    }

    public static int naiveEvaluateSide(Board board, Player side){
        int score = 0;

        /* 1️⃣ Win/Loss detection – overrides everything else. */
        if (Board.checkplayerWon(board, side)) return WIN_LOSS_WEIGHT;
        if (Board.checkplayerWon(board, opposite(side))) return -WIN_LOSS_WEIGHT;

        /* 2️⃣ Material – each piece is worth 100 points. */
        int pieces = board.numPieces(side);
        score += pieces * MATERIAL_PER_PIECE;   // Simple but stable.

        return score;
    }

    /* ---------------- helper methods below – deliberately compact ---------------- */

    public static Player opposite(Player p) {
        return (p == Player.RED) ? Player.BLUE : Player.RED;
    }

    // counts extra levels above 1 for all towers of this side.
    public static int totalExtraTowerLevels(Board b, Player side) {
        int extra = 0;
        long mask = (side == Player.RED) ? b.getRed() : b.getBlue();
        for (int h = 1; h < 7; h++) {
            long bits = b.getStack(h) & mask;
            extra += Long.bitCount(bits);  // each bit = one tower of ≥(h+1) → +1 level
        }
        return extra;
    }

    private static final int[] CENTER_SQUARES = {15, 16, 17, 22, 23, 24, 29, 30, 31}; // 7×7 index

    public static int countInCenter(Board b, Player side) {
        long mask = (side == Player.RED) ? b.getRed() : b.getBlue();
        int cnt = 0;
        for (int idx : CENTER_SQUARES) if (((mask >> idx) & 1L) != 0) cnt++;
        return cnt;
    }

    public static int alignedWithEnemyGuard(Board b, Player side) {
        long myTowers = (side == Player.RED) ? b.getRed() : b.getBlue();
        long enemyGuard = b.getGuards() & ((side == Player.RED) ? b.getBlue() : b.getRed());
        if (enemyGuard == 0) return 0; // guard already captured – handled earlier
        int guardIdx = Long.numberOfTrailingZeros(enemyGuard);
        int gRow = guardIdx / 7, gCol = guardIdx % 7;
        int aligned = 0;
        for (int idx = 0; idx < 49; idx++)
            if (((myTowers >> idx) & 1L) != 0) {
                int r = idx / 7, c = idx % 7;
                if (r == gRow || c == gCol) aligned++;
            }
        return aligned;
    }

    private static final int[] CASTLE_INDEX = {3, 45}; // Red target, Blue target
    private static final int MAX_DISTANCE = 12; // Manhattan dist on 7×7 board ≤12

    public static int guardDistanceToTarget(Board b, Player side) {
        long guard = b.getGuards() & ((side == Player.RED) ? b.getRed() : b.getBlue());
        if (guard == 0) return MAX_DISTANCE; // captured – hopeless
        int idx = Long.numberOfTrailingZeros(guard);
        int r = idx / 7, c = idx % 7;
        int targetIdx = (side == Player.RED) ? CASTLE_INDEX[0] : CASTLE_INDEX[1];
        int tr = targetIdx / 7, tc = targetIdx % 7;
        return Math.abs(r - tr) + Math.abs(c - tc);
    }

    public static int countBlockedTowers(Board b, Player side) {
        int blocked = 0;
        List<MovePair> moves = MoveGenerator.generateAllLegalMoves(b);
        long mask = (side == Player.RED) ? b.getRed() : b.getBlue();
        boolean[] hasMove = new boolean[49];
        for (MovePair m : moves) {
            int from = m.from();
            hasMove[from] = true;
        }
        for (int idx = 0; idx < 49; idx++)
            if (((mask >> idx) & 1L) != 0) {
                if (!hasMove[idx]) blocked++;
            }
        return blocked;
    }

    public static int friendsNearGuard(Board b, Player side) {
        long guard = b.getGuards() & ((side == Player.RED) ? b.getRed() : b.getBlue());
        if (guard == 0) return 0;
        int g = Long.numberOfTrailingZeros(guard);
        int gr = g / 7, gc = g % 7;
        long mask = (side == Player.RED) ? b.getRed() : b.getBlue();
        int cnt = 0;
        for (int idx = 0; idx < 49; idx++)
            if (((mask >> idx) & 1L) != 0 && idx != g) {
                int r = idx / 7, c = idx % 7;
                if (Math.abs(r - gr) + Math.abs(c - gc) <= 2) cnt++;
            }
        return cnt;
    }

    public static int enemiesNearOurGuard(Board b, Player side) {
        long guard = b.getGuards() & ((side == Player.RED) ? b.getRed() : b.getBlue());
        if (guard == 0) return 0;
        int g = Long.numberOfTrailingZeros(guard);
        int gr = g / 7, gc = g % 7;
        long mask = (side == Player.RED) ? b.getBlue() : b.getRed();
        int cnt = 0;
        for (int idx = 0; idx < 49; idx++)
            if (((mask >> idx) & 1L) != 0) {
                int r = idx / 7, c = idx % 7;
                if (Math.abs(r - gr) + Math.abs(c - gc) <= 2) cnt++;
            }
        return cnt;
    }
}
