package MasterSystem;

import Components.Task;

import java.net.Socket;
import java.util.Map;
import java.util.Queue;
import java.util.concurrent.atomic.AtomicBoolean;

public class ClientsNotifier implements Runnable {
    Queue<Task> TasksToNotify;
    AtomicBoolean amRunning;
    private final Object completedLock;
    Map<Task, Socket> clientMap;
    final Object clientLock;

    ClientsNotifier(Queue<Task> CompletedTaskQueue, Object QueueLock, AtomicBoolean isRunning,
                    Map<Task, Socket> clientMap, Object clientLock){
        TasksToNotify = CompletedTaskQueue;
        completedLock = QueueLock;
        amRunning = isRunning;
        this.clientMap = clientMap;
        this.clientLock = clientLock;
    }

    public void run() {
        while (!TasksToNotify.isEmpty()) {
            Socket client = null;
            Task completedTask;

            synchronized (completedLock) {
                completedTask = TasksToNotify.poll();
            }
            TasksToNotify.notifyAll();

            if (completedTask != null) {
                synchronized (clientLock) {
                    client = clientMap.get(completedTask);
                    clientMap.remove(completedTask);
                }
                clientLock.notifyAll();
            }

            if(client != null){
                notifyClient(client, completedTask);
                System.out.println("Task ID " + completedTask.taskID + " client ID " + completedTask.clientID);
            }
        }
        amRunning.set(false);
    }

    void notifyClient(Socket client, Task completedTask){
        // Send the completed task to the client and close the socket
    }
}
