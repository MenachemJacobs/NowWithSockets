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
 * The ASlave class represents a slave server that listens for incoming
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
public class SlaveServer implements Runnable {

    private static volatile boolean isRunning = true;

    /**
     * A blocking queue that holds tasks that need to be processed.
     */
    BlockingQueue<Task> UncompletedTasks = new LinkedBlockingQueue<>();
    TaskType myType;

    private SlaveServer(TaskType taskType) {
        myType = taskType;
    }

    /**
     * The main entry point for the ASlave application. This method
     * creates an instance of ASlave and starts the server.
     *
     * @param args command line arguments (not used).
     */
    public static void main(String[] args) {
        SlaveServer aSlave = new SlaveServer(TaskType.A);
        SlaveServer bSlave = new SlaveServer(TaskType.B);
        new Thread(aSlave).start();
        new Thread(bSlave).start();

        // Add shutdown hook
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            System.out.println("Shutdown hook triggered. Stopping servers...");
            isRunning = false;
        }));
    }

    /**
     * Initializes the server socket to listen for incoming connections
     * from clients. It starts the task processor and master notifier
     * in separate threads. The server listens indefinitely for tasks
     * from clients and handles incoming connections.
     */
    public void run() {
        int portNumber = myType == TaskType.A ? PortNumbers.ASlavePort : PortNumbers.BSlavePort;
        int listenerPort = myType == TaskType.A ? PortNumbers.ASlaveListenerPort : PortNumbers.BSlaveListenerPort;

        BlockingQueue<Task> CompletedTasks = new LinkedBlockingQueue<>();
        TaskProcessor myWorker = new TaskProcessor(myType, UncompletedTasks, CompletedTasks);
        MasterNotifier masterNotifier = new MasterNotifier(listenerPort, CompletedTasks);

        // Start threads for processing tasks and notifying the master
        new Thread(myWorker).start();
        new Thread(masterNotifier).start();

        ServerSocket serverSocket;

        // Create a server socket to listen for incoming tasks
        try  {
            serverSocket = new ServerSocket(portNumber);
            System.out.println("System listening on port: " + portNumber);

            // Accept incoming client connection
            Socket clientSocket = serverSocket.accept();
            System.out.println("Accepted connection from " + clientSocket.getInetAddress());

            while (isRunning) {
                try {
                    if(clientSocket.isClosed()) {
                        System.out.println("Lost connection to client. Trying to reconnect...");
                        clientSocket = serverSocket.accept();
                        System.out.println("Accepted connection from " + clientSocket.getInetAddress());
                    }

                    // Process tasks from this connection
                    processIncomingTasks(clientSocket);
                } catch (IOException e) {
                    System.err.println("Error accepting connection: " + e.getMessage());
                    // Wait a bit before trying to accept another connection
                    Thread.sleep(1000);
                }
            }

            serverSocket.close();
        } catch (IOException | InterruptedException e) {
            System.err.println("Server socket error: " + e.getMessage());
        }
    }

    private void processIncomingTasks(Socket clientSocket) {
        ObjectInputStream inputStream;
        String connectionMessage = myType == TaskType.A ? "Slave A received task: "
                : "Slave B received task: ";

        try {
            inputStream = new ObjectInputStream(clientSocket.getInputStream());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        try {
            Object obj = inputStream.readObject();
            if (obj instanceof Task task) {
                System.out.println(connectionMessage + task.taskID);
                UncompletedTasks.put(task);  // Add the task to the UncompletedTasks queue
            }
            clientSocket.close();  // Close the connection after receiving the task
        } catch (ClassNotFoundException e) {
            System.err.println("Received unknown object type: " + e.getMessage());
        } catch (IOException | InterruptedException e) {
            System.err.println("Connection lost while reading task: " + e.getMessage());
        }
    }
}

