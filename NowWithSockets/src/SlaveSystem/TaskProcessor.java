package SlaveSystem;

import Components.PortNumbers;
import Components.Task;

import java.io.IOException;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.Queue;
import java.util.concurrent.atomic.AtomicBoolean;

public class TaskProcessor implements Runnable {
    Queue<Task> efficientStore;
    Queue<Task> inEfficientStore;
    AtomicBoolean isRunning;

    String name;

    TaskProcessor(Queue<Task> efficientStore, Queue<Task> inEfficientStore, AtomicBoolean isRunning, String name) {
        this.efficientStore = efficientStore;
        this.inEfficientStore = inEfficientStore;
        this.isRunning = isRunning;
        this.name = name;
    }

    public void run() {
        Task task;

        while (isRunning.get()) {
            task = efficientStore.poll();

            if (task != null) {
                task.efficientExecute();
                System.out.println(name + "Executed task " + task.taskID + " efficiently");
                notifyMaster(task);
                continue;
            }

            task = inEfficientStore.poll();
            if (task != null) {
                task.inefficientExecute();
                System.out.println(name + "Executed task " + task.taskID + " inefficiently");
                notifyMaster(task);
                continue;
            }

            // Ensure proper cleanup when the thread finishes
            //TODO figure out a thread safe way to update status
            isRunning.set(false);
        }
    }

    void notifyMaster(Task task) {
        try (Socket tempSocket = new Socket("localhost", PortNumbers.MasterServerPort);
             ObjectOutputStream outStream = new ObjectOutputStream(tempSocket.getOutputStream())) {

            outStream.writeObject(task);
            outStream.flush();

        } catch (IOException e) {
            System.err.println("Failed to notify master server about task completion: " + e.getMessage());
        }
    }
}
