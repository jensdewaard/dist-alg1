public class Message {
    public final String message;
    public final OrderingBuffer Sm;
    public final Timestamp Vm;

    public Message(String message, OrderingBuffer Sm, Timestamp Vm) {
        this.message = message;
        this.Sm = Sm;
        this.Vm = Vm;
    }

    public boolean equals(Object o) {
        Message other = (Message) o;
        return this.message.equals(other.message) &&
                this.Sm.equals(other.Sm) &&
                this.Vm.equals(other.Vm);
    }

    public int hashCode() {
        return message.hashCode() + Sm.hashCode() + Vm.hashCode();
    }
}
