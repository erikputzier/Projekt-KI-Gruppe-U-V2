import java.util.List;

public class Main {
    public static void main(String[] args) {
        try {
            new Client().start();
        } catch (Exception exception) {
            exception.printStackTrace();
        }
    }
}