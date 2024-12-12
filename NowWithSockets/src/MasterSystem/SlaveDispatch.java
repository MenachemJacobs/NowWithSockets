package MasterSystem;

import Components.SlaveSocketManager;
import Components.Task;
import Components.TaskType;

import java.io.IOException;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.atomic.AtomicInteger;

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
public class SlaveDispatch implements Runnable {

    /**
     * A blocking queue that holds tasks waiting to be assigned
     * to slave servers for processing.
     */
    BlockingQueue<Task> ToAssign;

    static final AtomicInteger ASlaveTime = new AtomicInteger(0);
    static final AtomicInteger BSlaveTime = new AtomicInteger(0);
    static final int EFFICIENT_TIME = 2;
    static final int INEFFICIENT_TIME = 10;
    static final int TIME_THRESHOLD = 8;

    /**
     * Constructs a new TasksToSlavesBroadcaster instance.
     *
     * @param UnassignedTaskQueue the blocking queue containing tasks
     *                            that are waiting to be assigned to slaves.
     */
    public SlaveDispatch(BlockingQueue<Task> UnassignedTaskQueue) {
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
                // Send the retrieved task to the selected slave server
                sendTaskToSlave(uncompletedTask);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
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
    static void sendTaskToSlave(Task task) {
        int portNumber;

        synchronized (SlaveDispatch.class) {
            // Determine which slave to send the task to based on task type and current workload
            if (task.taskType == TaskType.A) {
                if (ASlaveTime.get() > BSlaveTime.get() + TIME_THRESHOLD) {
                    portNumber = BSlavePort;
                    BSlaveTime.addAndGet(INEFFICIENT_TIME); // Increase workload for Slave B
                } else {
                    portNumber = ASlavePort;
                    ASlaveTime.addAndGet(EFFICIENT_TIME); // Increase workload for Slave A
                }
            } else {
                if (BSlaveTime.get() > ASlaveTime.get() + TIME_THRESHOLD) {
                    portNumber = ASlavePort;
                    ASlaveTime.addAndGet(INEFFICIENT_TIME); // Increase workload for Slave A
                } else {
                    portNumber = BSlavePort;
                    BSlaveTime.addAndGet(EFFICIENT_TIME); // Increase workload for Slave B
                }
            }
        }

        // Adjust the processing times of the slaves after sending the task
        int minTime = min(ASlaveTime.get(), BSlaveTime.get());
        ASlaveTime.addAndGet(-minTime);
        BSlaveTime.addAndGet(-minTime);


        // Establish a socket connection to the selected slave server and send the task
        try {
            Socket socket = SlaveSocketManager.getSlaveSocket(portNumber);

            try (ObjectOutputStream ooStream = new ObjectOutputStream(socket.getOutputStream())) {
                ooStream.writeObject(task);
                ooStream.flush();
                System.out.println("Sent task: " + task.taskID + " to slave server " + (portNumber == ASlavePort ? "Slave A" : "Slave B"));
            } finally {
                SlaveSocketManager.releaseSlaveSocket(portNumber);
            }

        } catch (IOException | InterruptedException e) {
            System.err.println("Error sending task to slave server: " + e.getMessage());
        }
    }
}