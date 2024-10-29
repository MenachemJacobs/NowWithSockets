package MasterSystem;

import Components.Task;

import java.io.IOException;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.Map;
import java.util.concurrent.BlockingQueue;

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
public class ClientsNotifier implements Runnable {

    /**
     * A blocking queue containing completed tasks that need to be
     * notified to the respective clients.
     */
    BlockingQueue<Task> TasksToNotify;

    /**
     * A map that associates each completed task with its corresponding
     * client socket. This allows the notifier to identify the correct
     * client for each completed task.
     */
    Map<Task, Socket> clientMap;

    /**
     * Constructs a new ClientsNotifier instance.
     *
     * @param CompletedTaskQueue the blocking queue containing completed
     *                           tasks to notify clients about.
     * @param clientMap          a map that associates completed tasks with
     *                           their corresponding client sockets.
     */
    public ClientsNotifier(BlockingQueue<Task> CompletedTaskQueue, Map<Task, Socket> clientMap) {
        TasksToNotify = CompletedTaskQueue;
        this.clientMap = clientMap;
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

            // Retrieve the corresponding client socket for the completed task
            Socket clientSocket = clientMap.get(completedTask);

            // Notify the client if the socket is valid
            if (clientSocket != null) {
                notifyClient(clientSocket, completedTask);
                System.out.println("Client " + completedTask.clientID +
                        " alerted to completion of task " +
                        completedTask.taskID);

                // Close the client socket after notifying
                try {
                    clientSocket.close();
                } catch (IOException e) {
                    System.err.println("Error closing client socket: " + e.getMessage());
                }
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
