import java.io.Serializable;

public interface Timestamp extends Serializable {
    boolean leq(Timestamp other);
    boolean gt(Timestamp other);
    Timestamp copy();
}
