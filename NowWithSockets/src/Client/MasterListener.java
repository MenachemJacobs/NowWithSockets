package Client;

import Components.Task;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.net.Socket;

public class MasterListener implements Runnable {
    private final Socket masterSystemSocket;

    public MasterListener(Socket masterSystemSocket) {
        this.masterSystemSocket = masterSystemSocket;
    }

    @Override
    public void run() {
        try (ObjectInputStream inTask = new ObjectInputStream(masterSystemSocket.getInputStream())) {
            while (true) {
                // Wait for the server to send a response indicating task completion
                Object response = inTask.readObject();
                if (response instanceof Task completedTask) {
                    System.out.println("Task completed: " + completedTask.taskID + " of type: " + completedTask.taskType);
                } else {
                    System.out.println("Received unknown response from server.");
                }
            }
        } catch (IOException | ClassNotFoundException e) {
            System.err.println("Error in client reading response: " + e.getMessage());
        }
    }
}