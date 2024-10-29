package SlaveSystem;

import Components.Task;
import Components.TaskType;

import java.util.concurrent.BlockingQueue;

/**
 * The TaskProcessor class is responsible for processing tasks from a
 * blocking queue. It executes tasks based on their type, using either
 * an efficient or inefficient execution method. Upon completion,
 * processed tasks are added to another blocking queue for further
 * handling.
 *
 * <p>
 * This class implements the Runnable interface, allowing it to run in
 * a separate thread, thus enabling concurrent processing of tasks.
 * </p>
 */
public class TaskProcessor implements Runnable {

    /**
     * The type of task this processor is responsible for.
     */
    TaskType myType;

    /**
     * A blocking queue that stores tasks to be processed.
     */
    BlockingQueue<Task> taskStore;

    /**
     * A blocking queue that stores completed tasks.
     */
    BlockingQueue<Task> completedTasks;

    /**
     * Constructs a TaskProcessor instance with the specified task type,
     * task store, and completed tasks store.
     *
     * @param efficientType the type of task this processor will handle.
     * @param taskStore the blocking queue containing tasks to be processed.
     * @param completedStore the blocking queue where completed tasks will be stored.
     */
    TaskProcessor(TaskType efficientType, BlockingQueue<Task> taskStore, BlockingQueue<Task> completedStore) {
        myType = efficientType;
        this.taskStore = taskStore;
        completedTasks = completedStore;
    }

    /**
     * Continuously processes tasks from the task store.
     * Depending on the task type, it executes the task using either
     * an efficient or inefficient method. Once a task is completed,
     * it is added to the completed tasks queue.
     */
    public void run() {
        Task task;

        while (true) {
            try {
                // Waits for a task to become available for processing
                task = taskStore.take();
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }

            // Execute the task based on its type
            if (task.taskType == myType) {
                task.efficientExecute();  // Execute efficiently for matching type
            } else {
                task.inefficientExecute(); // Execute inefficiently for non-matching type
            }

            System.out.println((myType == TaskType.A ? "SlaveA" : "SlaveB") + " finished task " + task.taskID);

            // Add the completed task to the completed tasks queue
            completedTasks.add(task);
        }
    }
}
