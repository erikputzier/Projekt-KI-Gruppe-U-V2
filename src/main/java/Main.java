import java.util.List;

public class Main {
    public static void main(String[] args) {
        TranspositionTableArray tt = new TranspositionTableArray();
        long key = 0x1234ABCDL;
        MovePair mv = new MovePair(10, 17, 1);

        tt.store(key, 42, (short) 6, (byte) TranspositionTableArray.EXACT_SCORE, mv);

        TranspositionTableArray.TTEntry e = tt.retrieve(key);
        assert e != null && e.score == 42 && e.best.equals(mv);
        System.out.println("Hit OK, depth=" + e.depth);
        try {
            new Client().start();
        } catch (Exception exception) {
            exception.printStackTrace();
        }
    }
}