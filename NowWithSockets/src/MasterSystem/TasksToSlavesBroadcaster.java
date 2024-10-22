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
    private final Object dispatchLock;
    Map<Task, Socket> clientMap;
    private final Object clientLock;

    TasksToSlavesBroadcaster(Queue<TaskSocketPair> UnnasignedTaskQueue, Object QueueLock, AtomicBoolean isRunning,
                             Map<Task, Socket> clientMap, Object clientLock) {
        TasksSocketsToAssign = UnnasignedTaskQueue;
        dispatchLock = QueueLock;
        amRunning = isRunning;
        this.clientMap = clientMap;
        this.clientLock = clientLock;
    }

    // TODO I must remember to make sure that when the server wakes up the thread it sets the status to running
    public void run() {
        TaskSocketPair taskSocket;
        Task task = null;
        Socket socket = null;

        while (!TasksSocketsToAssign.isEmpty()) {
            synchronized (dispatchLock) {
                taskSocket = TasksSocketsToAssign.poll();
            }
            dispatchLock.notifyAll();

            if (taskSocket != null) {
                task = taskSocket.task();
                socket = taskSocket.socket();
            }

            if(task != null && socket != null) {
                synchronized (clientLock) {
                    clientMap.put(task, socket);
                }
                clientLock.notifyAll();
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
