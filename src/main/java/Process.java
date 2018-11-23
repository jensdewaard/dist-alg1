import java.rmi.AlreadyBoundException;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.CopyOnWriteArrayList;

public class Process implements Node, Runnable {
    private final Id id;
    private final Clock clock;
    private final ArrayList<Message> B;
    private final ArrayList<Message> delivered;
    private final OrderingBuffer S;
    private final Registry registry;
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
    }

    public void run() {
        // Bind the remote object's stub in the registry
        System.err.println(this.id.toString() + ".run()");
        while (outbox != null && outbox.size() > 0) {
            Random random = new Random();
            try {
                Thread.sleep(random.nextInt(500));
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            Pair<Id, String> outgoingMessage = outbox.get(0);
            outbox.remove(0);

            Id receiverId = outgoingMessage.fst;
            clock.increment();
            OrderingBuffer bufferCopy = S.copy();
            Timestamp sendTime = clock.stamp();
            Runnable sendMessage = () -> {
                try {
                    Node stub = (Node) this.registry.lookup(receiverId.toString());

                    Thread.sleep(random.nextInt(2000));
                    stub.receiveMessage(outgoingMessage.snd, bufferCopy, sendTime);
                } catch (RemoteException | NotBoundException | InterruptedException e) {
                    e.printStackTrace();
                }
            };
            new Thread(sendMessage).start();
            S.put(receiverId, (VectorTimestamp) clock.stamp());
        }
    }

    void unbind() throws RemoteException, NotBoundException {
        registry.unbind(this.id.toString());
    }

    @Override
    public void receiveMessage(String message, OrderingBuffer Sm, Timestamp Vm) throws RemoteException {
        if (canDeliver(Sm)) {
            System.out.println("" + id + ": Received and Delivered " + message + " from time " + Vm + " at time " + clock.stamp());
            deliver(message, Sm.copy(), Vm.copy());
        } else {
            B.add(new Message(message, Sm.copy(), Vm.copy()));
            System.out.println("" + id + ": Cant deliver message " + message + " yet at time " + clock.stamp() + ", requires " + Sm.get(id));
        }
    }

    void setOutbox(List<Pair<Id, String>> outgoing) {
        this.outbox = outgoing;
    }

    private void deliver(String message, OrderingBuffer Sm, Timestamp Vm) {
        clock.update(Vm);
        clock.increment();
        S.merge(Sm);
        System.out.println(id + ": Updated Sm, new value = " + S);
        this.delivered.add(new Message(message, Sm, Vm));
        List<Message> messageBuffer = (ArrayList<Message>) B.clone();
        for (Message m : messageBuffer) {
            if (canDeliver(m.Sm)) {
                B.remove(m);
                clock.update(m.Vm);
                clock.increment();
                S.merge(m.Sm);
                System.out.println(id + ": Updated Sm, new value = " + S);
                System.out.println("" + id + ": Deliver " + m.message + ", " + m.Vm.toString());
                this.delivered.add(m);
            } else {
                System.out.println("" + id + ": Cant deliver message " + m.Vm + " at time " + clock.stamp() + ", requires " + m.Sm.get(id));
            }
        }
    }

    private boolean canDeliver(OrderingBuffer Sm) {
        System.out.println(id + ": " + Sm);
        if(Sm.contains(id)) {
            return Sm.get(id).leq(clock.stamp());
        }
        return true;

    }

    void flush() {
        if(this.delivered.size() != 5) {
            System.out.println("Process " + id + " is missing messages, has only " + delivered.size());
            return;
        }
        for(int i = 0 ; i < this.delivered.size() - 1; i++) {
            VectorTimestamp prev = (VectorTimestamp) this.delivered.get(i).Vm;
            VectorTimestamp next = (VectorTimestamp) this.delivered.get(i+1).Vm;
            if(prev.gt(next)) {
                System.out.println("Timestamp " + prev + " is not less than or concurrent with " + next);
                System.out.println("Causal ordering failed in Process " + id + " :(");
                return;
            }
        }
        System.out.println("Message order correct in Process " + id + "! :D (" + delivered.size() + " messages)");
    }
}
