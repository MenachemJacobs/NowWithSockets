package MasterSystem;

import Components.Task;

import java.io.IOException;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * The ClientNotifier class is responsible for notifying clients of the completion of their tasks
 * in a multithreaded server environment. It retrieves completed tasks from a blocking queue
 * and sends them back to the respective clients using their associated sockets.
 *
 * <p>
 * This class implements the {@link Runnable} interface, allowing it to run in a separate thread.
 * It continuously listens for completed tasks and sends notifications to the corresponding clients.
 * </p>
 *
 * <p>
 * The class is designed to handle task completion notification in a thread-safe manner
 * by leveraging a {@link BlockingQueue} to manage completed tasks.
 * </p>
 */
public class ClientNotifier implements Runnable {

    /**
     * A blocking queue that holds completed tasks waiting to be sent to clients.
     * This queue ensures thread-safe communication between the task producer and this notifier.
     */
    public BlockingQueue<Task> completedTasks;

    /**
     * The output stream used to communicate with the client.
     */
    ObjectOutputStream outStream = null;

    /**
     * The socket associated with the client being notified.
     */
    Socket myClient;

    /**
     * Constructs a ClientNotifier instance for a specified client socket.
     *
     * @param clientSocket the socket associated with the client to be notified of task completions.
     */
    public ClientNotifier(Socket clientSocket) {
        myClient = clientSocket;
        completedTasks = new LinkedBlockingQueue<>();
    }

    /**
     * The main execution method for the ClientNotifier. This method runs in a separate thread,
     * continuously listening for completed tasks in the {@code completedTasks} queue.
     *
     * <p>
     * When a task is retrieved, it sends the completed task back to the client
     * via the output stream. If the client socket is invalid or closed,
     * the task is not sent, and an error is logged.
     * </p>
     */
    public void run() {
        Task completedTask;

        try {
            outStream = new ObjectOutputStream(myClient.getOutputStream());
        } catch (IOException e) {
            System.err.println("Error creating output stream: " + e.getMessage());
            return;
        }

        while (true) {
            try {
                // Wait for a completed task to become available
                completedTask = completedTasks.take();
            } catch (InterruptedException e) {
                System.err.println("Thread interrupted: " + e.getMessage());
                return;
            }

            // Notify the client if the socket is valid
            if (myClient != null && !myClient.isClosed()) {
                notifyClient(outStream, completedTask);
                System.out.println("Client " + completedTask.clientID + " alerted to completion of task " +
                        completedTask.taskID);
            } else {
                System.err.println("Client socket is invalid or closed");
            }
        }
    }

    /**
     * Sends a completed task notification to the client.
     *
     * <p>
     * This method writes the completed {@link Task} object to the specified {@link ObjectOutputStream}
     * and flushes the stream to ensure the data is sent. If an error occurs during the process,
     * an error message is logged.
     * </p>
     *
     * @param outStream     the {@link ObjectOutputStream} connected to the client for sending the completed task.
     * @param completedTask the completed {@link Task} object to be sent to the client.
     */
    void notifyClient(ObjectOutputStream outStream, Task completedTask) {
        try {
            outStream.writeObject(completedTask);
            outStream.flush();
            System.out.println("Sent task back to client: " + completedTask.clientID);
        } catch (IOException e) {
            System.err.println("Error notifying client for task ID " +
                    completedTask.taskID + ": " + e.getMessage());
        }
    }
}
