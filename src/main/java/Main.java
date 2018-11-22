import java.rmi.AlreadyBoundException;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.List;

public class Main {
    public static void main(String[] args) throws AlreadyBoundException, RemoteException, NotBoundException {
        Process p1 = new Process(1, 3);
        Process p2 = new Process(2, 3);
        Process p3 = new Process(3, 3);

        List<Pair<Id, String>> p1Messages = new ArrayList<>();
        p1Messages.add(new Pair<>(new Id(3), "p1 -> p3"));
        p1Messages.add(new Pair<>(new Id(2), "p1 -> p2"));
        p1Messages.add(new Pair<>(new Id(3), "p1 -> p3"));
        p1Messages.add(new Pair<>(new Id(2), "p1 -> p2"));
        p1Messages.add(new Pair<>(new Id(3), "p1 -> p3"));
        p1.setOutbox(p1Messages);

        List<Pair<Id, String>> p2Messages = new ArrayList<>();
        p2Messages.add(new Pair<>(new Id(1), "p2 -> p1"));
        p2Messages.add(new Pair<>(new Id(3), "p2 -> p3"));
        p2Messages.add(new Pair<>(new Id(1), "p2 -> p1"));
        p2Messages.add(new Pair<>(new Id(3), "p2 -> p3"));
        p2Messages.add(new Pair<>(new Id(1), "p2 -> p1"));
        p2.setOutbox(p2Messages);

        List<Pair<Id, String>> p3Messages = new ArrayList<>();
        p3Messages.add(new Pair<>(new Id(2), "p3 -> p2"));
        p3Messages.add(new Pair<>(new Id(1), "p3 -> p1"));
        p3Messages.add(new Pair<>(new Id(2), "p3 -> p2"));
        p3Messages.add(new Pair<>(new Id(1), "p3 -> p1"));
        p3Messages.add(new Pair<>(new Id(2), "p3 -> p2"));
        p3.setOutbox(p3Messages);

        new Thread(p1).start();
        new Thread(p2).start();
        new Thread(p3).start();

        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            try {
                p1.flush();
                p2.flush();
                p3.flush();
                p1.unbind();
                p2.unbind();
                p3.unbind();
            } catch (RemoteException | NotBoundException e) {
                e.printStackTrace();
            }
        }));
    }
}
