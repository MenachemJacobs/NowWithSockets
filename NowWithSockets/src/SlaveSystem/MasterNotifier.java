package SlaveSystem;

import Components.PortNumbers;
import Components.Task;

import java.io.IOException;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.concurrent.BlockingQueue;

/**
 * The MasterNotifier class is responsible for notifying the master server
 * about the completion of tasks. It listens for completed tasks from a
 * blocking queue and sends them to the master server over a socket
 * connection.
 *
 * <p>
 * This class implements the Runnable interface, allowing it to run in a
 * separate thread, thus enabling it to continuously listen for completed
 * tasks without blocking the main execution flow.
 * </p>
 */
public class MasterNotifier implements Runnable {

    /**
     * A blocking queue that holds completed tasks to be notified to the master.
     */
    BlockingQueue<Task> CompletedTasks;
    int slaveListenerPortNum;
    Socket slaveListener = null;
    ObjectOutputStream outStream = null;

    /**
     * Constructs a MasterNotifier instance with the specified blocking queue.
     *
     * @param CompletedTasks the blocking queue containing completed tasks.
     */
    MasterNotifier(int slaveListenerPortNum, BlockingQueue<Task> CompletedTasks) {
        this.CompletedTasks = CompletedTasks;
        this.slaveListenerPortNum = slaveListenerPortNum;
    }

    /**
     * Continuously listens for completed tasks in the blocking queue.
     * Once a task is available, it calls the method to notify the master
     * server.
     */
    public void run() {
        Task task;

        while (true) {
            try {
                // Waits for a completed task
                task = CompletedTasks.take();
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
            // Notify the master server of the completed task
            messageMaster(task);
        }
    }

    /**
     * Sends the completed task to the master server through a socket connection.
     *
     * @param task the completed task to be sent to the master server.
     */
    private void messageMaster(Task task) {
        if (slaveListener == null) {
            try {
                slaveListener = new Socket("localhost", slaveListenerPortNum);
            } catch (IOException e) {
                System.err.println("Error connecting to master: " + e.getMessage());
                return;
            }
        }

        if(outStream == null) {
            try {
                outStream = new ObjectOutputStream(slaveListener.getOutputStream());
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }

        try {
            // Sends the completed task object to the master server
            outStream.writeObject(task);
            outStream.flush();
            System.out.println("Sent task " + task.taskID + " to slaveListener");
        } catch (IOException e) {
            System.err.println("Error sending task to master: " + e.getMessage());
        }
    }
}
