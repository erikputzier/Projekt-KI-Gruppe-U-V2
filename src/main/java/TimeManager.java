import java.util.List;

public class TimeManager {

    public static long computeTimeBudget(Board board, List<MovePair> moves, long baseTimeMs) {
        double mobilityScore = computeMobilityFactor(moves);
        //double instabilityScore = computeInstabilityFactor(board, moves);
        double pressureScore = computeGuardPressureFactor(board);
        double tensionScore = computeTacticalTension(board, moves);

        // Gewichtung je nach Bedeutung
        double weightedScore = 1.0 + (mobilityScore * 0.5 + pressureScore * 0.8 + tensionScore * 0.6);

        // Normalisierung
        double timeFactor = Math.min(5, 1.0 + weightedScore);  // max 5x Zeit

        // Alle Faktoren ausgeben lassen um Bewertung zu überprüfen
        //System.out.println("Mobility Score: " + mobilityScore);
        //System.out.println("Instability Score: " + instabilityScore);
        //System.out.println("Pressure Score: " + pressureScore);
        //System.out.println("Tension Score: " + tensionScore);

        return (long) (baseTimeMs * timeFactor);
    }

    //Wertebereich [1/10, 2], durchschnittlich wahrscheinlich um die 1.5
    private static double computeMobilityFactor(List<MovePair> moves) {
        int numMoves = moves.size();
        return Math.min(2.0, numMoves / 10.0); // 10 Züge = neutral, >20 = 2.0
    }

    // Wertebereich
    public static double computeInstabilityFactor(Board board, List<MovePair> moves) {
        int originalEval = Eval.evaluate(board);
        System.out.println("Original Eval: " + originalEval);
        double sum = 0;
        for (MovePair move : moves) {
            Board next = Board.makeMove(move, board.copy());
            int eval = Eval.evaluate(next);
            //quadrierte Abweichung der nächsten Bewertungen mit originaler Bewertunge als Mittelwert berechnen
            sum += Math.pow(eval - originalEval, 2);
        }
        double variance = sum / moves.size();
        return Math.sqrt(variance);
    }

    public static double computeGuardPressureFactor(Board board) {
        int distanceBlue = Eval.guardDistanceToTarget(board, Player.BLUE);
        int distanceRed = Eval.guardDistanceToTarget(board, Player.RED);
        System.out.println("Red Distance: " + distanceRed);
        System.out.println("Blue Distance: " + distanceBlue);

        // Normiere auf einen Maximalwert, z.B. 12 (maximale Manhattan-Distanz im 7x7)
        final int MAX_DIST = 6;

        double pressureBlue = 1.0 - (Math.min(distanceBlue, MAX_DIST) / (double) MAX_DIST);
        double pressureRed = 1.0 - (Math.min(distanceRed, MAX_DIST) / (double) MAX_DIST);

        // Beide Guards summieren
        return pressureBlue + pressureRed;  // Wertebereich: 0.0 (weit weg) bis 2.0 (beide am Ziel)
    }

    private static double computeTacticalTension(Board board, List<MovePair> moves) {
        int tensionCount = 0;
        long enemy = board.getCurrentPlayer() == Player.BLUE ? board.getRed() : board.getBlue();

        for (MovePair move : moves) {
            long to = 1L << move.to();
            if ((to & enemy) != 0) tensionCount++;
        }

        return tensionCount / 10.0; // >10 Drohungen = hoch
    }
}