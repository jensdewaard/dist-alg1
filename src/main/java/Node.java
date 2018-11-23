import java.rmi.Remote;
import java.rmi.RemoteException;

public interface Node extends Remote {
    void receiveMessage(String message, OrderingBuffer Sm, Timestamp Vm) throws RemoteException;
}
