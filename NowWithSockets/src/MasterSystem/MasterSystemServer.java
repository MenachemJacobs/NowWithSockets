package MasterSystem;

import Components.PortNumbers;
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
 * The MasterSystemServer class manages client connections and task delegation
 * between clients and slave listeners. It serves as the central server for
 * handling task submissions and processing in a distributed system.
 */
public class MasterSystemServer {
    /**
     * An executor service for handling client connections and tasks asynchronously.
     */
    ExecutorService clientExecutor = Executors.newCachedThreadPool();

    /**
     * An executor service with a fixed number of threads for managing slave listeners.
     */
    ExecutorService slaveExecutor = Executors.newFixedThreadPool(2);

    /**
     * A flag to indicate whether the server is running. It is used to control
     * the server's main loop and safely shut down the server.
     */
    private volatile static Boolean isRunning = true;

    /**
     * A map that associates each task with its corresponding client socket.
     * This allows the server to track which client submitted each task
     * for later notification upon task completion.
     */
    Map<Task, ClientNotifier> TaskNotifierMap = new ConcurrentHashMap<>();

    /**
     * Constructs a new MasterSystemServer instance. This constructor initializes
     * the client and slave listeners and starts them in separate threads.
     *
     * <p>
     * - Listens for incoming client connections on a predefined port. <br>
     * - Delegates task handling to slave listeners based on task type. <br>
     * - Manages the lifecycle of the server and thread pools.
     * </p>
     */
    public MasterSystemServer() {
        slaveExecutor.execute(new SlaveListener(TaskType.A, TaskNotifierMap, isRunning));
        slaveExecutor.execute(new SlaveListener(TaskType.B, TaskNotifierMap, isRunning));

        try (ServerSocket serverSocket = new ServerSocket(PortNumbers.MasterClientPort)) {
            System.out.println("MasterSystemServer is running...");

            while (isRunning) {
                Socket clientSocket = serverSocket.accept();
                System.out.println("New client connected: " + clientSocket.getInetAddress());
                clientExecutor.execute(new ClientListener(clientSocket, TaskNotifierMap, isRunning));
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
     *
     * <p>
     * This method creates an instance of MasterSystemServer, effectively starting
     * the server and its listening functionalities. A shutdown hook is added to
     * ensure proper cleanup and termination of server resources upon application exit.
     * </p>
     *
     * @param args Command line arguments (not used in this implementation).
     */
    public static void main(String[] args) {
        new MasterSystemServer();

        // Add shutdown hook
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            System.out.println("Shutdown hook triggered. Stopping servers...");
            isRunning = false;
        }));
    }
}
