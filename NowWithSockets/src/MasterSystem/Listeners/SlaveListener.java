package MasterSystem.Listeners;

import Components.PortNumbers;
import Components.Task;
import Components.TaskType;
import MasterSystem.ClientNotifier;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Map;

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

    String connectionMessage = "Connection made with client";
    private final TaskType taskType;
    private final Map<Task, ClientNotifier> TaskNotifierMap;
    private volatile boolean isRunning;

    public SlaveListener(TaskType myType, Map<Task, ClientNotifier> TaskNotifierMap, Boolean isRunning) {
        this.TaskNotifierMap = TaskNotifierMap;
        taskType = myType;
        this.isRunning = isRunning;
    }

    /**
     * The main execution method for the SlaveListener. This method
     * runs in a separate thread and continuously listens for incoming
     * slave server connections. When a connection is established,
     * it reads the task object sent by the slave and processes it.
     */
    @Override
    public void run() {
        ServerSocket serverSocket;
        Socket slaveSocket;
        ObjectInputStream inputStream = null;

        try {
            serverSocket = new ServerSocket(getPortForTaskType(taskType));
            slaveSocket = serverSocket.accept();

            if (serverSocket.isClosed() || slaveSocket.isClosed()) {
                System.err.println("Server or slave socket is closed");
                return;
            }

            while (isRunning) {
                if (inputStream == null)
                    inputStream = new ObjectInputStream(slaveSocket.getInputStream());

                try {
                    // Process tasks from the slave socket
                    Object receivedObject = inputStream.readObject();
                    HandleCommunication(receivedObject);
                } catch (IOException | ClassNotFoundException e) {
                    System.err.println("Error reading from input stream: " + e.getMessage());
                    inputStream = null; // Reset the input stream to refresh it
                }
            }

            inputStream.close();
            slaveSocket.close();
            serverSocket.close();

        } catch (IOException e) {
            System.err.println("Error establishing server socket: " + e.getMessage() + " in Slave Listener");
        }
    }

    private int getPortForTaskType(TaskType taskType) {
        // Return the appropriate port number based on the task type
        if (taskType == TaskType.A) {
            return PortNumbers.ASlaveListenerPort;
        } else if (taskType == TaskType.B) {
            return PortNumbers.BSlaveListenerPort;
        }
        throw new IllegalArgumentException("Unknown task type: " + taskType);
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
            // Identify the client notifier to assign the completed task
            ClientNotifier clientNotifier = TaskNotifierMap.get(task);

            if (clientNotifier == null) {
                System.err.println("ClientNotifier not found for task: " + task.taskID + " from client: " + task.clientID);
                return;
            }

            clientNotifier.completedTasks.add(task);
            System.out.println("Received task: " + task.taskID + " from slave");
        } else {
            System.out.println("Received object of unknown type " + object);
        }
    }
}
