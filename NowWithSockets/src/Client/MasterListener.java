package Client;

import Components.Task;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.net.Socket;

public class MasterListener implements Runnable {
    private final Socket masterSystemSocket;

    public MasterListener(Socket masterSystemSocket) {
        this.masterSystemSocket = masterSystemSocket;
        if (masterSystemSocket.isClosed()) System.err.println("Master System has closed the connection.");
        else System.out.println("socket is open in the MasterListener constructor");
    }

    @Override
    public void run() {
        if (masterSystemSocket.isClosed()) System.err.println("Master System has closed the connection.");
        else System.out.println("socket is open in the MasterListener run method");

        ObjectInputStream inTask;
        try {
            inTask = new ObjectInputStream(masterSystemSocket.getInputStream());

            while (!masterSystemSocket.isClosed()) {
                Object response = inTask.readObject();
                if (response instanceof Task completedTask)
                    System.out.println("Task completed: " + completedTask.taskID + " of type: " + completedTask.taskType);
                else System.out.println("Received unknown response from server.");
            }
        } catch (IOException | ClassNotFoundException e) {
            System.err.println("Error in client reading response: " + e.getMessage());
        }

        if (masterSystemSocket.isClosed()) System.err.println("Master System has closed the connection.");
    }
}