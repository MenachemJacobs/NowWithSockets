package MasterSystem;

import Components.Task;

import java.util.Queue;
import java.util.concurrent.atomic.AtomicBoolean;

public class ClientsNotifier implements Runnable {
    Queue<Task> TasksToNotify;
    AtomicBoolean amRunning;
    private final Object lock;

    ClientsNotifier(Queue<Task> CompletedTaskQueue, Object QueueLock, AtomicBoolean isRunning){
        TasksToNotify = CompletedTaskQueue;
        lock = QueueLock;
        amRunning = isRunning;
    }

    public void run() {
        for (Task task : TasksToNotify) {
            System.out.println("Task ID " + task.taskID + " client ID " + task.clientID);
        }
    }
}
