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
 * The ClientListener class is responsible for handling connections
 * from clients and receiving tasks to be processed. It listens on
 * a designated port for incoming client connections and forwards
 * received tasks to the TasksToSlavesBroadcaster for distribution
 * to slave servers.
 *
 * <p>
 * This class implements the Runnable interface, allowing it to
 * run in its own thread. Upon receiving a task from a client, it
 * stores the task in a blocking queue and maps the task to the
 * client's socket for future communication.
 * </p>
 */
public class ClientListener implements Runnable {

    /**
     * Message indicating a successful connection with a client.
     */
    String connectionMessage = "Connection made with client";

    /**
     * A map that associates each task with the corresponding clientNotifier
     * socket to enable future communication.
     */
    Map<Task, ClientNotifier> taskNotifierMap;

    /**
     * A blocking queue that holds tasks received from clients
     * that are waiting to be dispatched to slave servers.
     */
    BlockingQueue<Task> uncompletedTasks = new LinkedBlockingQueue<>();

    ExecutorService clientProcessExecutor = Executors.newFixedThreadPool(2);

    Socket clientSocket;
    ClientNotifier myNotifier;

    public ClientListener(Socket clientSocket, Map<Task, ClientNotifier> taskNotifierMap) {
        this.clientSocket = clientSocket;
        this.taskNotifierMap = taskNotifierMap;

        // Initialize and start the dispatcher for uncompleted tasks
        SlaveDispatch dispatch = new SlaveDispatch(uncompletedTasks);
        clientProcessExecutor.execute(dispatch);

        myNotifier = new ClientNotifier(clientSocket);
        clientProcessExecutor.execute(myNotifier);
    }

    /**
     * The main execution method for the ClientListener. This method
     * runs in a separate thread and continuously listens for incoming
     * client connections. When a connection is established, it reads
     * the task object sent by the client and processes it.
     */
    public void run() {
        try (ObjectInputStream ois = new ObjectInputStream(clientSocket.getInputStream())) {
            while (true) {
                Object received = ois.readObject();
                if (received == null) break; // Exit if the client closes the connection
                // Handle incoming task from the connected client
                HandleCommunication(received);
            }
        } catch (IOException | ClassNotFoundException e) {
            System.err.println("Error reading task: " + e.getMessage());
        } finally {
            cleanup(); // Ensure proper cleanup
        }
    }

    /**
     * Processes incoming communication from a client. If the
     * received object is a Task, it is added to the uncompleted
     * tasks queue and mapped to the client's socket.
     *
     * @param object the object received from the client, expected to be a Task.
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

    private void cleanup() {
        try {
            if (clientSocket != null && !clientSocket.isClosed())
                clientSocket.close();
        } catch (IOException e) {
            System.err.println("Error closing client socket: " + e.getMessage());
        }

        // Remove the ClientNotifier from the TaskNotifierMap
        taskNotifierMap.values().removeIf(notifier -> notifier == myNotifier);
        clientProcessExecutor.shutdown();
    }
}
