package MasterSystem;

import Components.Task;
import Components.TaskType;

import java.io.IOException;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.concurrent.BlockingQueue;

import static Components.PortNumbers.*;

public class TasksToSlavesBroadcaster implements Runnable {
    BlockingQueue<Task> ToAssign;

    Integer ASlaveTime = 0;
    Integer BSlaveTime = 0;

    public TasksToSlavesBroadcaster(BlockingQueue<Task> UnassignedTaskQueue) {
        ToAssign = UnassignedTaskQueue;
    }

    public void run() {
        Task uncompletedTask;

        while (true) {
            try {
                uncompletedTask = ToAssign.take();
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }

            sendTaskToSlave(uncompletedTask);
        }
    }


    private void sendTaskToSlave(Task task) {
        int portNumber;

        if (task.taskType == TaskType.A) {
            if (ASlaveTime > BSlaveTime + 8) {
                portNumber = BSlavePort;
                BSlaveTime += 10;
            } else {
                portNumber = ASlavePort;
                ASlaveTime += 2;
            }
        } else {
            if (BSlavePort > ASlaveTime + 8) {
                portNumber = ASlavePort;
                ASlaveTime += 10;
            } else {
                portNumber = BSlavePort;
                BSlaveTime += 2;
            }
        }

        try (Socket socket = new Socket("localhost", portNumber);
             ObjectOutputStream ooStream = new ObjectOutputStream(socket.getOutputStream())) {

            ooStream.writeObject(task);
            ooStream.flush();
            System.out.println("Sent task: " + task.taskID + " to the slave server");
        } catch (IOException e) {
            System.err.println("Error sending task to slave server: " + e.getMessage());
        }
    }
}
