package MasterSystem;

import Components.Task;
import Components.TaskType;

import java.util.Queue;
import java.util.concurrent.atomic.AtomicBoolean;

public class BroadcastTasksToSlaves implements Runnable {
    Queue<Task> TasksToAssign;
    Queue<Task> ATaskQueue;
    Queue<Task> BTaskQueue;
    AtomicBoolean amRunning;

    BroadcastTasksToSlaves(Queue<Task> UnnasignedTaskQueue, Queue<Task> ATaskQueue, Queue<Task> BTaskQueue,
                           AtomicBoolean isRunning) {
        TasksToAssign = UnnasignedTaskQueue;
        this.ATaskQueue = ATaskQueue;
        this.BTaskQueue = BTaskQueue;
        amRunning = isRunning;
    }

    public void run() {
        amRunning.set(true);
        while (!TasksToAssign.isEmpty()) {
            Task task = TasksToAssign.poll();
            if (task != null) assignTask(task);
        }
        amRunning.set(false);
    }

    private void assignTask(Task task){
//        TODO This logic will have to change a little when number of slaves is variable
        Queue<Task> primaryQueue;
        Queue<Task> secondaryQueue;

        if(task.taskType == TaskType.A){
            primaryQueue = ATaskQueue;
            secondaryQueue = BTaskQueue;
        }
//        else if(task.taskType == TaskType.B){
        else{
            primaryQueue = BTaskQueue;
            secondaryQueue = primaryQueue;
        }

        if (!secondaryQueue.isEmpty()) primaryQueue.add(task);
        else secondaryQueue.add(task);
    }
}
