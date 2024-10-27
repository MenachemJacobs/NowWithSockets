package MasterSystem;

import Components.PortNumbers;
import Components.Task;
import Components.TaskSocketPair;

import java.io.IOException;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.Map;
import java.util.Queue;

public class TasksToSlavesBroadcaster extends Thread {
    Queue<TaskSocketPair> TasksSocketsToAssign;
    Map<Task, Socket> clientMap;

    TasksToSlavesBroadcaster(Queue<TaskSocketPair> UnnasignedTaskQueue, Map<Task, Socket> clientMap) {
        TasksSocketsToAssign = UnnasignedTaskQueue;
        this.clientMap = clientMap;
    }

    // TODO I must remember to make sure that when the server wakes up the thread it sets the status to running
    public void run() {
        TaskSocketPair taskSocket;
        Task task;
        Socket socket;

        while (true) {
            taskSocket = TasksSocketsToAssign.poll();

            if (taskSocket != null) {
                task = taskSocket.task();
                socket = taskSocket.socket();

                if(task != null && socket != null) {
                    clientMap.put(task, socket);
                    sendTaskToSlave(task);
                }
            }
        }
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
