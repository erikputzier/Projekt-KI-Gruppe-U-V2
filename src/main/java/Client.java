import com.google.gson.Gson;
import com.google.gson.annotations.SerializedName;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.nio.charset.StandardCharsets;

/**
 * Simple, self-contained network client for the instructor’s Python game-server.
 * It relies exclusively on already-existing engine classes
 * (Board, BitBoardUtils, Move, …) – no changes to the server code required.
 */
public class Client {

    /* ————————————————————————————————————  configuration  ———————————————————————————————————— */

    //private static final String SERVER_HOST = "GAME.guard-and-towers.com";
    private static final String SERVER_HOST = "localhost";
    private static final int SERVER_PORT = 5555;
    private static final int BUFFER_SIZE = 4_096;     // matches server-side recv-buffer

    /* ————————————————————————————————————  network fields  ———————————————————————————————————— */

    private Socket socket;
    private InputStream in;
    private OutputStream out;
    private final Gson gson = new Gson();

    /* ————————————————————————————————————game field———————————————————————————————————— */

    private char myTurnToken;      // 'r' or 'b'
    /* =================================================================================================================
                                              │ public bootstrap │
       ===============================================================================================================*/

    public static void main(String[] args) {
        try {
            new Client().start();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void start() throws IOException, InterruptedException {
        connect();
        gameLoop();
        close();
    }

    /* =================================================================================================================
                                              │ network helpers │
       ===============================================================================================================*/

    private void connect() throws IOException {
        socket = new Socket(SERVER_HOST, SERVER_PORT);
        in = socket.getInputStream();
        out = socket.getOutputStream();

        // The server sends a single byte: '0' or '1'
        int firstByte = in.read();
        if (firstByte == -1) throw new IOException("Server closed connection before sending player ID");
        // 0 = red, 1 = blue (as defined by the server)
        int playerId = firstByte - '0';
        myTurnToken = (playerId == 0) ? 'r' : 'b';

        System.out.printf("Connected – I am player %d (%s)%n", playerId, (myTurnToken == 'r' ? "RED" : "BLUE"));
    }

    private void close() {
        try {
            if (socket != null) socket.close();
        } catch (IOException ignored) {
        }
    }

    /**
     * Sends a payload (already a single JSON value) and waits for the server’s JSON reply.
     */
    private String sendAndReceive(String jsonValue) throws IOException {
        // encode and flush
        byte[] payload = jsonValue.getBytes(StandardCharsets.UTF_8);
        out.write(payload);
        out.flush();

        // read reply
        byte[] buf = new byte[BUFFER_SIZE];
        int len = in.read(buf);
        if (len == -1) throw new IOException("Server closed connection");
        return new String(buf, 0, len, StandardCharsets.UTF_8);
    }

    /* =================================================================================================================
                                              │ main game loop │
       ===============================================================================================================*/

    private void gameLoop() throws IOException, InterruptedException {

        GameState state = requestGameState();           // initial state
        while (!state.bothConnected) {                  // wait for opponent
            Thread.sleep(200);
            state = requestGameState();
        }

        while (!state.end) {

            boolean myTurn = (myTurnToken == state.turn.charAt(0));
            if (myTurn) {
                String moveStr = chooseMove(state.board);

                if (moveStr == null) {                  // no legal move – concede
                    System.err.println("No legal moves! Terminating.");
                    break;
                }
                System.out.println("This is the current baord:");
                FenUtils.printBoard(state.board);
                System.out.println("I play: " + moveStr);
                state = sendMove(moveStr);              // server responds with an updated state

            } else {
                // poll politely while the opponent thinks
                Thread.sleep(100);
                state = requestGameState();
            }
        }

        System.out.println("Game finished – server reported ‘end=true’. Closing connection.");
    }

    /* =================================================================================================================
                                              │ high-level helpers │
       ===============================================================================================================*/

    /**
     * Performs a single `"get"` round-trip.
     */
    private GameState requestGameState() throws IOException {
        String reply = sendAndReceive(gson.toJson("get"));
        //System.out.println(reply);
        return gson.fromJson(reply, GameState.class);
    }

    /**
     * Sends a move string (already validated by our engine) to the server and returns the resulting state.
     */
    private GameState sendMove(String move) throws IOException {
        String reply = sendAndReceive(gson.toJson(move));
        return gson.fromJson(reply, GameState.class);
    }


    /**
     * Builds a legal move for the current FEN and converts it into the server’s “A7-B7-1” format.
     */
    private String chooseMove(String fen) {
        try {
            MovePair choice = AI.pickMove(new Board(fen));
            return choice.toMove().toAlgebraic();

        } catch (Exception e) {                                 // any parsing / engine failure → no move
            e.printStackTrace();
            return null;
        }
    }



    /* =================================================================================================================
                                               │ helper record │
       ===============================================================================================================*/

    /**
     * Mirror of the JSON object the server sends after each request.
     * (Field names match exactly, so default GSON mapping works.)
     */
    private static class GameState {
        String board;                // FEN + side-to-move
        String turn;                 // "r" / "b"
        boolean bothConnected;       // both players ready
        @SerializedName("time")
        long timeMs;                 // remaining time in ms (server’s clock)
        boolean end;                 // true when the game is finished
    }
}