package MasterSystem;

import Components.Task;

import java.io.IOException;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * The ClientsNotifier class is responsible for notifying clients
 * of the completion of their tasks in a multi-threaded server
 * environment. It retrieves completed tasks from a blocking queue
 * and sends them back to the corresponding clients using their
 * associated sockets.
 *
 * <p>
 * This class implements the Runnable interface, allowing it to
 * be executed in a separate thread. It continuously checks for
 * completed tasks and notifies the respective clients accordingly.
 * </p>
 *
 * <p>
 * It maintains a mapping of tasks to client sockets to ensure
 * that each completed task is communicated back to the correct client.
 * </p>
 */
public class ClientNotifier implements Runnable {

    /**
     * A blocking queue containing completed tasks that need to be
     * notified to the respective clients.
     */
    public BlockingQueue<Task> TasksToNotify;

    Socket myClient;

    public ClientNotifier(Socket clientSocket) {
        myClient = clientSocket;
        TasksToNotify = new LinkedBlockingQueue<>();
    }

    /**
     * The main execution method for the ClientsNotifier. This method
     * runs in a separate thread and continuously waits for completed
     * tasks to notify clients. When a task is retrieved from the queue,
     * it sends a notification to the corresponding client.
     */
    public void run() {
        Task completedTask;

        while (true) {
            try {
                // Wait for a completed task to become available
                completedTask = TasksToNotify.take();
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }

            // Notify the client if the socket is valid
            if (myClient != null) {
                notifyClient(myClient, completedTask);
                System.out.println("Client " + completedTask.clientID + " alerted to completion of task " +
                        completedTask.taskID);
            }
        }
    }

    /**
     * Sends the completed task back to the client through the specified
     * socket. This method creates an ObjectOutputStream to write the
     * task object and flushes the stream to ensure the data is sent.
     *
     * @param client        the socket connected to the client that will receive
     *                      the completed task notification.
     * @param completedTask the completed task object to be sent to the client.
     */
    void notifyClient(Socket client, Task completedTask) {
        try (ObjectOutputStream outStream = new ObjectOutputStream(client.getOutputStream())) {
            outStream.writeObject(completedTask);
            outStream.flush();
            System.out.println("Sent task back to client: " + completedTask.clientID);
        } catch (IOException e) {
            System.err.println("Error notifying client for task ID " +
                    completedTask.taskID + ": " + e.getMessage());
        }
    }
}
