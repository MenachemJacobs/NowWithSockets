package MasterSystem;

import Components.Task;
import MasterSystem.Listeners.ClientListener;
import MasterSystem.Listeners.SlaveListener;

import java.net.Socket;
import java.util.Map;
import java.util.concurrent.*;

/**
 * The MasterSystemServer class represents the main server component
 * of the Master-Slave architecture. It is responsible for managing
 * communication between clients and slaves, including receiving tasks
 * from clients and dispatching them to slave workers.
 *
 * <p>
 * The MasterSystemServer initializes two key components:
 * <ul>
 *     <li>{@link ClientListener}: Listens for incoming connections from clients
 *     and handles task submissions.</li>
 *     <li>{@link SlaveListener}: Listens for incoming connections from slaves
 *     and manages the completion of tasks.</li>
 * </ul>
 * </p>
 *
 * <p>
 * This server runs both listeners in separate threads to handle
 * concurrent connections and ensure non-blocking operation.
 * </p>
 *
 * @see ClientListener
 * @see SlaveListener
 */
public class MasterSystemServer {

    /**
     * A map that associates each task with its corresponding client socket.
     * This allows the server to track which client submitted each task
     * for later notification upon task completion.
     */
    Map<Task, Socket> clientMap = new ConcurrentHashMap<>();

    /**
     * The listener responsible for handling client connections and task submissions.
     */
    ClientListener clientListener;

    /**
     * The listener responsible for handling slave connections and task completions.
     */
    SlaveListener slaveListener;

    /**
     * Constructs a new MasterSystemServer instance. This constructor initializes
     * the client and slave listeners and starts them in separate threads.
     */
    public MasterSystemServer() {
        clientListener = new ClientListener(clientMap);
        slaveListener = new SlaveListener(clientMap);

        new Thread(clientListener).start();
        new Thread(slaveListener).start();
    }

    /**
     * The entry point for the MasterSystemServer application.
     * This method creates an instance of MasterSystemServer,
     * effectively starting the server and its listening functionalities.
     *
     * @param args command line arguments (not used).
     */
    public static void main(String[] args) {
        new MasterSystemServer();
    }
}
