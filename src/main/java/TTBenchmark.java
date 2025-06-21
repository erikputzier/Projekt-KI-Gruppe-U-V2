import java.util.Random;

public class TTBenchmark {

    private static final int PROBES = 2_000_000;

    public static void main(String[] args) {
        //bench("HashMap");
        bench("Array");
    }

    private static void bench(String tag) {
        Random rnd = new Random(42);
        long[] keys = rnd.longs(PROBES).toArray();
        TranspositionTableArray tt = new TranspositionTableArray();
        // warm-up
        for (long k : keys) tt.store(k, 0, (short)1, (byte) TranspositionTableArray.EXACT_SCORE, null);

        long t0 = System.nanoTime();
        for (long k : keys) {
            tt.retrieve(k);
            tt.store(k, 0, (short)1, (byte) TranspositionTableArray.EXACT_SCORE, null);
        }
        long nano = System.nanoTime() - t0;

        System.out.printf("%s  – %.1f M probes/s%n",
                tag, PROBES * 2 / (nano / 1e9));
    }
}
