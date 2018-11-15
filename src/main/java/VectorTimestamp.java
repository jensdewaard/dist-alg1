public class VectorTimestamp implements Timestamp {
    final int[] clock;

    public VectorTimestamp(int[] clock) {
        this.clock = clock;
    }

    public boolean leq(Timestamp other) {
        VectorTimestamp vOther = (VectorTimestamp) other;
        boolean leq = true;
        for(int i = 0; i < clock.length; i++) {
            leq = leq && (clock[i] <= vOther.clock[i]);
        }
        return leq;
    }
}
