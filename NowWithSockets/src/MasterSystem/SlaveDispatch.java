package MasterSystem;

import Components.Task;
import Components.TaskType;

import java.io.IOException;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

import static Components.PortNumbers.*;
import static java.lang.Math.max;

/**
 * The SlaveDispatch class is responsible for distributing tasks to slave servers
 * in a balanced manner based on the task type and the current workload of each slave.
 *
 * <p>
 * The class implements the {@link Runnable} interface, enabling it to run in a separate thread.
 * It continuously retrieves unprocessed tasks from a {@link BlockingQueue} and forwards
 * them to the most suitable slave server for execution.
 * </p>
 *
 * <p>
 * This class uses atomic counters to maintain thread-safe tracking of each slave server's
 * current workload, ensuring efficient task assignment even in a multithreaded environment.
 * </p>
 */
public class SlaveDispatch implements Runnable {

    /**
     * A blocking queue containing tasks waiting to be assigned to slave servers.
     * Tasks are retrieved from this queue and dispatched to the appropriate slave.
     */
    BlockingQueue<Task> uncompletedTasks;

    /**
     * An atomic counter tracking the accumulated processing time for Slave A.
     */
    static final AtomicInteger ASlaveTime = new AtomicInteger(0);

    /**
     * An atomic counter tracking the accumulated processing time for Slave B.
     */
    static final AtomicInteger BSlaveTime = new AtomicInteger(0);

    /**
     * The timestamp of the last task assignment. Used to adjust workload times dynamically.
     */
    private static final AtomicLong lastTimestamp = new AtomicLong(System.currentTimeMillis());

    /**
     * A flag indicating whether the dispatch thread is running. Used for graceful shutdown.
     */
    private volatile Boolean running = true;

    /**
     * Constructs a new SlaveDispatch instance and assigns a task queue.
     *
     * @param UnassignedTaskQueue the blocking queue containing unprocessed tasks.
     */
    public SlaveDispatch(BlockingQueue<Task> UnassignedTaskQueue) {
        uncompletedTasks = UnassignedTaskQueue;
    }

    /**
     * The main execution method for the SlaveDispatch. This method runs in a separate thread,
     * continuously polling the {@code uncompletedTasks} queue for tasks.
     *
     * <p>
     * When a task becomes available, it is assigned to the appropriate slave server.
     * If the thread is interrupted, it terminates gracefully.
     * </p>
     */
    @Override
    public void run() {
        Task uncompletedTask;

        while (running) {
            try {
                if (!uncompletedTasks.isEmpty()) {
                    // Wait for an unassigned task to become available
                    uncompletedTask = uncompletedTasks.take();
                    sendTaskToSlave(uncompletedTask);
                }
            } catch (InterruptedException e) {
                if (!running) break;
                throw new RuntimeException(e);
            }
        }
    }

    /**
     * Sends a task to the appropriate slave server based on its type and the current workload.
     * This method attempts to establish a connection with the selected slave server
     * and retries if the connection fails.
     *
     * @param task the {@link Task} to be sent to the slave server.
     */
    static void sendTaskToSlave(Task task) {
        int portNumber = chooseSlave(task.taskType);

        adjustProcessTime();

        int retries = 3;

        while (retries-- > 0) {

            // Establish a socket connection to the selected slave server and send the task
            try {
                // TODO: Get SlaveSocketManager to work
                Socket socket = new Socket("localhost", portNumber);
                ObjectOutputStream ooStream;

                ooStream = new ObjectOutputStream(socket.getOutputStream());
                ooStream.writeObject(task);
                ooStream.flush();
                System.out.println("Sent task: " + task.taskID + " to slave server " + (portNumber == ASlavePort ? "Slave A" : "Slave B"));
                socket.close();
                return;

            } catch (IOException e) {
                retries--;
                System.err.println("Error sending task to slave server, retries left: " + retries);
                if (retries == 0) System.err.println("Task failed after 3 retries: " + task.taskID);
            }
        }
    }

    /**
     * Selects the most suitable slave server for the task based on its type and the workload.
     * This method dynamically adjusts the processing time of each slave to account for current load.
     *
     * @param type the {@link TaskType} of the task being assigned.
     * @return the port number of the chosen slave server.
     */
    private static synchronized int chooseSlave(TaskType type) {
        // Processing times for each slave and task type
        int aSlaveTypeATime = 2;   // A Slave takes 2 seconds for Type A tasks
        int aSlaveTypeBTime = 10;  // A Slave takes 10 seconds for Type B tasks
        int bSlaveTypeATime = 10;  // B Slave takes 10 seconds for Type A tasks
        int bSlaveTypeBTime = 2;   // B Slave takes 2 seconds for Type B tasks

        // Current accumulated workload times
        int aSlaveCurrentTime = ASlaveTime.get();
        int bSlaveCurrentTime = BSlaveTime.get();

        // Determine processing time for each slave based on task type
        int aSlaveExpectedTime = (type == TaskType.A) ?
                aSlaveCurrentTime + aSlaveTypeATime :
                aSlaveCurrentTime + aSlaveTypeBTime;

        int bSlaveExpectedTime = (type == TaskType.A) ?
                bSlaveCurrentTime + bSlaveTypeATime :
                bSlaveCurrentTime + bSlaveTypeBTime;

        // Choose the slave with the shortest expected completion time
        if (aSlaveExpectedTime <= bSlaveExpectedTime) {
            // Update A Slave time based on task type
            ASlaveTime.addAndGet((type == TaskType.A) ? aSlaveTypeATime : aSlaveTypeBTime);
            return ASlavePort;
        } else {
            // Update B Slave time based on task type
            BSlaveTime.addAndGet((type == TaskType.A) ? bSlaveTypeATime : bSlaveTypeBTime);
            return BSlavePort;
        }
    }

    /**
     * Dynamically adjusts the workload times for both slaves based on the time
     * elapsed since the last task assignment. This ensures that idle time
     * reduces the perceived load of each slave.
     */
    private static void adjustProcessTime(){
        int timeSinceLastTask = (int) (System.currentTimeMillis() - lastTimestamp.get());

        ASlaveTime.set(max(0, ASlaveTime.get() - timeSinceLastTask));
        BSlaveTime.set(max(0, BSlaveTime.get() - timeSinceLastTask));

        lastTimestamp.set(System.currentTimeMillis());
    }

    /**
     * Gracefully shuts down the dispatcher by setting the {@code running} flag to false
     * and interrupting the current thread.
     */
    public void shutdown() {
        running = false;
        Thread.currentThread().interrupt();
    }
}