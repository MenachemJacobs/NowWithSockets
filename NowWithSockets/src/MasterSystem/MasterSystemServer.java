package MasterSystem;

import Components.PortNumbers;
import Components.SlaveSocketManager;
import Components.Task;
import Components.TaskType;
import MasterSystem.Listeners.ClientListener;
import MasterSystem.Listeners.SlaveListener;

import java.io.IOException;
import java.net.ServerSocket;
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
    ExecutorService clientExecutor = Executors.newCachedThreadPool();
    ExecutorService slaveExecutor = Executors.newFixedThreadPool(2);

    /**
     * A map that associates each task with its corresponding client socket.
     * This allows the server to track which client submitted each task
     * for later notification upon task completion.
     */
    Map<Task, ClientNotifier> TaskNotifierMap = new ConcurrentHashMap<>();

    /**
     * Constructs a new MasterSystemServer instance. This constructor initializes
     * the client and slave listeners and starts them in separate threads.
     */
    // TODO: Slaves needs their own thread set
    public MasterSystemServer() {
        SlaveSocketManager.addSlaveSocket(PortNumbers.ASlavePort);
        SlaveSocketManager.addSlaveSocket(PortNumbers.BSlavePort);

        slaveExecutor.execute(new SlaveListener(TaskType.A, TaskNotifierMap));
        slaveExecutor.execute(new SlaveListener(TaskType.B, TaskNotifierMap));

        try (ServerSocket serverSocket = new ServerSocket(PortNumbers.MasterClientPort)) {
            System.out.println("MasterSystemServer is running...");

            while (true) {
                Socket clientSocket = serverSocket.accept();
                System.out.println("New client connected: " + clientSocket.getInetAddress());
                clientExecutor.execute(new ClientListener(clientSocket, TaskNotifierMap));
            }
        } catch (IOException e) {
            System.err.println("Server exception: " + e.getMessage());
        } finally {
            clientExecutor.shutdown();
            slaveExecutor.shutdown();
        }
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
