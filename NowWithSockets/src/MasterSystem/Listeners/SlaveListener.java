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
 * The SlaveListener class is responsible for receiving completed tasks
 * from slave servers. It listens for connections on specific ports
 * based on task types and forwards completed tasks to the corresponding
 * {@link ClientNotifier} instances for notifying clients.
 *
 * <p>
 * This class implements the {@link Runnable} interface, enabling it to run
 * in its own thread. It ensures that completed tasks received from slave
 * servers are processed efficiently and mapped back to the clients
 * who originated the tasks.
 * </p>
 */
public class SlaveListener implements Runnable {

    /**
     * The type of task (A or B) this listener is responsible for.
     */
    private final TaskType taskType;

    /**
     * A map linking tasks to their corresponding {@link ClientNotifier}.
     * This ensures that completed tasks are correctly assigned back to clients.
     */
    private final Map<Task, ClientNotifier> TaskNotifierMap;

    /**
     * A flag indicating whether the listener is running. This is used
     * for gracefully stopping the listener.
     */
    private volatile boolean isRunning;

    /**
     * Constructs a new SlaveListener instance.
     *
     * @param myType         the {@link TaskType} this listener is responsible for.
     * @param TaskNotifierMap a map associating tasks with their {@link ClientNotifier}.
     * @param isRunning       a flag to indicate whether the listener should start running.
     */
    public SlaveListener(TaskType myType, Map<Task, ClientNotifier> TaskNotifierMap, Boolean isRunning) {
        this.TaskNotifierMap = TaskNotifierMap;
        taskType = myType;
        this.isRunning = isRunning;
    }

    /**
     * The main execution method for the SlaveListener. It creates a server socket
     * to listen for incoming connections from slave servers. Once a connection
     * is established, it reads and processes tasks sent by the slave.
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

    /**
     * Determines the port number for this listener based on the assigned task type.
     *
     * @param taskType the type of task this listener handles (A or B).
     * @return the port number for the corresponding task type.
     * @throws IllegalArgumentException if an unknown task type is provided.
     */
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
     * Processes incoming objects from a slave server. If the object is a {@link Task},
     * it is assigned to the corresponding {@link ClientNotifier} for further processing.
     *
     * <p>
     * This method ensures that tasks are properly matched to the client that
     * submitted them. If the task has no associated {@link ClientNotifier},
     * an error is logged.
     * </p>
     *
     * @param object the object received from the slave server, expected to be a {@link Task}.
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
