package MasterSystem;

import Components.PortNumbers;
import Components.Task;
import Components.TaskSocketPair;

import java.io.IOException;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.Map;
import java.util.Queue;
import java.util.concurrent.atomic.AtomicBoolean;

public class TasksToSlavesBroadcaster implements Runnable {
    Queue<TaskSocketPair> TasksSocketsToAssign;
    AtomicBoolean amRunning;
    Map<Task, Socket> clientMap;

    TasksToSlavesBroadcaster(Queue<TaskSocketPair> UnnasignedTaskQueue, AtomicBoolean isRunning,
                             Map<Task, Socket> clientMap) {
        TasksSocketsToAssign = UnnasignedTaskQueue;
        amRunning = isRunning;
        this.clientMap = clientMap;
    }

    // TODO I must remember to make sure that when the server wakes up the thread it sets the status to running
    public void run() {
        TaskSocketPair taskSocket;
        Task task;
        Socket socket;

        while ((taskSocket = TasksSocketsToAssign.poll()) != null) {

            task = taskSocket.task();
            socket = taskSocket.socket();


            if(task != null && socket != null) {
                clientMap.put(task, socket);
                sendTaskToSlave(task);
            }
        }

        amRunning.set(false);
    }

    private void sendTaskToSlave(Task task) {
        try(Socket socket = new Socket("localhost", PortNumbers.SlaveServerPort);
            ObjectOutputStream ooStream = new ObjectOutputStream(socket.getOutputStream())){

            ooStream.writeObject(task);
            ooStream.flush();
            System.out.println("Sent task: " + task.taskID + " to the slave server");
        } catch (IOException e){ System.err.println("Error sending task to slave server: " + e.getMessage()); }
    }
}
