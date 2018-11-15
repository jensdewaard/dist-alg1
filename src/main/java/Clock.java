public interface Clock {
    public void increment();

    public void update(Timestamp other);

    public Timestamp stamp();
}
