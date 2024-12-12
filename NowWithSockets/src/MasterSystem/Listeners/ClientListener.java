package MasterSystem.Listeners;

import Components.Task;
import MasterSystem.ClientNotifier;
import MasterSystem.SlaveDispatch;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.net.Socket;
import java.util.Map;
import java.util.concurrent.BlockingQueue;
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
     * A blocking queue that holds tasks received from clients
     * that are waiting to be dispatched to slave servers.
     */
    BlockingQueue<Task> uncompletedTasks = new LinkedBlockingQueue<>();

    /**
     * A map that associates each task with the corresponding client
     * socket to enable future communication.
     */
    Map<Task, ClientNotifier> taskNotifierMap;

    /**
     * The dispatcher responsible for sending tasks to slave servers.
     */
    SlaveDispatch dispatcher;

    Socket clientSocket;
    ClientNotifier myNotifier;

    public ClientListener(Socket clientSocket) {
        this.clientSocket = clientSocket;

        // Initialize and start the dispatcher for uncompleted tasks
        dispatcher = new SlaveDispatch(uncompletedTasks);
        new Thread(dispatcher).start();
        myNotifier = new ClientNotifier(clientSocket);
        new Thread(myNotifier).start();
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
                // Handle incoming task from the connected client
                HandleCommunication(ois.readObject());
            }
        } catch (IOException | ClassNotFoundException e) {
            System.err.println("Error reading task: " + e.getMessage());
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
            uncompletedTasks.add(task);
            taskNotifierMap.put(task, myNotifier);
        } else {
            System.out.println("Received Object of unknown type: " + object);
        }
    }
}
