package MasterSystem;

import Components.Task;
import Components.TaskType;

import java.io.IOException;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.concurrent.BlockingQueue;

import static Components.PortNumbers.*;
import static java.lang.Math.min;

/**
 * The TasksToSlavesBroadcaster class is responsible for distributing
 * tasks to slave servers based on their types and the current workload
 * of each slave. It manages the assignment of tasks to either Slave A
 * or Slave B, ensuring balanced processing times.
 *
 * <p>
 * This class implements the Runnable interface, allowing it to run
 * in its own thread. It continuously retrieves unassigned tasks from
 * a blocking queue and forwards them to the appropriate slave server
 * for processing.
 * </p>
 */
public class TasksToSlavesBroadcaster implements Runnable {

    /**
     * A blocking queue that holds tasks waiting to be assigned
     * to slave servers for processing.
     */
    BlockingQueue<Task> ToAssign;

    /**
     * The accumulated processing time for tasks assigned to Slave A.
     */
    Integer ASlaveTime = 0;

    /**
     * The accumulated processing time for tasks assigned to Slave B.
     */
    Integer BSlaveTime = 0;

    /**
     * Constructs a new TasksToSlavesBroadcaster instance.
     *
     * @param UnassignedTaskQueue the blocking queue containing tasks
     *                            that are waiting to be assigned to slaves.
     */
    public TasksToSlavesBroadcaster(BlockingQueue<Task> UnassignedTaskQueue) {
        ToAssign = UnassignedTaskQueue;
    }

    /**
     * The main execution method for the TasksToSlavesBroadcaster.
     * This method runs in a separate thread and continuously waits
     * for unassigned tasks to become available. When a task is
     * retrieved, it is sent to the appropriate slave server.
     */
    public void run() {
        Task uncompletedTask;

        while (true) {
            try {
                // Wait for an unassigned task to become available
                uncompletedTask = ToAssign.take();
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }

            // Send the retrieved task to the selected slave server
            sendTaskToSlave(uncompletedTask);
        }
    }

    /**
     * Sends the specified task to the appropriate slave server based
     * on the type of task and the current workload of each slave.
     *
     * <p>
     * This method updates the processing times for the slaves based
     * on the task type and chooses the most suitable slave to handle
     * the task. After sending the task, it adjusts the workload
     * times of both slaves to reflect the task assignment.
     * </p>
     *
     * @param task the task to be sent to the slave server for processing.
     */
    private void sendTaskToSlave(Task task) {
        int portNumber;

        // Determine which slave to send the task to based on task type and current workload
        if (task.taskType == TaskType.A) {
            if (ASlaveTime > BSlaveTime + 8) {
                portNumber = BSlavePort;
                BSlaveTime += 10; // Increase workload for Slave B
            } else {
                portNumber = ASlavePort;
                ASlaveTime += 2; // Increase workload for Slave A
            }
        } else {
            if (BSlaveTime > ASlaveTime + 8) {
                portNumber = ASlavePort;
                ASlaveTime += 10; // Increase workload for Slave A
            } else {
                portNumber = BSlavePort;
                BSlaveTime += 2; // Increase workload for Slave B
            }
        }

        // Establish a socket connection to the selected slave server and send the task
        try (Socket socket = new Socket("localhost", portNumber);
             ObjectOutputStream ooStream = new ObjectOutputStream(socket.getOutputStream())) {

            ooStream.writeObject(task);
            ooStream.flush();
            System.out.println("Sent task: " + task.taskID + " to slave server " +
                    (portNumber == ASlavePort ? "Slave A" : "Slave B"));
        } catch (IOException e) {
            System.err.println("Error sending task to slave server: " + e.getMessage());
        }

        // Adjust the processing times of the slaves after sending the task
        int min = min(ASlaveTime, BSlaveTime);
        ASlaveTime -= min;
        BSlaveTime -= min;
    }
}