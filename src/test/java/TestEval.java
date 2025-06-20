import org.junit.Test;
import static org.junit.Assert.*;
import java.util.List;
import java.util.ArrayList;
import java.lang.reflect.Method;

public class TestEval {

    @Test
    public void testStartEval(){
        Board startBoard = new Board();
        assertEquals(Eval.evaluate(startBoard), 0);
    }

    @Test
    public void testEvaluateSide(){
        Board startBoard = new Board();
        System.out.println("Eval Blue: " + Eval.evaluateSide(startBoard, Player.BLUE));
        System.out.println("Eval Red: " + Eval.evaluateSide(startBoard, Player.RED));
    }
}
