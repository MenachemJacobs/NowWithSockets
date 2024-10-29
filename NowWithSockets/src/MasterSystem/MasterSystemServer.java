package MasterSystem;

import Components.PortNumbers;
import Components.Task;
import Components.TaskSocketPair;
import MasterSystem.Listeners.ClientListener;
import MasterSystem.Listeners.ListenerServer;
import MasterSystem.Listeners.SlaveListener;

import java.net.Socket;
import java.util.Map;
import java.util.concurrent.*;

public class MasterSystemServer {
    Map<Task, Socket> clientMap = new ConcurrentHashMap<>();

    ClientListener clientListener;
    SlaveListener slaveListener;

    public MasterSystemServer() {
        clientListener = new ClientListener(clientMap);
        slaveListener = new SlaveListener(clientMap);

        new Thread(clientListener).start();
        new Thread(slaveListener).start();
    }

    public static void main(String[] args) {
        MasterSystemServer masterSystem = new MasterSystemServer();
        masterSystem.startServer();
    }

    private void startServer() {
            while (true) {}
    }
}
