package MasterSystem;

import Components.Task;

import java.io.IOException;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.Map;
import java.util.concurrent.BlockingQueue;

public class ClientsNotifier implements Runnable {
    BlockingQueue<Task> TasksToNotify;
    Map<Task, Socket> clientMap;

    public ClientsNotifier(BlockingQueue<Task> CompletedTaskQueue, Map<Task, Socket> clientMap) {
        TasksToNotify = CompletedTaskQueue;
        this.clientMap = clientMap;
    }

    public void run() {
        Task completedTask;

        while (true) {
            try {
                completedTask = TasksToNotify.take();
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }

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
