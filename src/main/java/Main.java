import java.util.ArrayList;
import java.util.List;

public class Main {
    public static void main(String[] args) {
        Process p1 = new Process(1, 3);
        Process p2 = new Process(2, 3);
        Process p3 = new Process(3, 3);

        List<Pair<Process, String>> p1Messages = new ArrayList<>();
        p1Messages.add(new Pair<>(p3, "Message p1 -> p3"));
        p1Messages.add(new Pair<>(p2, "Message p1 -> p2"));
        p1.setOutbox(p1Messages);

        List<Pair<Process, String>> p2Messages = new ArrayList<>();
        p2Messages.add(new Pair<>(p3, "Message p2 -> p3"));
        p2.setOutbox(p2Messages);

        p1.start();
        p2.start();
        p3.start();
    }
}
