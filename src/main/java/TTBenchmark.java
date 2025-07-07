import java.util.Arrays;
import java.util.Random;

public class TTBenchmark {

    private static final int PROBES = 2_000_000;

    // --- Adapter-Interface für Polymorphie ---
    interface TT {
        void store(long key, int score, short depth, byte type, MovePair best);

        Object retrieve(long key);
    }

    // --- Adapter für die HashMap-basierte Implementierung ---
    static class HashMapTT implements TT {
        private final TranspositionTable tt = new TranspositionTable();

        @Override
        public void store(long key, int score, short depth, byte type, MovePair best) {
            // Die TranspositionTable verwendet int für Tiefe und Typ, daher gibt es eine implizite Umwandlung
            tt.store(key, score, depth, type, best);
        }

        @Override
        public Object retrieve(long key) {
            return tt.retrieve(key);
        }
    }

    // --- Adapter für die Array-basierte Implementierung ---
    static class ArrayTT implements TT {
        private final TranspositionTableArray tt = new TranspositionTableArray();

        @Override
        public void store(long key, int score, short depth, byte type, MovePair best) {
            tt.store(key, score, depth, type, best);
        }

        @Override
        public Object retrieve(long key) {
            return tt.retrieve(key);
        }
    }

    public static void main(String[] args) {
        final int RUNS = 100;
        double[] hashMapResults = new double[RUNS];
        double[] arrayResults = new double[RUNS];

        for (int i = 0; i < RUNS; i++) {
            hashMapResults[i] = bench(new HashMapTT());
            //arrayResults[i] = bench(new ArrayTT());
        }

        double hashMapAvg = Arrays.stream(hashMapResults).average().orElse(Double.NaN);
        double arrayAvg = Arrays.stream(arrayResults).average().orElse(Double.NaN);

        System.out.printf("Average probes/s after %d runs:%n", RUNS);
        System.out.printf("%-10s – %5.1f M probes/s%n", "HashMap", hashMapAvg);
        System.out.printf("%-10s – %5.1f M probes/s%n", "Array", arrayAvg);
    }

    private static double bench(TT tt) {

        Random rnd = new Random(42);
        long[] keys = rnd.longs(PROBES).toArray();

        // Aufwärmen
        for (long k : keys) tt.store(k, 0, (short) 1, (byte) TranspositionTable.EXACT_SCORE, null);

        long t0 = System.nanoTime();
        for (long k : keys) {
            tt.retrieve(k);
            tt.store(k, 0, (short) 1, (byte) TranspositionTable.EXACT_SCORE, null);
        }
        long nano = System.nanoTime() - t0;

        return PROBES * 2 / (nano / 1e9);
    }
}