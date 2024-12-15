package Client;

import Components.Task;

import java.io.ObjectInputStream;

public class MasterListener implements Runnable {
    private final ObjectInputStream inTask;

    public MasterListener(ObjectInputStream inTask) {
        this.inTask = inTask;
    }

    @Override
    public void run() {
        try {
            while (true) {
                // Wait for the server to send a response indicating task completion
                Object response = inTask.readObject();
                if (response instanceof Task completedTask) {
                    System.out.println("Task completed: " + completedTask.taskID + " of type: " + completedTask.taskType
                            + " from Client ID: " + completedTask.clientID);
                } else {
                    System.out.println("Received unknown response from server.");
                }
            }
        } catch (java.io.IOException | ClassNotFoundException e) {
            System.err.println("Error in client reading response: " + e.getMessage());
        }
    }
}