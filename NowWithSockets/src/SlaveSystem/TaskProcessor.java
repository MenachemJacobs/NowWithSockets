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
    Socket socket;

    TaskProcessor(Queue<Task> efficientStore, Queue<Task> inEfficientStore, AtomicBoolean isRunning, String name) {
        this.efficientStore = efficientStore;
        this.inEfficientStore = inEfficientStore;
        this.isRunning = isRunning;
        this.name = name;

        try {
            socket = new Socket("localhost", PortNumbers.MasterServerPort);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
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
            }
        }

        // Ensure proper cleanup when the thread finishes
        cleanup();
        //TODO figure out a thread safe way to update status
        isRunning.set(false);
    }

    void notifyMaster(Task task) {
        try (ObjectOutputStream outStream = new ObjectOutputStream(socket.getOutputStream())) {

            outStream.writeObject(task);
            outStream.flush();
        } catch (IOException e) {
            System.err.println("Failed to notify master server about task completion: " + e.getMessage());
        }
    }

    void cleanup() {
        try{
            if(socket != null && !socket.isClosed()) {
                socket.close();
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
