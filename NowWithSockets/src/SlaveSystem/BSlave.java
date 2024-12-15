package SlaveSystem;

import Components.PortNumbers;
import Components.Task;
import Components.TaskType;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * The BSlave class represents a slave server that listens for incoming
 * tasks specifically of type B and processes them accordingly. It operates
 * on a designated port and handles task execution in a separate thread.
 *
 * <p>
 * This class maintains two blocking queues: one for tasks to be
 * processed and another for completed tasks. Upon receiving a task,
 * it adds it to the queue for processing. After completing the task,
 * it notifies the master server of the task's completion.
 * </p>
 */
public class BSlave {

    /**
     * A blocking queue that holds tasks that need to be processed.
     */
    BlockingQueue<Task> TasksToDo = new LinkedBlockingQueue<>();

    /**
     * A blocking queue that holds tasks that have been completed.
     */
    BlockingQueue<Task> Done = new LinkedBlockingQueue<>();

    /**
     * The task processor that executes tasks of type B.
     */
    TaskProcessor myWorker = new TaskProcessor(TaskType.B, TasksToDo, Done);

    /**
     * The notifier that communicates completed tasks back to the master.
     */
    MasterNotifier masterNotifier;

    /**
     * The main entry point for the BSlave application. This method
     * creates an instance of BSlave and starts the server.
     *
     * @param args command line arguments (not used).
     */
    public static void main(String[] args) {
        BSlave bSlave = new BSlave();
        bSlave.StartServer();
    }

    /**
     * Initializes the server socket to listen for incoming connections
     * from clients. It starts the task processor and master notifier
     * in separate threads. The server listens indefinitely for tasks
     * from clients and handles incoming connections.
     */
    void StartServer() {
        int portNumber = PortNumbers.BSlavePort;
        String connectionMessage = "Slave B receiving a task";

        masterNotifier = new MasterNotifier(PortNumbers.BSlaveListenerPort, Done);

        // Start threads for processing tasks and notifying the master
        new Thread(myWorker).start();
        new Thread(masterNotifier).start();

        try (ServerSocket serverSocket = new ServerSocket(portNumber)) {
            System.out.println("System listening on port: " + portNumber);

            while (true) {
                Socket socket = serverSocket.accept();
                System.out.println(connectionMessage);
                receiveTask(socket);
            }
        } catch (IOException e) {
            System.err.println("Error reading task: " + e.getMessage());
        }
    }

    /**
     * Receives a task from a connected client through the provided socket.
     * It reads the task object from the input stream and adds it to the
     * TasksToDo queue for processing.
     *
     * @param socket the socket through which the task is received.
     */
    void receiveTask(Socket socket) {
        try (ObjectInputStream inputStream = new ObjectInputStream(socket.getInputStream())) {
            Object obj = inputStream.readObject();
            if (obj instanceof Task task) {
                System.out.println("BSlave received task: " + task.taskID);
                TasksToDo.put(task);
            }
        } catch (IOException | ClassNotFoundException | InterruptedException e) {
            System.err.println("Error reading task: " + e.getMessage());
        }
    }
}
