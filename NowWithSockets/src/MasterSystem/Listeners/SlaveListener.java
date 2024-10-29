package MasterSystem.Listeners;

import Components.PortNumbers;
import Components.Task;
import MasterSystem.ClientsNotifier;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * The SlaveListener class is responsible for listening to incoming
 * connections from slave servers and receiving completed tasks.
 * It listens on a designated port for incoming connections from
 * slave servers, processes received tasks, and forwards them to
 * the ClientsNotifier for notifying clients.
 *
 * <p>
 * This class implements the Runnable interface, allowing it to
 * run in its own thread. Upon receiving a completed task from a
 * slave server, it stores the task in a blocking queue for
 * further processing.
 * </p>
 */
public class SlaveListener implements Runnable {

    /**
     * Message indicating a successful connection with a slave server.
     */
    String connectionMessage = "Connection made with client";

    /**
     * A blocking queue that holds completed tasks received from
     * slave servers waiting to be notified to clients.
     */
    BlockingQueue<Task> completedTasks = new LinkedBlockingQueue<>();

    /**
     * A map that associates each completed task with the
     * corresponding client socket for future communication.
     */
    Map<Task, Socket> clientMap;

    /**
     * The notifier responsible for notifying clients about
     * completed tasks.
     */
    ClientsNotifier Replier;

    /**
     * Constructs a new SlaveListener instance.
     *
     * @param clientMap a map that associates completed tasks with
     *                  client sockets, allowing for task tracking
     *                  and communication.
     */
    public SlaveListener(Map<Task, Socket> clientMap) {
        this.clientMap = clientMap;

        // Initialize and start the notifier for completed tasks
        Replier = new ClientsNotifier(completedTasks, clientMap);
        new Thread(Replier).start();
    }

    /**
     * The main execution method for the SlaveListener. This method
     * runs in a separate thread and continuously listens for incoming
     * slave server connections. When a connection is established,
     * it reads the task object sent by the slave and processes it.
     */
    public void run() {
        int portNumber = PortNumbers.MasterSlavePort;

        try (ServerSocket serverSocket = new ServerSocket(portNumber)) {
            System.out.println("System listening on port: " + portNumber);

            while (true) {
                Socket socket = serverSocket.accept();
                System.out.println(connectionMessage);

                // Handle incoming task from the connected slave server
                try (ObjectInputStream objectInputStream = new ObjectInputStream(socket.getInputStream())) {
                    HandleCommunication(objectInputStream.readObject());
                } catch (IOException | ClassNotFoundException e) {
                    System.err.println("Error reading task: " + e.getMessage());
                }
            }
        } catch (IOException e) {
            System.err.println("Error reading task: " + e.getMessage());
        }
    }

    /**
     * Processes incoming communication from a slave server. If the
     * received object is a Task, it is added to the completed tasks
     * queue for notifying the respective clients.
     *
     * @param object the object received from the slave server,
     *               expected to be a Task.
     */
    void HandleCommunication(Object object) {
        if (object instanceof Task task) {
            completedTasks.add(task);
            System.out.println("Received task: " + task.taskID + " from slave");
        } else {
            System.out.println("Received object of unknown type " + object);
        }
    }
}
