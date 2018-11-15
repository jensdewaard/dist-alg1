import java.util.*;

public class Process extends Thread {
    private final Id id;
    private final Clock clock;
    private final ArrayList<Message> B;
    private final OrderingBuffer S;
    private List<Pair<Process, String>> outbox;

    public Process(int id, int networkSize) {
        this.id = new Id(id);
        this.S = new OrderingBuffer();
        this.clock = new VectorClock(id - 1, networkSize);
        this.B = new ArrayList<>();
    }

    public void run() {
        Random random = new Random();
        while (!Thread.currentThread().isInterrupted()) {
            try {
                    Thread.sleep(random.nextInt(3000));
                } catch (InterruptedException ex) {
            }
            for( Message m : B) {
                //System.out.println("Checking if messages can be delivered...");
                if(canDeliver(m.Sm)) {
                    B.remove(m);
                    deliver(m.message, m.Sm, m.Vm);
                }
            }
            if (outbox != null && outbox.size() > 0) {
                Pair<Process, String> outgoingMessage = outbox.get(0);
                outbox.remove(0);
                outgoingMessage.fst.receiveMessage(
                        outgoingMessage.snd,
                        S,
                        clock.stamp()
                );
                S.put(outgoingMessage.fst.id, (VectorTimestamp) clock.stamp());
                clock.increment();
                continue;
            }
            //System.out.println("Process " + this.id + ": No messages in inbox or outbox, idling...");

        }
    }

    public void receiveMessage(String message, OrderingBuffer Sm, Timestamp Vm) {
        //System.out.println("Receiving message..." + message);
        if(canDeliver(Sm)) {
            deliver(message, Sm, Vm);
        } else {
            B.add(new Message(message, Sm, Vm));
        }
        clock.increment();
    }

    public void setOutbox(List<Pair<Process, String>> outgoing) {
        this.outbox = outgoing;
    }

    private void deliver(String message, OrderingBuffer Sm, Timestamp Vm) {
        System.out.println(message);
        S.merge(Sm);
        clock.update(Vm);
        clock.increment();
        for(Message m : B) {
            if(canDeliver(m.Sm)) {
                B.remove(m);
                deliver(m.message, m.Sm, m.Vm);
            }
        }

    }

    private boolean canDeliver(OrderingBuffer Sm) {
       if(Sm.contains(id)) {
          return Sm.get(id).leq(clock.stamp());
       }
       return true;
    }

}
