import java.util.List;

public class Main {
    public static void main(String[] args) {
        Board board = new Board();
        board.printBoard();
        int numberOfTurns = 100;
        while (!(Board.checkplayerWon(board, Player.BLUE) && Board.checkplayerWon(board, Player.RED))) {

            List<MovePair> moves = MoveGenerator.generateAllLegalMoves(board);
            MovePair chosenMove = AI.pickMove(board);
            board = Board.makeMove(chosenMove, board);
            board.printBoard();

            numberOfTurns--;
        }
    }
}