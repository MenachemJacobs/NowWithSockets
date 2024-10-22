package SlaveSystem;

import Components.Task;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.atomic.AtomicBoolean;

public class TaskProcessor implements Runnable {
    BlockingQueue<Task> efficientStore;
    BlockingQueue<Task> inEfficientStore;
    AtomicBoolean isRunning;
    private final Object efficientLock;
    private final Object inefficientLock;

    String name;

    TaskProcessor(BlockingQueue<Task> efficientStore, BlockingQueue<Task> inEfficientStore, Object efficientLock,
                  Object inefficientLock, AtomicBoolean isRunning, String name) {
        this.efficientStore = efficientStore;
        this.inEfficientStore = inEfficientStore;
        this.isRunning = isRunning;
        this.efficientLock = efficientLock;
        this.inefficientLock = inefficientLock;

        this.name = name;
    }

    public void run() {
        Task task;
        boolean runAgain;

        do {
            task = null;
            runAgain = false;

            if (!efficientStore.isEmpty()) {
               synchronized (efficientLock) {
                   task = efficientStore.poll();
               }

                if(task != null) {
                    task.efficientExecute();
                    System.out.println(name + "Executed task " + task.taskID + " efficiently");
                    runAgain = true;
                }
            }

            if (task == null && !inEfficientStore.isEmpty()) {
                synchronized (inefficientLock) {
                    task = inEfficientStore.poll();
                }

                if(task != null) {
                    task.inefficientExecute();
                    System.out.println(name + "Executed task " + task.taskID + " inefficiently");
                    runAgain = true;
                }
            }
        } while (runAgain);

        isRunning.set(false);
    }
}
