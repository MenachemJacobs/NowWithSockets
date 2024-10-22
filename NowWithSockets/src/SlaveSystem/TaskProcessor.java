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
    private final Object efficientLock;
    private final Object inefficientLock;

    String name;

    TaskProcessor(Queue<Task> efficientStore, Queue<Task> inEfficientStore, Object efficientLock,
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
               efficientLock.notifyAll();

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
                inefficientLock.notifyAll();

                if(task != null) {
                    task.inefficientExecute();
                    System.out.println(name + "Executed task " + task.taskID + " inefficiently");
                    runAgain = true;
                }
            }

            if(task != null) notifyMaster(task);
        } while (runAgain);

        isRunning.set(false);
    }

    // TODO: WHy is everything always creating the socket? Make the socket in Constructor?
    void notifyMaster(Task task) {
        try(Socket socket = new Socket("localhost", PortNumbers.MasterServerPort);
            ObjectOutputStream outStream = new ObjectOutputStream(socket.getOutputStream())) {

            outStream.writeObject(task);
            outStream.flush();
        } catch (IOException e){
            System.err.println("Failed to notify master server about task completion: " + e.getMessage());
        }
    }
}
