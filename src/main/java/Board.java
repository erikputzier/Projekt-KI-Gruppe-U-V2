import java.util.*;

public class Board {
    public static final int BOARD_SIZE = 7;
    private long guards;
    private long blue;
    private long red;
    // Stacks indicate the minimum of how many pieces a tower contains
    // a Tower with three pieces has "1" entries in Stacks 0,1,2 and "0" entries in all Stacks above
    private long[] stacks = {0L, 0L, 0L, 0L, 0L, 0L, 0L};
    private Player currentPlayer;

    /**
     * Constructor to create a specific Board according to Parameters
     */
    public Board(long guards, long blue, long red, long[] stacks, Player player) {
        this.guards = guards;
        this.blue = blue;
        this.red = red;
        this.stacks = stacks;
        this.currentPlayer = player;
    }

    /**
     * Constructor to create the starting Board
     */
    public Board() {
        this.guards = 1L << 3 | 1L << 45;
        this.blue = 1L | 1L << 1 | 1L << 3 | 1L << 5 | 1L << 6 | 1L << 9 | 1L << 11 | 1L << 17;
        this.red = 1L << 31 | 1L << 37 | 1L << 39 | 1L << 42 | 1L << 43 | 1L << 45 | 1L << 47 | 1L << 48;
        this.stacks[0] = this.blue | this.red;
        for (int i = 1; i < 7; i++) {
            this.stacks[i] = 0L;
        }
        this.currentPlayer = Player.RED;
    }

    public Board(String fen) {
        String[] splitFen = fen.split(" ");
        String positionString = splitFen[0];
        String playerString = splitFen[1];

        this.guards = 0L;
        this.blue = 0L;
        this.red = 0L;

        int boardIndex = 48;
        for (int i = 0; i < positionString.length(); ) {
            char c = positionString.charAt(i);

            if (Character.isDigit(c)) {
                // Empty Fields
                int empty = c - '0';
                boardIndex -= empty;
                i++;
            } else if ((c == 'r') && i + 1 < positionString.length() && Character.isDigit(positionString.charAt(i + 1))) {
                // Red Tower
                int height = positionString.charAt(i + 1) - '0' - 1;
                // populate Stack corresponding to height
                stacks[height] = stacks[height] | (1L << boardIndex);
                red |= (1L << boardIndex);
                // move String index
                i += 2;
                // move Board index
                boardIndex -= 1;
            } else if ((c == 'b') && i + 1 < positionString.length() && Character.isDigit(positionString.charAt(i + 1))) {
                // Blue Tower
                int height = positionString.charAt(i + 1) - '0' - 1;
                // populate Stack corresponding to height and color Bitboard
                stacks[height] = stacks[height] | (1L << boardIndex);
                blue |= (1L << boardIndex);
                // move String index
                i += 2;
                // move Board index
                boardIndex -= 1;
            } else if (c == 'R') {
                // Red Guard
                //populate guard and color bitboard
                guards |= 1L << boardIndex;
                red |= (1L << boardIndex);
                // move String index
                i += 2;
                //move board index
                boardIndex -= 1;
            } else if (c == 'B') {
                // Blue Guard
                //populate guard and color bitboard
                guards |= 1L << boardIndex;
                blue |= (1L << boardIndex);
                // move String index
                i += 2;
                //move board index
                boardIndex -= 1;
            } else if (c == '/') {
                i += 1;
            } else {
                throw new IllegalArgumentException("Unbekanntes FEN-Element bei Index " + i + ": " + positionString.substring(i));
            }
        }
        //current Player setzen
        if (Objects.equals(playerString, "r")) {
            this.currentPlayer = Player.RED;
        } else if (Objects.equals(playerString, "b")) {
            this.currentPlayer = Player.BLUE;
        }

        // connect stacks so that they indicate minimum height and not absolute height
        this.stacks[0] = stacks[0] | stacks[1] | stacks[2] | stacks[3] | stacks[4] | stacks[5] | stacks[6] | guards;
        this.stacks[1] = stacks[1] | stacks[2] | stacks[3] | stacks[4] | stacks[5] | stacks[6];
        this.stacks[2] = stacks[2] | stacks[3] | stacks[4] | stacks[5] | stacks[6];
        this.stacks[3] = stacks[3] | stacks[4] | stacks[5] | stacks[6];
        this.stacks[4] = stacks[4] | stacks[5] | stacks[6];
        this.stacks[5] = stacks[5] | stacks[6];
    }

    public void setGuards(long guards) {
        this.guards = guards;
    }

    public void setBlue(long blue) {
        this.blue = blue;
    }

    public void setRed(long red) {
        this.red = red;
    }

    public void setStack(int i, long stack) {
        this.stacks[i] = stack;
    }

    public Player getCurrentPlayer() {
        return currentPlayer;
    }

    public void setCurrentPlayer(Player currentPlayer) {
        this.currentPlayer = currentPlayer;
    }

    public long getGuards() {
        return guards;
    }

    public long getBlue() {
        return blue;
    }

    public long getRed() {
        return red;
    }

    public long getStack(int i) {
        return stacks[i];
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Board)) {
            return false;
        }
        boolean guardsEqual = this.guards == ((Board) o).getGuards();
        boolean blueEqual = this.blue == ((Board) o).getBlue();
        boolean redEqual = this.red == ((Board) o).getRed();
        boolean currentPlayerEqual = this.currentPlayer == ((Board) o).getCurrentPlayer();
        boolean stacksEqual = true;
        for (int i = 0; i < 7; i++) {
            stacksEqual = stacksEqual && this.stacks[i] == ((Board) o).getStack(i);
        }
        return guardsEqual && blueEqual && redEqual && currentPlayerEqual && stacksEqual;
    }

    @Override
    public int hashCode() {
        int result = Objects.hash(this.blue, this.red, this.guards);
        result = 31 * result + Arrays.hashCode(this.stacks);
        return result;
    }

    /**
     * @param player for which the number of pieces should be calculated
     * @return returns the number of pieces of the given player on the board
     */
    public int numPieces(Player player) {
        int numPiece = 0;
        long playerMask = 0;
        if (player == Player.RED) {
            playerMask = this.red;
        } else if (player == Player.BLUE) {
            playerMask = this.blue;
        }

        for (int i = 0; i < 7; i++) {
            long colorStackI = stacks[i] & playerMask;
            for (int j = 0; j < 49; j++) {
                if (((colorStackI >>> j) & 1L) != 0) {
                    numPiece += 1;
                }
            }
        }
        return numPiece;
    }

    /**
     * Method to check if the Player who has just made a move has won the game.
     *
     * @param board  Board which is checked for a winner.
     * @param player Player who has just made a move.
     * @return boolean true if the player has won the game, false if not.
     */
    public static boolean checkplayerWon(Board board, Player player) {
        long playerMask;
        long enemyCastle;
        long enemyMask;
        if (player == Player.BLUE) {
            playerMask = board.getBlue();
            enemyMask = board.getRed();
            enemyCastle = 1L << 45;
        } else if (player == Player.RED) {
            playerMask = board.getRed();
            enemyMask = board.getBlue();
            enemyCastle = 1L << 3;
        } else {
            throw new RuntimeException("Wrong player input in checkplayerWon");
        }

        return (board.getGuards() & playerMask) == enemyCastle || (board.getGuards() & enemyMask) == 0;
    }

    public static Board makeMove(MovePair move, Board board) {
        long to = (1L << move.to());

        long from = (1L << move.from());
        long friendly;
        long enemy;

        if (board.getCurrentPlayer() == Player.BLUE) {
            friendly = board.getBlue();
            enemy = board.getRed();
        } else {
            friendly = board.getRed();
            enemy = board.getBlue();
        }

        //Delete "From" Position
        int n = move.height();
        for (int i = 6; i >= 0; i--) {
            //If there is a bit present at the "from" position the ^= operation will lead to that bit being deleted which means the height of the Stack at that position will be decreased by 1
            if ((board.getStack(i) | from) == board.getStack(i)) {
                board.setStack(i, board.getStack(i) ^ from);
                n--;
            }
            if (n == 0) {
                break;
            }
        }
        //update friendly to include the removal of the "from" position
        if (board.getCurrentPlayer() == Player.BLUE) {
            board.setBlue(board.getBlue() & board.getStack(0));
        } else {
            board.setRed(board.getRed() & board.getStack(0));
        }

        //delete beaten enemy Stack
        for (int i = 0; i < BOARD_SIZE; i++) {
            board.setStack(i, (board.getStack(i) & enemy ^ to & board.getStack(i)) | (friendly & board.getStack(i)));
        }
        //update enemy to include the removal of beaten stack
        if (board.getCurrentPlayer() == Player.BLUE) {
            board.setRed(enemy & board.getStack(0));
        } else {
            board.setBlue((enemy & board.getStack(0)));
        }

        //increase Stacks which player who moved owns
        n = move.height();
        for (int i = 0; i < BOARD_SIZE; i++) {
            //If there is no bit present at the "to" position the | operation will lead to that bit being added which means the height of the Stack at that position will be increased by 1
            if ((board.getStack(i) | to) != board.getStack(i)) {
                board.setStack(i, board.getStack(i) | to);
                n--;
            }
            if (n == 0) {
                break;
            }
        }

        //update friendly to include the increased Stack
        if (board.getCurrentPlayer() == Player.BLUE) {
            board.setBlue(board.getStack(0) ^ board.getRed());
        } else {
            board.setRed(board.getStack(0) ^ board.getBlue());
        }


        // update guard mask
        if ((board.getGuards() | from) == board.getGuards()) {
            board.setGuards(board.getGuards() ^ from ^ to | to);
        } else if ((board.getGuards() | to) == board.getGuards()) {
            board.setGuards(board.getGuards() ^ to);
        }

        //update currentPlayer
        if (board.getCurrentPlayer() == Player.BLUE) {
            board.setCurrentPlayer(Player.RED);
        } else if (board.getCurrentPlayer() == Player.RED) {
            board.setCurrentPlayer(Player.BLUE);
        }

        return board;
    }

    public Board copy() {
        Board b = new Board();
        b.setBlue(this.blue);
        b.setRed(this.red);
        b.setGuards(this.guards);

        for (int i = 0; i < 7; i++) {
            b.setStack(i, this.stacks[i]);        // copy the stacks
        }
        b.setCurrentPlayer(this.currentPlayer);   // copy the side to move
        return b;
    }
}