public class Message {
    public final String message;
    public final OrderingBuffer Sm;
    public final Timestamp Vm;

    public Message(String message, OrderingBuffer Sm, Timestamp Vm) {
        this.message = message;
        this.Sm = Sm;
        this.Vm = Vm;
    }
}
