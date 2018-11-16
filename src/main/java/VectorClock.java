public class VectorClock implements Clock {
    private int[] clock;
    private int index;
    private int size;

    public VectorClock(int index, int size) {
        clock = new int[size];
        this.index = index;
        this.size = size;
    }

    public void increment() {
        clock[index] += 1;
    }

    public void update(Timestamp other) {
        VectorTimestamp c = (VectorTimestamp) other;
        for(int i = 0; i < size; i++) {
//            if(i != index) {
                clock[i] = Math.max(clock[i], c.clock[i]);
//            }
        }
        increment();
    }

    public Timestamp stamp() {
        return new VectorTimestamp(clock);
    }
}
