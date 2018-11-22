import java.io.Serializable;

public class Id implements Serializable {
    private final int id;

    public Id(int id) {
        this.id = id;
    }

    public String toString() {
        return "" + id;
    }

    public boolean equals(Object o) {
        Id other = (Id) o;
        return other.id == this.id;
    }

    public int hashCode() {
       return Integer.valueOf(id).hashCode();
    }
}
