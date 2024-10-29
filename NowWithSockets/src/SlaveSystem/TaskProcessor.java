package SlaveSystem;

import Components.Task;
import Components.TaskType;

import java.util.concurrent.BlockingQueue;

public class TaskProcessor implements Runnable {
    TaskType myType;
    BlockingQueue<Task> taskStore;
    BlockingQueue<Task> completedTasks;

    TaskProcessor(TaskType efficientType, BlockingQueue<Task> taskStore, BlockingQueue<Task> completedStore) {
        myType = efficientType;
        this.taskStore = taskStore;
        completedTasks = completedStore;
    }

    public void run() {
        Task task;

        while (true) {
            try {
                task = taskStore.take();
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }

            if (task.taskType == myType) task.efficientExecute();
            else task.inefficientExecute();

            completedTasks.add(task);
        }
    }
}
