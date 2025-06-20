import org.junit.Test;
import static org.junit.Assert.*;
import java.util.List;
import java.util.ArrayList;
import java.lang.reflect.Method;

public class TimeManagerTest {

    /**
     * Test um den WerteBereich von "computeInstabilityFactor zu ermitteln
     */
    @Test
    public void testComputeInstabilityFactor(){
        // Startboard
        Board startBoard = new Board();
        List<MovePair> moves = MoveGenerator.generateAllLegalMoves(startBoard);
        System.out.println("Startboard Instabilityfactor: " +TimeManager.computeInstabilityFactor(startBoard, moves));

        // Guard kann geschlagen werden
        Board beatGuard = new Board("3RG3/7/7/7/4b11b1/3r41r11/3BG1b11 r");
        moves = MoveGenerator.generateAllLegalMoves(beatGuard);
        System.out.println("BeatGuard Instabilityfactor: " + TimeManager.computeInstabilityFactor(beatGuard, moves));
    }

    @Test
    public void testComputeGuardPressureFactor(){
        // Startboard
        Board startBoard = new Board();
        List<MovePair> moves = MoveGenerator.generateAllLegalMoves(startBoard);
        System.out.println("Startboard Instabilityfactor: " +TimeManager.computeGuardPressureFactor(startBoard));

    }


}
