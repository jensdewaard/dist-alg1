import java.io.Serializable;

public class Id implements Serializable {
    private final int id;

    public Id(int id) {
        this.id = id;
    }

    public String toString() {
        return "" + id;
    }
}
