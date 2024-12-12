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
    TaskType myType;
    Map<Task, ClientNotifier> TaskNotifierMap;
    Socket slaveSocket;

    //TODO distinguish slaves in the listeners, persist the sockets
    public SlaveListener(TaskType myType, Map<Task, ClientNotifier> TaskNotifierMap) {
        this.TaskNotifierMap = TaskNotifierMap;
        this.myType = myType;
        int portNumber = myType == TaskType.A ? PortNumbers.ASlavePort : PortNumbers.BSlavePort;

        try {
            ServerSocket serverSocket = new ServerSocket(portNumber);
            System.out.println("System listening on port: " + portNumber);
            this.slaveSocket = serverSocket.accept();
            System.out.println(connectionMessage);
        } catch (IOException e) {
            System.err.println("Error establishing connection: " + e.getMessage());
        }
    }

    /**
     * The main execution method for the SlaveListener. This method
     * runs in a separate thread and continuously listens for incoming
     * slave server connections. When a connection is established,
     * it reads the task object sent by the slave and processes it.
     */
    public void run() {
        while (true) {
            // Handle incoming task from the connected slave server
            try (ObjectInputStream objectInputStream = new ObjectInputStream(slaveSocket.getInputStream())) {
                HandleCommunication(objectInputStream.readObject());
            } catch (IOException | ClassNotFoundException e) {
                System.err.println("Error reading task: " + e.getMessage());
            }
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
            TaskNotifierMap.get(task).TasksToNotify.add(task);
            System.out.println("Received task: " + task.taskID + " from slave");
        } else {
            System.out.println("Received object of unknown type " + object);
        }
    }
}
