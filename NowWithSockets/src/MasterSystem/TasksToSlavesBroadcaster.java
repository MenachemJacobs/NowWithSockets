package MasterSystem;

import Components.PortNumbers;
import Components.Task;

import java.io.IOException;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.Queue;
import java.util.concurrent.atomic.AtomicBoolean;

public class TasksToSlavesBroadcaster implements Runnable {
    Queue<Task> TasksToAssign;
    AtomicBoolean amRunning;
    private final Object lock;

    TasksToSlavesBroadcaster(Queue<Task> UnnasignedTaskQueue, Object QueueLock, AtomicBoolean isRunning) {
        TasksToAssign = UnnasignedTaskQueue;
        lock = QueueLock;
        amRunning = isRunning;
    }

    // TODO I must remember to make sure that when the server wakes up the thread it sets the status to running
    public void run() {
        Task task;

        while (!TasksToAssign.isEmpty()) {
            synchronized (lock) {
                task = TasksToAssign.poll();
            }

            if (task != null) {
                sendTaskToSlave(task);
            }
        }

        amRunning.set(false);
    }

    private void sendTaskToSlave(Task task) {
        try(Socket socket = new Socket("localhost", PortNumbers.SlaveServerPort);
            ObjectOutputStream ooStream = new ObjectOutputStream(socket.getOutputStream())){

            ooStream.writeObject(task);
            ooStream.flush();
            System.out.println("Sent task: " + task.taskID + " to the slave server");
        } catch (IOException e){ System.err.println("Error sending task to slave server: " + e.getMessage()); }
    }
}
