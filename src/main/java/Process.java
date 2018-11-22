import java.rmi.AlreadyBoundException;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class Process implements Node, Runnable {
    private final Id id;
    private final Clock clock;
    private final ArrayList<Message> B;
    private final ArrayList<Message> delivered;
    private final OrderingBuffer S;
    private final Registry registry;
    private final Node node;
    private List<Pair<Id, String>> outbox;

    public Process(int id, int networkSize) throws RemoteException, AlreadyBoundException {
        this.id = new Id(id);
        this.S = new OrderingBuffer();
        this.clock = new VectorClock(id - 1, networkSize);
        this.B = new ArrayList<>();
        this.delivered = new ArrayList<>();

        Node stub = (Node) UnicastRemoteObject.exportObject(this, 0);
        this.registry = LocateRegistry.getRegistry("localhost", 1099);
        registry.bind(this.id.toString(), stub);
        this.node = stub;
    }

    public void run() {
        // Bind the remote object's stub in the registry
        System.err.println(this.id.toString() + ".run()");
        while (outbox != null && outbox.size() > 0) {
            Random random = new Random();
            try {
                Thread.sleep(random.nextInt(3000));
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            Pair<Id, String> outgoingMessage = outbox.get(0);
            outbox.remove(0);

            Id id = outgoingMessage.fst;
            try {
                Node stub = (Node) this.registry.lookup(id.toString());
                S.put(id, (VectorTimestamp) clock.stamp());
                clock.increment();
//                System.out.println("Send: " + outgoingMessage.snd + ", " + clock.stamp().toString());
                stub.receiveMessage(outgoingMessage.snd, S, clock.stamp());
            } catch (RemoteException | NotBoundException e) {
                e.printStackTrace();
            }
        }
        System.out.println("Process " + id + " done sending.");
    }

    public void unbind() throws RemoteException, NotBoundException {
        registry.unbind(this.id.toString());
    }

    @Override
    public void receiveMessage(String message, OrderingBuffer Sm, Timestamp Vm) throws RemoteException {
        clock.increment();
//        System.out.println("Receive: " + message + ", " + Vm.toString());
        Random random = new Random();
        try {
            Thread.sleep(random.nextInt(100));
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        if (canDeliver(Sm)) {
            deliver(message, Sm, Vm);
        } else {
            B.add(new Message(message, Sm, Vm));
            System.out.println("Cant deliver message " + Vm + " at time " + clock.stamp());
        }
    }

    public void setOutbox(List<Pair<Id, String>> outgoing) {
        this.outbox = outgoing;
    }

    @Override
    public void deliver(String message, OrderingBuffer Sm, Timestamp Vm) throws RemoteException {
        clock.update(Vm);
        clock.increment();
        S.merge(Sm);
//        System.out.println("Deliver: " + message + ", " + Vm.toString());
        this.delivered.add(new Message(message, Sm, Vm));
        for (Message m : B) {
            if (canDeliver(m.Sm)) {
                B.remove(m);
                clock.update(m.Vm);
                clock.increment();
                S.merge(m.Sm);
                System.out.println("Deliver: " + m.message + ", " + Vm.toString());
            } else {
                System.out.println("Cant deliver message " + m.Vm + " at time " + clock.stamp());
            }
        }
    }

    private boolean canDeliver(OrderingBuffer Sm) {
        return !Sm.contains(id) || !Sm.get(id).gt(clock.stamp());
    }

    public void flush() {
        if(this.delivered.size() != 5) {
            System.out.println("Process " + id + " is missing messages, has only " + delivered.size());
        }
        for(int i = 0 ; i < this.delivered.size() - 1; i++) {
            Timestamp prev = this.delivered.get(i).Vm;
            Timestamp next = this.delivered.get(i+1).Vm;
            if(prev.gt(next)) {
                System.out.println("Timestamp " + prev + "is not less than or concurrent with " + next);
                System.out.println("Causal ordering failed in Process " + id + " :(");
                return;
            }
        }
        System.out.println("Message order correct in Process " + id + "! :D");
    }
}
