package SlaveSystem;

import Components.PortNumbers;
import Components.Task;

import java.io.IOException;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.concurrent.BlockingQueue;

public class MasterNotifier implements Runnable {
    BlockingQueue<Task> CompletedTasks;

    MasterNotifier(BlockingQueue<Task> CompletedTasks) {
        this.CompletedTasks = CompletedTasks;
    }

    public void run() {
        Task task;

        while (true) {
            try {
                task = CompletedTasks.take();
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
            messageMaster(task);
        }
    }

    private void messageMaster(Task task) {
        try (Socket tempSocket = new Socket("localhost", PortNumbers.MasterSlavePort);
             ObjectOutputStream outStream = new ObjectOutputStream(tempSocket.getOutputStream())) {
            outStream.writeObject(task);
            outStream.flush();
        } catch (IOException ignored) {
        }
    }
}
