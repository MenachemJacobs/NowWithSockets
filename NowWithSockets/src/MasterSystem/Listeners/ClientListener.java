package MasterSystem.Listeners;

import Components.Task;
import MasterSystem.ClientNotifier;
import MasterSystem.SlaveDispatch;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.net.Socket;
import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * The ClientListener class is responsible for managing the interaction with a connected client.
 * It receives tasks from the client, processes them, and forwards them to be dispatched to slave servers.
 *
 * <p>
 * This class implements the {@link Runnable} interface, allowing it to run in its own thread.
 * It listens for objects sent from the client, verifies that they are tasks, and adds them to
 * a task queue for further processing.
 * </p>
 */
public class ClientListener implements Runnable {

    private volatile static Boolean isRunning = null;

    /**
     * A map that links each {@link Task} with its corresponding {@link ClientNotifier},
     * enabling communication with the client that submitted the task.
     */
    Map<Task, ClientNotifier> taskNotifierMap;

    /**
     * A blocking queue containing tasks received from clients that are yet to be processed
     * or dispatched to slave servers.
     */
    BlockingQueue<Task> uncompletedTasks = new LinkedBlockingQueue<>();

    /**
     * An executor service for managing threads that handle task dispatch and client notifications.
     */
    ExecutorService clientProcessExecutor = Executors.newFixedThreadPool(2);

    /**
     * The socket associated with the connected client.
     */
    Socket clientSocket;

    /**
     * The notifier responsible for communicating with the client.
     */
    ClientNotifier myNotifier;

    /**
     * Constructs a ClientListener instance for a connected client.
     *
     * <p>
     * This constructor initializes the blocking queue, task notifier map, and starts the
     * {@link SlaveDispatch} and {@link ClientNotifier} threads.
     * </p>
     *
     * @param clientSocket the socket connected to the client.
     * @param taskNotifierMap a map linking tasks to their corresponding client notifiers.
     */
    public ClientListener(Socket clientSocket, Map<Task, ClientNotifier> taskNotifierMap, Boolean isRunning) {
        this.clientSocket = clientSocket;
        this.taskNotifierMap = taskNotifierMap;
        if(ClientListener.isRunning == null) ClientListener.isRunning = isRunning;

        // Initialize and start the dispatcher for uncompleted tasks
        SlaveDispatch dispatch = new SlaveDispatch(uncompletedTasks);
        clientProcessExecutor.execute(dispatch);

        myNotifier = new ClientNotifier(clientSocket);
        clientProcessExecutor.execute(myNotifier);
    }

    /**
     * The main execution method for the ClientListener.
     *
     * <p>
     * This method continuously listens for objects sent by the client. If a {@link Task}
     * is received, it is processed and added to the task queue. If an error occurs or the
     * client disconnects, the listener stops processing and performs cleanup.
     * </p>
     */
    public void run() {
        ObjectInputStream ois;

        if (clientSocket.isClosed()) System.err.println("Master System has closed the connection.");
        else System.out.println("socket is open");

        try {
            ois = new ObjectInputStream(clientSocket.getInputStream());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        while (isRunning) {
            if (clientSocket.isClosed()) {
                System.err.println("Client socket is already closed, exiting loop.");
                break;
            }

            try {
                Object received = ois.readObject();
                if (received == null) {
                    System.err.println("Received null object, exiting loop.");
                    break; // Exit if the client closes the connection
                }
                // Handle incoming task from the connected client
                HandleCommunication(received);
            } catch (IOException | ClassNotFoundException e) {
                System.err.println("Error reading task: " + e.getMessage() + ". Something is clearly wrong with the ois");
                break; // Exit the loop on error
            }
        }

        cleanup();
    }

    /**
     * Processes incoming communication from the client.
     *
     * <p>
     * If the received object is a {@link Task}, it is added to the {@code uncompletedTasks} queue
     * and mapped to the client's socket in the {@code taskNotifierMap}.
     * </p>
     *
     * @param object the object received from the client, expected to be a {@link Task}.
     */
    void HandleCommunication(Object object) {
        if (object instanceof Task task) {
            System.out.println("Received task: " + task.taskID + " from client " + task.clientID);

            // The order here is important, log tasks before letting anyone work on them!
            taskNotifierMap.put(task, myNotifier);
            uncompletedTasks.add(task);
        } else {
            System.out.println("Received Object of unknown type: " + object);
        }
    }

    /**
     * Cleans up resources associated with the client.
     *
     * <p>
     * This method closes the client socket, removes the client's notifier
     * from the {@code taskNotifierMap}, and shuts down the executor service.
     * </p>
     */
    private void cleanup() {
        try {
            if (clientSocket != null && !clientSocket.isClosed()) {
                clientSocket.close();
            }
        } catch (IOException e) {
            System.err.println("Error closing client socket: " + e.getMessage());
        }

        // Remove the ClientNotifier from the TaskNotifierMap
        taskNotifierMap.values().removeIf(notifier -> notifier == myNotifier);
        clientProcessExecutor.shutdown();
    }
}
