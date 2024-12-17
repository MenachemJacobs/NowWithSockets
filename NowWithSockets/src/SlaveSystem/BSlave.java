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
 * tasks and processes them accordingly. It operates on a designated
 * port and handles task execution in a separate thread.
 *
 * <p>
 * The class maintains two blocking queues: one for tasks to be
 * processed and another for completed tasks. Upon receiving a task,
 * it adds it to the queue for processing. After completing the task,
 * it notifies the master server of the task's completion.
 * </p>
 */
public class BSlave {

    /**
     * A blocking queue that holds tasks that need to be processed.
     */
    BlockingQueue<Task> UncompletedTasks = new LinkedBlockingQueue<>();

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
        String connectionMessage = "Slave B connected to Master System";

        BlockingQueue<Task> CompletedTasks = new LinkedBlockingQueue<>();
        TaskProcessor myWorker = new TaskProcessor(TaskType.B, UncompletedTasks, CompletedTasks);
        MasterNotifier masterNotifier = new MasterNotifier(PortNumbers.BSlaveListenerPort, CompletedTasks);

        // Start threads for processing tasks and notifying the master
        new Thread(myWorker).start();
        new Thread(masterNotifier).start();

        // Create a server socket to listen for incoming tasks
        try (ServerSocket serverSocket = new ServerSocket(portNumber)) {
            System.out.println("System listening on port: " + portNumber);

            while (true) {
                try {
                    // Accept incoming client connection
                    Socket clientSocket = serverSocket.accept();
                    System.out.println("Accepted connection from " + clientSocket.getInetAddress());

                    // Process tasks from this connection
                    processIncomingTasks(clientSocket);
                } catch (IOException e) {
                    System.err.println("Error accepting connection: " + e.getMessage());
                    // Wait a bit before trying to accept another connection
                    Thread.sleep(1000);
                }
            }
        } catch (IOException | InterruptedException e) {
            System.err.println("Server socket error: " + e.getMessage());
        }
    }

    private void processIncomingTasks(Socket clientSocket) {
        ObjectInputStream inputStream;

        try {
            inputStream = new ObjectInputStream(clientSocket.getInputStream());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        while (!clientSocket.isClosed()) {
            try {
                Object obj = inputStream.readObject();
                if (obj instanceof Task task) {
                    System.out.println("BSlave received task: " + task.taskID);
                    UncompletedTasks.put(task);  // Add the task to the UncompletedTasks queue
                }
            } catch (ClassNotFoundException e) {
                System.err.println("Received unknown object type: " + e.getMessage());
            } catch (IOException | InterruptedException e) {
                System.err.println("Connection lost while reading task: " + e.getMessage());
                break;
            }
        }
    }
}
