import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

public class OrderingBuffer implements Serializable {
    Map<Id, VectorTimestamp> buffer;

    public OrderingBuffer() {
        buffer = new HashMap<>();
    }

    private OrderingBuffer(HashMap<Id, VectorTimestamp> buffer) {
        this.buffer = buffer;
    }

    public void put(Id id, VectorTimestamp ts) {
        buffer.put(id, ts);
    }

    public boolean contains(Id id) {
       return buffer.containsKey(id);
    }

    public VectorTimestamp get(Id id) {
        return buffer.get(id);
    }

    public void merge(OrderingBuffer other) {
        other.buffer.forEach((id, ts) -> {
            if(!buffer.containsKey(id)) {
                buffer.put(id, ts);
            } else {
                buffer.put(id, VectorTimestamp.max(buffer.get(id), other.buffer.get(id)));
            }
        });
    }

    public OrderingBuffer copy() {
        HashMap<Id, VectorTimestamp> bf = new HashMap<>();
        buffer.forEach(bf::put);
        return new OrderingBuffer(bf);
    }

    public String toString() {
        StringBuffer bf = new StringBuffer();
        buffer.forEach((id, ts) -> {
            bf.append("[").append(id).append(" --> ").append(ts).append("]");
        });
        return bf.toString();
    }

    public boolean equals(Object o) {
        OrderingBuffer other = (OrderingBuffer) o;
        return this.buffer.equals(other.buffer);
    }

    public int hashCode() {
        return this.buffer.hashCode();
    }

}
