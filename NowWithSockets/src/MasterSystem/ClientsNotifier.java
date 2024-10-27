package MasterSystem;

import Components.Task;

import java.io.IOException;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.Map;
import java.util.Queue;

public class ClientsNotifier implements Runnable {
    Queue<Task> TasksToNotify;
    Map<Task, Socket> clientMap;
    private volatile boolean running = false;

    ClientsNotifier(Queue<Task> CompletedTaskQueue, Map<Task, Socket> clientMap) {
        TasksToNotify = CompletedTaskQueue;
        this.clientMap = clientMap;
    }

    public void run() {
        running = true;

        while (running) {
            Task completedTask = TasksToNotify.poll();

            if (completedTask != null) {
                Socket clientSocket = clientMap.get(completedTask);

                if (clientSocket != null) {
                    notifyClient(clientSocket, completedTask);
                    System.out.println("Task ID " + completedTask.taskID + " client ID " + completedTask.clientID);

                    try {
                        clientSocket.close();
                    } catch (IOException e) {
                        System.err.println("Error closing client socket: " + e.getMessage());
                    }
                }
            } else running = false;
        }
    }

    public void restart() {
        if (!running) {
            Thread thread = new Thread(this);
            thread.start();
        }
    }

    void notifyClient(Socket client, Task completedTask) {
        try (ObjectOutputStream outStream = new ObjectOutputStream(client.getOutputStream())) {
            outStream.writeObject(completedTask);
            outStream.flush();
            System.out.println("Sent task back to client: " + completedTask.clientID);
        } catch (IOException e) {
            System.err.println("Error notifying client for task ID " + completedTask.taskID + ": " + e.getMessage());
        }
    }
}
