import java.util.List;

public class TimeManager {

    public static long computeTimeBudget(Board board,List<MovePair> moves, long baseTimeMs) {
        double mobilityScore = computeMobilityFactor(board, moves);
        double instabilityScore = computeInstabilityFactor(board, moves);
        double pressureScore = computeGuardPressureFactor(board);
        double tensionScore = computeTacticalTension(board);

        // Gewichtung je nach Bedeutung
        double weightedScore =
                mobilityScore * 0.4
                        + instabilityScore * 1.0
                        + pressureScore * 0.8
                        + tensionScore * 0.6;

        // Normalisierung
        double timeFactor = Math.min(6.0, 1.0 + weightedScore * 2.0);  // max 4x Zeit

        return (long) (baseTimeMs * timeFactor);
    }

    private static double computeMobilityFactor(Board board, List<MovePair> moves) {
        int numMoves =moves.size();
        return Math.min(2.0, numMoves / 10.0); // 10 Züge = neutral, >20 = 2.0
    }

    private static double computeInstabilityFactor(Board board, List<MovePair> moves) {
        int originalEval = Eval.evaluate(board);

        double sum = 0;
        for (MovePair move : moves) {
            Board next = Board.makeMove(move, board.copy());
            int eval = Eval.evaluate(next);
            sum += Math.abs(eval - originalEval);
        }
        return sum / Math.max(1, moves.size());
    }

    private static double computeGuardPressureFactor(Board board) {
        int distanceBlue = Eval.guardDistanceToTarget(board, Player.BLUE);
        int distanceRed = Eval.guardDistanceToTarget(board, Player.RED);

        // Normiere auf einen Maximalwert, z.B. 12 (maximale Manhattan-Distanz im 7x7)
        final int MAX_DIST = 12;

        double pressureBlue = 1.0 - (Math.min(distanceBlue, MAX_DIST) / (double) MAX_DIST);
        double pressureRed = 1.0 - (Math.min(distanceRed, MAX_DIST) / (double) MAX_DIST);

        // Beide Guards summieren
        return pressureBlue + pressureRed;  // Wertebereich: 0.0 (weit weg) bis 2.0 (beide am Ziel)
    }

    private static double computeTacticalTension(Board board) {
        int tensionCount = 0;
        List<MovePair> moves = MoveGenerator.generateAllLegalMoves(board);
        long enemy = board.getCurrentPlayer() == Player.BLUE ? board.getRed() : board.getBlue();

        for (MovePair move : moves) {
            long to = 1L << move.to();
            if ((to & enemy) != 0) tensionCount++;
        }

        return tensionCount / 10.0; // >10 Drohungen = hoch
    }






}
